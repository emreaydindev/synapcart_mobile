package com.neilb.synapcart.ui.screens.auth

import com.neilb.synapcart.R
import com.neilb.synapcart.data.remote.toUiText
import androidx.lifecycle.SavedStateHandle
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
class ResetPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    savedStateHandle: SavedStateHandle,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val token: String? = savedStateHandle["token"]

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun onNewPasswordChange(p: String) { _newPassword.value = p }
    fun onConfirmPasswordChange(p: String) { _confirmPassword.value = p }

    fun resetPassword() {
        viewModelScope.launch {
            if (_newPassword.value != _confirmPassword.value) {
                snackbarController.showSnackbar(R.string.error_passwords_mismatch)
                return@launch
            }
            if (token.isNullOrBlank()) {
                snackbarController.showSnackbar(R.string.error_reset_link_invalid)
                return@launch
            }
            _isLoading.value = true

            val result = authUseCases.resetPassword(token, _newPassword.value)

            result.fold(
                onSuccess = { _isSuccess.value = true },
                onFailure = {
                    snackbarController.showSnackbar(it.toUiText(R.string.error_password_update_failed))
                }
            )
            _isLoading.value = false
        }
    }
}