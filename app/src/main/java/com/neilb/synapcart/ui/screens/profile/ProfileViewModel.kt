package com.neilb.synapcart.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.domain.use_case.user.UserUseCases
import com.neilb.synapcart.util.SessionManager
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
    private val sessionManager: SessionManager,
    private val snackbarController: SnackbarController
) : ViewModel() {

    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    private val _language = MutableStateFlow("tr")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _currency = MutableStateFlow("TRY")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAccountDeleted = MutableStateFlow(false)
    val isAccountDeleted: StateFlow<Boolean> = _isAccountDeleted.asStateFlow()

    init {
        fetchProfileData()
    }

    private fun fetchProfileData() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = userUseCases.getUser()

            result.fold(
                onSuccess = { profileData ->
                    _fullName.value = profileData.fullName ?: ""
                    _language.value = profileData.language ?: "tr"
                    _currency.value = profileData.currency ?: "TRY"
                },
                onFailure = {
                    snackbarController.showSnackbar("Profil bilgileri alınamadı.")
                }
            )
            _isLoading.value = false
        }
    }

    fun onFullNameChange(name: String) { _fullName.value = name }
    fun onLanguageChange(lang: String) { _language.value = lang }
    fun onCurrencyChange(curr: String) { _currency.value = curr }

    fun updateProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = userUseCases.updateProfile(
                fullName = _fullName.value.ifBlank { null },
                language = _language.value,
                currency = _currency.value
            )

            result.fold(
                onSuccess = {
                    snackbarController.showSnackbar("Profil başarıyla güncellendi!")
                    sessionManager.saveUserName(_fullName.value)
                },
                onFailure = {
                    snackbarController.showSnackbar("Güncelleme başarısız oldu.")
                }
            )

            _isLoading.value = false
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = userUseCases.deleteAccount()

            result.fold(
                onSuccess = {
                    sessionManager.clearSession()
                    snackbarController.showSnackbar("Hesabınız kalıcı olarak silindi.")
                    _isAccountDeleted.value = true
                },
                onFailure = {
                    snackbarController.showSnackbar("Hesap silinirken bir hata oluştu.")
                }
            )

            _isLoading.value = false
        }
    }
}