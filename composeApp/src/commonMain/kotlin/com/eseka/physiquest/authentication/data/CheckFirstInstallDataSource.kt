package com.eseka.physiquest.authentication.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import co.touchlab.kermit.Logger
import com.eseka.physiquest.authentication.domain.CheckFirstInstallUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CheckFirstInstallDataSource(private val prefs: DataStore<Preferences>) :
    CheckFirstInstallUseCase {
    private companion object {
        private const val TAG = "CheckFirstInstallDataSource"

        private object PreferencesKeys {
            val FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
            val ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        }
    }

    override suspend fun invoke(): Flow<Boolean> = prefs.data
        .catch { exception ->
            Logger.e(tag = TAG, message = { "Error reading preferences" }, throwable = exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val isFirstLaunch = preferences[PreferencesKeys.FIRST_LAUNCH] != false
            val isOnboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] == true

            isFirstLaunch || !isOnboardingCompleted
        }

    override suspend fun markOnboardingComplete() {
        prefs.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = false
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun resetFirstLaunchState() {
        prefs.edit { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH] = true
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = false
        }
    }
}