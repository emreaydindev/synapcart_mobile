package com.neilb.synapcart.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.domain.use_case.user.UserUseCases
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
                    _language.value = profile.language ?: "tr"
                    _currency.value = profile.currency ?: "TRY"
                },
                onFailure = { e -> snackbarController.showSnackbar(e.message ?: "Profil bilgileri alınamadı.") }
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
                onSuccess = { snackbarController.showSnackbar("Profil başarıyla güncellendi.") },
                onFailure = { e -> snackbarController.showSnackbar(e.message ?: "Güncelleme başarısız oldu.") }
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
                    snackbarController.showSnackbar("Şifreniz güncellendi. Diğer cihazlardaki oturumlar kapatıldı.")
                },
                onFailure = { e -> snackbarController.showSnackbar(e.message ?: "Şifre güncellenemedi.") }
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
                snackbarController.showSnackbar("Hesabınızı silmek için şifrenizi girin.")
                return@launch
            }
            _isLoading.value = true
            userUseCases.deleteAccount(password).fold(
                onSuccess = {
                    snackbarController.showSnackbar("Hesabınız kalıcı olarak silindi.")
                    _isAccountDeleted.value = true
                },
                onFailure = { e -> snackbarController.showSnackbar(e.message ?: "Hesap silinirken bir hata oluştu.") }
            )
            _isLoading.value = false
        }
    }
}
