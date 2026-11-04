package com.neilb.synapcart.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

/** File name of the preferences store; Android keeps it at `files/datastore/<name>` as before. */
const val SESSION_DATASTORE_FILE = "synapcart_prefs.preferences_pb"

/**
 * Creates the session DataStore at [path] (absolute). Create exactly one per process:
 * Android passes `filesDir/datastore/synapcart_prefs.preferences_pb`, iOS a file in the
 * app's Documents directory (see `IosPlatform`).
 */
fun createSessionDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })

/** Session and device preferences: JWT, user name, language, currency, plan and onboarding flags. */
class SessionManager(private val dataStore: DataStore<Preferences>) {

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
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
            if (!userName.isNullOrBlank()) preferences[USER_NAME_KEY] = userName
        }
    }

    suspend fun saveOnboardingCompleted(isCompleted: Boolean) {
        dataStore.edit { preferences -> preferences[ONBOARDING_COMPLETED_KEY] = isCompleted }
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_COMPLETED_KEY] ?: false }

    val authToken: Flow<String?> = dataStore.data.map { it[ACCESS_TOKEN_KEY] }

    val userName: Flow<String?> = dataStore.data.map { it[USER_NAME_KEY] }

    /** The user's display currency (ISO code); products are searched in this currency. */
    val currency: Flow<String> = dataStore.data.map { it[CURRENCY_KEY] ?: DEFAULT_CURRENCY }

    val language: Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE }

    /** True when the cached plan (from `/user/me`) is premium. */
    val isPremium: Flow<Boolean> = dataStore.data.map { it[PLAN_KEY] == "premium" }

    suspend fun savePlan(plan: String?) {
        dataStore.edit { preferences ->
            if (plan.isNullOrBlank()) preferences.remove(PLAN_KEY) else preferences[PLAN_KEY] = plan
        }
    }

    /** Device-level: the notification permission prompt was already shown once. */
    val notificationPromptShown: Flow<Boolean> = dataStore.data.map { it[NOTIFICATION_PROMPT_KEY] ?: false }

    suspend fun setNotificationPromptShown() {
        dataStore.edit { preferences -> preferences[NOTIFICATION_PROMPT_KEY] = true }
    }

    suspend fun saveUserName(userName: String) {
        dataStore.edit { preferences -> preferences[USER_NAME_KEY] = userName }
    }

    suspend fun savePreferences(language: String?, currency: String?) {
        dataStore.edit { preferences ->
            if (!language.isNullOrBlank()) preferences[LANGUAGE_KEY] = language
            if (!currency.isNullOrBlank()) preferences[CURRENCY_KEY] = currency
        }
    }

    /**
     * Signs the user out. Device-level settings (onboarding done, language) survive so a
     * returning user isn't sent through onboarding again.
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(USER_NAME_KEY)
            preferences.remove(CURRENCY_KEY)
            preferences.remove(PLAN_KEY)
        }
    }
}

/**
 * App-wide session signals. [expired] fires when the backend rejects the stored token
 * (401 on an authenticated request); the apps listen and return to the login screen.
 */
class SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}
