package com.neilb.synapcart.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _selectedLanguage = MutableStateFlow("tr")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _selectedCurrency = MutableStateFlow("TRY")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isComplete = MutableStateFlow(false)
    val isComplete: StateFlow<Boolean> = _isComplete.asStateFlow()

    fun selectLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    fun selectCurrency(curr: String) {
        _selectedCurrency.value = curr
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = userUseCases.updateProfile(
                fullName = null,
                language = _selectedLanguage.value,
                currency = _selectedCurrency.value
            )

            result.fold(
                onSuccess = {
                    sessionManager.saveOnboardingCompleted(true)
                    _isComplete.value = true
                },
                onFailure = { exception ->
                    // İleride buraya hata göstermek için bir State eklenebilir
                    // _error.value = exception.message
                }
            )

            _isLoading.value = false
        }
    }
}