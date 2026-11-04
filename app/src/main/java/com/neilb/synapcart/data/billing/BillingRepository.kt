package com.neilb.synapcart.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.neilb.synapcart.BuildConfig
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.VerifyPurchaseRequest
import com.neilb.synapcart.data.remote.BillingApiService
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.domain.repository.UserRepository
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/** One purchasable base plan of the premium subscription. */
data class PremiumOffer(
    val basePlanId: String,
    val offerToken: String,
    /** Recurring price, e.g. "₺59,99". */
    val formattedPrice: String,
    val priceMicros: Long,
    /** ISO 8601 period of the recurring phase, e.g. "P1M" / "P1Y". */
    val billingPeriod: String,
    /** ISO 8601 length of a free trial, if this offer has one. */
    val freeTrialPeriod: String?
)

sealed interface BillingState {
    data object Loading : BillingState
    /** Play Billing isn't available (no Play Store, product not set up, network). */
    data object Unavailable : BillingState
    data class Ready(val monthly: PremiumOffer?, val yearly: PremiumOffer?) : BillingState {
        /** Whole-percent saving of yearly over 12 monthly payments, if both are known. */
        val yearlySavingPercent: Int?
            get() = savingPercent(monthly?.priceMicros, yearly?.priceMicros)
    }
}

sealed interface PurchaseEvent {
    /** Verified on the server; the plan is now premium. */
    data object Success : PurchaseEvent
    /** The payment is pending (e.g. cash); premium starts once it completes. */
    data object Pending : PurchaseEvent
    data object Cancelled : PurchaseEvent
    data class Failed(val message: UiText) : PurchaseEvent
}

fun savingPercent(monthlyMicros: Long?, yearlyMicros: Long?): Int? {
    if (monthlyMicros == null || yearlyMicros == null || monthlyMicros <= 0) return null
    val saving = 100.0 * (1 - yearlyMicros.toDouble() / (monthlyMicros * 12.0))
    return saving.toInt().takeIf { it > 0 }
}

/**
 * Google Play Billing for the `synapcart_premium` subscription: loads the monthly/yearly
 * offers, launches the purchase flow, verifies every purchase on the backend
 * (`/billing/google/verify`), then acknowledges it and refreshes `/user/me`.
 */
class BillingRepository(
    context: Context,
    private val billingApi: BillingApiService,
    private val userRepository: UserRepository,
    private val planRepository: UserPlanRepository,
    private val sessionManager: SessionManager
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectMutex = Mutex()
    private var productDetails: ProductDetails? = null
    private var restored = false

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val _state = MutableStateFlow<BillingState>(BillingState.Loading)
    val state: StateFlow<BillingState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<PurchaseEvent> = _events.asSharedFlow()

    private suspend fun ensureConnected(): Boolean = connectMutex.withLock {
        if (client.isReady) return true
        suspendCancellableCoroutine { cont ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) cont.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
    }

    /** Loads the subscription's base plans into [state]. */
    suspend fun loadOffers() {
        if (_state.value !is BillingState.Ready) _state.value = BillingState.Loading
        if (!ensureConnected()) {
            _state.value = BillingState.Unavailable
            return
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BuildConfig.PREMIUM_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        val result = client.queryProductDetails(params)
        val details = result.productDetailsList?.firstOrNull()
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK || details == null) {
            Log.w(TAG, "Product details unavailable: ${result.billingResult.responseCode}")
            _state.value = BillingState.Unavailable
            return
        }
        productDetails = details
        _state.value = BillingState.Ready(
            monthly = details.offerFor(BuildConfig.PREMIUM_PLAN_MONTHLY),
            yearly = details.offerFor(BuildConfig.PREMIUM_PLAN_YEARLY)
        )
    }

    /** Opens Google Play's purchase sheet. Returns false if it couldn't be started. */
    suspend fun launchPurchase(activity: Activity, offer: PremiumOffer): Boolean {
        val details = productDetails ?: return false
        if (!ensureConnected()) return false
        // Play links the purchase to this account; the backend rejects it for anyone else.
        val userId = planRepository.state.value.userId
            ?: userRepository.getUserProfile().getOrNull()?.id
            ?: return false
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offer.offerToken)
                        .build()
                )
            )
            .setObfuscatedAccountId(userId)
            .build()
        return client.launchBillingFlow(activity, params).responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        scope.launch {
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach { handlePurchase(it, restoring = false) }
                BillingClient.BillingResponseCode.USER_CANCELED -> _events.emit(PurchaseEvent.Cancelled)
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> restorePurchases(force = true)
                else -> _events.emit(PurchaseEvent.Failed(UiText.Res(R.string.billing_error)))
            }
        }
    }

    /**
     * Re-syncs existing subscriptions with the backend (new device, reinstall, a purchase
     * that was never verified or acknowledged). Runs once per process unless [force]d.
     */
    suspend fun restorePurchases(force: Boolean = false) {
        if (restored && !force) return
        if (sessionManager.authToken.first().isNullOrBlank()) return
        if (!ensureConnected()) return
        restored = true
        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
        result.purchasesList
            .filter { BuildConfig.PREMIUM_PRODUCT_ID in it.products }
            .forEach { handlePurchase(it, restoring = !force) }
    }

    private suspend fun handlePurchase(purchase: Purchase, restoring: Boolean) {
        if (BuildConfig.PREMIUM_PRODUCT_ID !in purchase.products) return
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> if (!restoring) _events.emit(PurchaseEvent.Pending)
            Purchase.PurchaseState.PURCHASED -> {
                val verified = safeApiCall {
                    billingApi.verifyGooglePurchase(
                        VerifyPurchaseRequest(purchase.purchaseToken, BuildConfig.PREMIUM_PRODUCT_ID)
                    )
                }
                verified.onFailure { e ->
                    // Unacknowledged purchases are refunded by Play after 3 days, so a purchase the
                    // server can't verify is never acknowledged here.
                    if (!restoring) _events.emit(PurchaseEvent.Failed(e.toUiText(R.string.billing_verify_failed)))
                    return
                }
                if (!purchase.isAcknowledged) {
                    val ack = client.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                    )
                    if (ack.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(TAG, "Acknowledge failed: ${ack.responseCode}")
                    }
                }
                userRepository.getUserProfile()
                if (!restoring) _events.emit(PurchaseEvent.Success)
            }
        }
    }

    private fun ProductDetails.offerFor(basePlanId: String): PremiumOffer? {
        val offers = subscriptionOfferDetails.orEmpty().filter { it.basePlanId == basePlanId }
        // Prefer a developer offer (e.g. a free trial) over the bare base plan.
        val offer = offers.firstOrNull { it.offerId != null } ?: offers.firstOrNull() ?: return null
        val phases = offer.pricingPhases.pricingPhaseList
        val recurring = phases.lastOrNull() ?: return null
        val trial = phases.firstOrNull { it.priceAmountMicros == 0L }
        return PremiumOffer(
            basePlanId = basePlanId,
            offerToken = offer.offerToken,
            formattedPrice = recurring.formattedPrice,
            priceMicros = recurring.priceAmountMicros,
            billingPeriod = recurring.billingPeriod,
            freeTrialPeriod = trial?.billingPeriod
        )
    }

    companion object {
        private const val TAG = "Billing"

        /** Play's subscription management page for this app. */
        fun manageSubscriptionUrl(packageName: String) =
            "https://play.google.com/store/account/subscriptions?sku=${BuildConfig.PREMIUM_PRODUCT_ID}&package=$packageName"
    }
}
