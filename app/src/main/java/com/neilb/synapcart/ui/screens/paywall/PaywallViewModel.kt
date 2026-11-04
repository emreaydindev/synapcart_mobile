package com.neilb.synapcart.ui.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.billing.BillingRepository
import com.neilb.synapcart.data.billing.BillingState
import com.neilb.synapcart.data.billing.PurchaseEvent
import com.neilb.synapcart.data.billing.UserPlanRepository
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    planRepository: UserPlanRepository,
    sessionManager: SessionManager,
    private val snackbarController: SnackbarController
) : ViewModel() {

    val billingState: StateFlow<BillingState> = billingRepository.state

    val isPremium: StateFlow<Boolean> = sessionManager.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val planExpiresAt: StateFlow<String?> = planRepository.state
        .map { it.expiresAt }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** True from tapping "buy" until Play reports back. */
    private val _purchasing = MutableStateFlow(false)
    val purchasing: StateFlow<Boolean> = _purchasing.asStateFlow()

    /** Fires after a successful purchase so the screen can close. */
    private val _closeEvents = Channel<Unit>(Channel.CONFLATED)
    val closeEvents = _closeEvents.receiveAsFlow()

    init {
        loadOffers()
        viewModelScope.launch {
            billingRepository.events.collect { event ->
                _purchasing.value = false
                when (event) {
                    PurchaseEvent.Success -> {
                        snackbarController.showSnackbar(R.string.billing_success)
                        _closeEvents.send(Unit)
                    }
                    PurchaseEvent.Pending -> snackbarController.showSnackbar(R.string.billing_pending)
                    PurchaseEvent.Cancelled -> Unit
                    is PurchaseEvent.Failed -> snackbarController.showSnackbar(event.message)
                }
            }
        }
    }

    fun loadOffers() {
        viewModelScope.launch { billingRepository.loadOffers() }
    }

    fun purchase(activity: Activity, yearly: Boolean) {
        val ready = billingState.value as? BillingState.Ready ?: return
        val offer = (if (yearly) ready.yearly else ready.monthly) ?: return
        if (_purchasing.value) return
        _purchasing.value = true
        viewModelScope.launch {
            if (!billingRepository.launchPurchase(activity, offer)) {
                _purchasing.value = false
                snackbarController.showSnackbar(R.string.billing_error)
            }
        }
    }
}
