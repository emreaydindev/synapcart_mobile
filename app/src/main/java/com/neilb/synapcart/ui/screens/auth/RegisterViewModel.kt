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
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRegisterSuccess = MutableStateFlow(false)
    val isRegisterSuccess: StateFlow<Boolean> = _isRegisterSuccess.asStateFlow()

    fun onFullNameChange(name: String) {
        _fullName.value = name
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun register() {
        viewModelScope.launch {
            if (_fullName.value.isBlank() || _email.value.isBlank() || _password.value.isBlank()) {
                snackbarController.showSnackbar("Lütfen tüm alanları eksiksiz doldurun.")
                return@launch
            }

            _isLoading.value = true

            val registerResult = authUseCases.register(_fullName.value, _email.value, _password.value)

            registerResult.fold(
                onSuccess = {
                    val loginResult = authUseCases.login(_email.value, _password.value)

                    loginResult.fold(
                        onSuccess = {
                            _isRegisterSuccess.value = true
                        },
                        onFailure = {
                            snackbarController.showSnackbar("Kayıt başarılı ancak giriş yapılamadı. Lütfen manuel giriş yapın.")
                        }
                    )
                },
                onFailure = { exception ->
                    snackbarController.showSnackbar(exception.message ?: "Kayıt sırasında bir hata oluştu.")
                }
            )

            _isLoading.value = false
        }
    }

    fun resetSuccessState() {
        _isRegisterSuccess.value = false
    }
}