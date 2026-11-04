package com.neilb.synapcart.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.AppLocale
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val sessionManager: SessionManager,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow(AppLocale.current())
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(
        if (AppLocale.current() == AppLocale.TURKISH) "TRY" else "USD"
    )
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    /** Switches the UI language right away, so the rest of onboarding is already in it. */
    fun selectLanguage(lang: String) {
        _selectedLanguage.value = lang
        AppLocale.apply(lang)
    }

    fun selectCurrency(curr: String) {
        _selectedCurrency.value = curr
    }

    fun finishOnboarding() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            userUseCases.updateProfile(
                fullName = null,
                language = _selectedLanguage.value,
                currency = _selectedCurrency.value
            ).fold(
                onSuccess = {
                    sessionManager.saveOnboardingCompleted(true)
                    _isComplete.value = true
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_generic)) }
            )
            _isLoading.value = false
        }
    }
}
