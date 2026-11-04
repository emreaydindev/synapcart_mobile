package com.neilb.synapcart.shared

import com.neilb.synapcart.data.billing.PlanState
import com.neilb.synapcart.data.model.AgentStreamEvent
import com.neilb.synapcart.data.model.AppleVerifyRequest
import com.neilb.synapcart.data.model.CompareResponse
import com.neilb.synapcart.data.model.DeviceTokenRequest
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.model.SessionResponse
import com.neilb.synapcart.data.model.UserProfile
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.domain.model.ChatMessage
import com.neilb.synapcart.domain.use_case.auth.ForgotPasswordUseCase
import com.neilb.synapcart.domain.use_case.auth.LoginUseCase
import com.neilb.synapcart.domain.use_case.auth.RegisterUseCase
import com.neilb.synapcart.domain.use_case.auth.ResetPasswordUseCase
import com.neilb.synapcart.domain.use_case.favorites.SetPriceAlertUseCase
import com.neilb.synapcart.domain.use_case.user.ChangePasswordUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Swift-friendly facade over [SynapCart]. Kotlin's `Result` and `Flow` don't export well to
 * Objective-C/Swift, so this exposes throwing `suspend` functions (Swift `async throws`;
 * the error's `KotlinException` is an [ApiException]) and callback-based observation
 * returning a [Cancellable]. Android uses the repositories directly instead.
 */
class SynapCartClient(val core: SynapCart) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun <T> Result<T>.unwrap(): T = getOrElse { throw it }

    // --- Session ---

    @Throws(Exception::class)
    suspend fun isSignedIn(): Boolean = !core.session.authToken.first().isNullOrBlank()

    @Throws(Exception::class)
    suspend fun isOnboardingCompleted(): Boolean = core.session.isOnboardingCompleted.first()

    @Throws(Exception::class)
    suspend fun setOnboardingCompleted(done: Boolean) = core.session.saveOnboardingCompleted(done)

    @Throws(Exception::class)
    suspend fun userName(): String? = core.session.userName.first()

    @Throws(Exception::class)
    suspend fun currency(): String = core.session.currency.first()

    @Throws(Exception::class)
    suspend fun language(): String = core.session.language.first()

    @Throws(Exception::class)
    suspend fun savePreferences(language: String?, currency: String?) = core.session.savePreferences(language, currency)

    @Throws(Exception::class)
    suspend fun notificationPromptShown(): Boolean = core.session.notificationPromptShown.first()

    @Throws(Exception::class)
    suspend fun setNotificationPromptShown() = core.session.setNotificationPromptShown()

    /** Calls [onExpired] (on the main thread) whenever the backend rejects the stored token. */
    fun watchSessionExpired(onExpired: () -> Unit): Cancellable = watch(core.sessionEvents.expired, { onExpired() })

    /** The in-memory plan state (refreshed by every profile load). */
    fun watchPlan(onChange: (PlanState) -> Unit): Cancellable = watch(core.plan.state, onChange)

    // --- Auth (with the same client-side validation as Android) ---

    @Throws(Exception::class)
    suspend fun login(email: String, password: String) = LoginUseCase(core.auth)(email, password).unwrap()

    @Throws(Exception::class)
    suspend fun register(fullName: String, email: String, password: String) =
        RegisterUseCase(core.auth)(fullName, email, password).unwrap()

    @Throws(Exception::class)
    suspend fun forgotPassword(email: String) = ForgotPasswordUseCase(core.auth)(email).unwrap()

    @Throws(Exception::class)
    suspend fun resetPassword(token: String, newPassword: String) =
        ResetPasswordUseCase(core.auth)(token, newPassword).unwrap()

    @Throws(Exception::class)
    suspend fun logout() = core.auth.logout()

    // --- User ---

    @Throws(Exception::class)
    suspend fun profile(): UserProfile = core.user.getUserProfile().unwrap()

    @Throws(Exception::class)
    suspend fun updateProfile(fullName: String?, language: String?, currency: String?): UserProfile =
        core.user.updateProfile(fullName, language, currency).unwrap()

    @Throws(Exception::class)
    suspend fun changePassword(current: String, new: String) = ChangePasswordUseCase(core.user)(current, new).unwrap()

    @Throws(Exception::class)
    suspend fun deleteAccount(password: String?) = core.user.deleteAccount(password).unwrap()

    // --- Chat ---

    @Throws(Exception::class)
    suspend fun sessions(): List<SessionResponse> = core.chat.getSessions().unwrap()

    @Throws(Exception::class)
    suspend fun createSession(): SessionResponse = core.chat.createSession().unwrap()

    @Throws(Exception::class)
    suspend fun renameSession(id: Int, title: String): SessionResponse = core.chat.renameSession(id, title).unwrap()

    @Throws(Exception::class)
    suspend fun deleteSession(id: Int) = core.chat.deleteSession(id).unwrap()

    @Throws(Exception::class)
    suspend fun messages(sessionId: Int): List<ChatMessage> = core.chat.getMessages(sessionId).unwrap()

    @Throws(Exception::class)
    suspend fun compare(products: List<ProductDTO>): CompareResponse = core.chat.compareProducts(products).unwrap()

    /**
     * Streams the agent for one message. [onEvent] gets each SSE event on the main thread;
     * [onComplete] gets null on success or the [ApiException]. Cancel to abort the request.
     */
    fun streamAgent(
        sessionId: Int,
        message: String,
        onEvent: (AgentStreamEvent) -> Unit,
        onComplete: (ApiException?) -> Unit
    ): Cancellable = watch(core.stream.streamAgent(sessionId, message), onEvent, onComplete)

    // --- Favorites ---

    fun watchFavorites(onChange: (List<FavoriteResponse>) -> Unit): Cancellable = watch(core.favorites.favorites, onChange)

    fun watchFavoriteLinks(onChange: (Set<String>) -> Unit): Cancellable = watch(core.favorites.favoriteLinks, onChange)

    @Throws(Exception::class)
    suspend fun refreshFavorites(): List<FavoriteResponse> = core.favorites.refresh().unwrap()

    /** Returns the new state: true = now a favorite. */
    @Throws(Exception::class)
    suspend fun toggleFavorite(product: ProductDTO): Boolean = core.favorites.toggle(product).unwrap()

    @Throws(Exception::class)
    suspend fun removeFavorite(favorite: FavoriteResponse) = core.favorites.remove(favorite).unwrap()

    /** Sets (or clears with null) a price alert, favoriting the product first if needed. */
    @Throws(Exception::class)
    suspend fun setPriceAlert(product: ProductDTO, targetPrice: Double?): FavoriteResponse =
        SetPriceAlertUseCase(core.favorites)(product, targetPrice).unwrap()

    // --- Billing and push ---

    /**
     * Sends a StoreKit 2 signed transaction to the backend. Finish the transaction only after
     * this returns; the returned profile is already cached (plan state updated).
     */
    @Throws(Exception::class)
    suspend fun verifyApplePurchase(signedTransaction: String): UserProfile {
        val profile = safeApiCall { core.api.verifyApplePurchase(AppleVerifyRequest(signedTransaction)) }.unwrap()
        core.user.applyProfile(profile)
        return profile
    }

    @Throws(Exception::class)
    suspend fun registerDeviceToken(token: String) {
        if (!isSignedIn()) return
        safeApiCall { core.api.registerDeviceToken(DeviceTokenRequest(token, platform = "ios")) }.unwrap()
    }

    @Throws(Exception::class)
    suspend fun unregisterDeviceToken(token: String) {
        safeApiCall { core.api.unregisterDeviceToken(DeviceTokenRequest(token, platform = "ios")) }
    }

    // --- Observation helpers ---

    private fun <T> watch(flow: Flow<T>, onEach: (T) -> Unit, onComplete: ((ApiException?) -> Unit)? = null): Cancellable {
        var failure: ApiException? = null
        val job = flow
            .onEach { onEach(it) }
            .catch { e ->
                if (e is CancellationException) throw e
                failure = e as? ApiException ?: ApiException.Network()
            }
            .onCompletion { cause -> if (cause !is CancellationException) onComplete?.invoke(failure) }
            .launchIn(scope)
        return Cancellable(job)
    }

    /** Runs [block] on the client scope, for fire-and-forget work started from Swift. */
    fun launch(block: suspend () -> Unit): Cancellable = Cancellable(scope.launch { block() })
}

/** Handle returned to Swift for observations and streams. */
class Cancellable internal constructor(private val job: Job) {
    fun cancel() = job.cancel()
}
