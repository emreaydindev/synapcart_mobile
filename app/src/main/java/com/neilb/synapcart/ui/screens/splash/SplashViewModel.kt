package com.neilb.synapcart.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _destination = MutableStateFlow<String?>(null)
    val destination: StateFlow<String?> = _destination.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            delay(1500)

            val token = sessionManager.authToken.firstOrNull()
            val onboardingCompleted = sessionManager.isOnboardingCompleted.firstOrNull() ?: false

            if (token.isNullOrBlank()) {
                _destination.value = "login_screen"
            } else if (!onboardingCompleted) {
                _destination.value = "onboarding_screen"
            } else {
                _destination.value = "dashboard_screen"
            }
        }
    }
}