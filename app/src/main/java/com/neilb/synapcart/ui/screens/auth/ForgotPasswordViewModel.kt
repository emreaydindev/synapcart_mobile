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
class ForgotPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun sendResetLink() {
        viewModelScope.launch {
            if (_email.value.isBlank()) {
                snackbarController.showSnackbar("Lütfen e-posta adresinizi girin.")
                return@launch
            }
            _isLoading.value = true

            val result = authUseCases.forgotPassword(_email.value)

            result.fold(
                onSuccess = {
                    _isSuccess.value = true
                },
                onFailure = { exception ->
                    snackbarController.showSnackbar(exception.message ?: "Şifre sıfırlama bağlantısı gönderilemedi. Lütfen tekrar deneyin.")
                }
            )

            _isLoading.value = false
        }
    }
}