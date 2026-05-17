package com.neilb.synapcart.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.domain.use_case.auth.AuthUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authUseCases: AuthUseCases
) : ViewModel() {

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isRegisterSuccess = MutableStateFlow(false)
    val isRegisterSuccess: StateFlow<Boolean> = _isRegisterSuccess.asStateFlow()

    fun onFullNameChange(name: String) {
        _fullName.value = name
        _error.value = null
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _error.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _error.value = null
    }

    fun register() {
        if (_fullName.value.isBlank() || _email.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Lütfen tüm alanları eksiksiz doldurun."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = authUseCases.register(_email.value, _password.value, _fullName.value)

            result.fold(
                onSuccess = {
                    _isRegisterSuccess.value = true
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Kayıt sırasında bir hata oluştu."
                }
            )

            _isLoading.value = false
        }
    }

    fun resetSuccessState() {
        _isRegisterSuccess.value = false
    }
}