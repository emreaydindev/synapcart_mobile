package com.neilb.synapcart.ui.screens.auth

import androidx.lifecycle.SavedStateHandle
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
class ResetPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val token: String? = savedStateHandle["token"]

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun onNewPasswordChange(p: String) { _newPassword.value = p; _error.value = null }
    fun onConfirmPasswordChange(p: String) { _confirmPassword.value = p; _error.value = null }

    fun resetPassword() {
        if (_newPassword.value.length < 6) {
            _error.value = "Şifre en az 6 karakter olmalıdır."
            return
        }
        if (_newPassword.value != _confirmPassword.value) {
            _error.value = "Şifreler birbiriyle eşleşmiyor."
            return
        }
        if (token.isNullOrBlank()) {
            _error.value = "Geçersiz veya süresi dolmuş bağlantı."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authUseCases.resetPassword(token, _newPassword.value)

            result.fold(
                onSuccess = { _isSuccess.value = true },
                onFailure = { _error.value = it.message ?: "Şifre güncellenemedi." }
            )
            _isLoading.value = false
        }
    }
}