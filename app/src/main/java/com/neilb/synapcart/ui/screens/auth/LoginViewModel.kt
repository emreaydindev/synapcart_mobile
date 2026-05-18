package com.neilb.synapcart.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.domain.use_case.auth.AuthUseCases
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow(false)
    val isLoginSuccess: StateFlow<Boolean> = _isLoginSuccess.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        viewModelScope.launch {
            if (_email.value.isBlank() || _password.value.isBlank()) {
                snackbarController.showSnackbar("Lütfen e-posta ve şifrenizi girin.")
                return@launch
            }

            _isLoading.value = true

            val result = authUseCases.login(_email.value, _password.value)

            result.fold(
                onSuccess = {
                    _isLoginSuccess.value = true
                },
                onFailure = { exception ->
                    snackbarController.showSnackbar(exception.message ?: "Giriş başarısız oldu. Lütfen tekrar deneyin.")
                }
            )

            _isLoading.value = false
        }
    }

    fun resetSuccessState() {
        _isLoginSuccess.value = false
    }
}