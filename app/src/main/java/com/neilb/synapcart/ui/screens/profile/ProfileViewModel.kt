package com.neilb.synapcart.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.AppLocale
import com.neilb.synapcart.util.SnackbarController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userUseCases: UserUseCases,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _language = MutableStateFlow("tr")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _currency = MutableStateFlow("TRY")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    /** Raw server timestamp of the premium expiry, if any. */
    private val _planExpiresAt = MutableStateFlow<String?>(null)
    val planExpiresAt: StateFlow<String?> = _planExpiresAt.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAccountDeleted = MutableStateFlow(false)
    val isAccountDeleted: StateFlow<Boolean> = _isAccountDeleted.asStateFlow()

    /** True once a password change succeeded (the dialog closes). */
    private val _passwordChanged = MutableStateFlow(false)
    val passwordChanged: StateFlow<Boolean> = _passwordChanged.asStateFlow()

    init {
        fetchProfileData()
    }

    private fun fetchProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            userUseCases.getUser().fold(
                onSuccess = { profile ->
                    _fullName.value = profile.fullName.orEmpty()
                    _email.value = profile.email
                    _language.value = AppLocale.normalize(profile.language ?: AppLocale.current())
                    _currency.value = profile.currency ?: "TRY"
                    _isPremium.value = profile.isPremium
                    _planExpiresAt.value = profile.planExpiresAt
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_profile_load)) }
            )
            _isLoading.value = false
        }
    }

    fun onFullNameChange(name: String) { _fullName.value = name }
    fun onLanguageChange(lang: String) { _language.value = lang }
    fun onCurrencyChange(curr: String) { _currency.value = curr }

    fun updateProfile() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            userUseCases.updateProfile(
                fullName = _fullName.value.ifBlank { null },
                language = _language.value,
                currency = _currency.value
            ).fold(
                onSuccess = { snackbarController.showSnackbar(R.string.profile_saved) },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_profile_update)) }
            )
            _isLoading.value = false
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            userUseCases.changePassword(currentPassword, newPassword).fold(
                onSuccess = {
                    _passwordChanged.value = true
                    snackbarController.showSnackbar(R.string.password_changed)
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_password_update_failed)) }
            )
            _isLoading.value = false
        }
    }

    fun onPasswordChangeHandled() {
        _passwordChanged.value = false
    }

    fun deleteAccount(password: String) {
        if (_isLoading.value) return
        viewModelScope.launch {
            if (password.isBlank()) {
                snackbarController.showSnackbar(R.string.error_delete_needs_password)
                return@launch
            }
            _isLoading.value = true
            userUseCases.deleteAccount(password).fold(
                onSuccess = {
                    snackbarController.showSnackbar(R.string.account_deleted)
                    _isAccountDeleted.value = true
                },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_account_delete)) }
            )
            _isLoading.value = false
        }
    }
}
