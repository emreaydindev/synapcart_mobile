package com.neilb.synapcart.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "synapcart_prefs")

class SessionManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val CURRENCY_KEY = stringPreferencesKey("currency")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val PLAN_KEY = stringPreferencesKey("plan")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val NOTIFICATION_PROMPT_KEY = booleanPreferencesKey("notification_prompt_shown")

        const val DEFAULT_CURRENCY = "TRY"
        const val DEFAULT_LANGUAGE = "tr"
    }

    suspend fun saveAuthToken(token: String, userName: String?) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
            if (!userName.isNullOrBlank()) preferences[USER_NAME_KEY] = userName
        }
    }

    suspend fun saveOnboardingCompleted(isCompleted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = isCompleted
        }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED_KEY] ?: false
    }

    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    /** The user's display currency (ISO code); products are searched in this currency. */
    val currency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CURRENCY_KEY] ?: DEFAULT_CURRENCY
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
    }

    /** True when the cached plan (from `/user/me`) is premium. */
    val isPremium: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PLAN_KEY] == "premium"
    }

    suspend fun savePlan(plan: String?) {
        context.dataStore.edit { preferences ->
            if (plan.isNullOrBlank()) preferences.remove(PLAN_KEY) else preferences[PLAN_KEY] = plan
        }
    }

    /** Device-level: the notification permission prompt was already shown once. */
    val notificationPromptShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_PROMPT_KEY] ?: false
    }

    suspend fun setNotificationPromptShown() {
        context.dataStore.edit { preferences -> preferences[NOTIFICATION_PROMPT_KEY] = true }
    }

    suspend fun saveUserName(userName: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = userName
        }
    }

    suspend fun savePreferences(language: String?, currency: String?) {
        context.dataStore.edit { preferences ->
            if (!language.isNullOrBlank()) preferences[LANGUAGE_KEY] = language
            if (!currency.isNullOrBlank()) preferences[CURRENCY_KEY] = currency
        }
    }

    /**
     * Signs the user out. Device-level settings (onboarding done, language) survive so a
     * returning user isn't sent through onboarding again.
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(CURRENCY_KEY)
            preferences.remove(PLAN_KEY)
        }
    }
}
