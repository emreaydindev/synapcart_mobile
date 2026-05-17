package com.neilb.synapcart.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.domain.use_case.auth.AuthUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow(false)
    val isLoginSuccess: StateFlow<Boolean> = _isLoginSuccess.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _error.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _error.value = null
    }

    fun login() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Lütfen e-posta ve şifrenizi girin."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authUseCases.login(_email.value, _password.value)

            result.fold(
                onSuccess = {
                    _isLoginSuccess.value = true
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Giriş başarısız oldu. Lütfen tekrar deneyin."
                }
            )

            _isLoading.value = false
        }
    }

    fun resetSuccessState() {
        _isLoginSuccess.value = false
    }
}