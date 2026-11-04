package com.neilb.synapcart.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.ui.navigation.Screen
import com.neilb.synapcart.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
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
            val token = sessionManager.authToken.firstOrNull()
            val onboardingCompleted = sessionManager.isOnboardingCompleted.firstOrNull() ?: false

            if (token.isNullOrBlank()) {
                _destination.value = Screen.Login.route
            } else if (!onboardingCompleted) {
                _destination.value = Screen.Onboarding.route
            } else {
                _destination.value = Screen.Dashboard.route
            }
        }
    }
}