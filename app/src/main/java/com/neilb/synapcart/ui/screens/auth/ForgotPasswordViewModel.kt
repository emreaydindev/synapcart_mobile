package com.neilb.synapcart.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.domain.use_case.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // E-posta başarıyla gönderildi mi?
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _error.value = null
    }

    fun sendResetLink() {
        if (_email.value.isBlank()) {
            _error.value = "Lütfen geçerli bir e-posta adresi girin."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authUseCases.forgotPassword(_email.value)

            result.fold(
                onSuccess = {
                    _isSuccess.value = true
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Bağlantı gönderilemedi. Lütfen tekrar deneyin."
                }
            )

            _isLoading.value = false
        }
    }
}