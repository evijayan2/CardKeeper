package com.vijay.cardkeeper.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val USER_PREFERENCES_NAME = "user_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "LIGHT", "DARK", "SYSTEM"
        val DATE_FORMAT = stringPreferencesKey("date_format") // "DEFAULT", "SYSTEM", "CUSTOM"
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val REMINDER_1_DAYS = intPreferencesKey("reminder_1_days")
        val REMINDER_2_DAYS = intPreferencesKey("reminder_2_days")
        val REMINDER_3_DAYS = intPreferencesKey("reminder_3_days")
    }

    val themeMode: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
        }

    val dateFormat: Flow<String> = context.dataStore.data
        .catch { exception ->
             if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT] ?: "DEFAULT"
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
             if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    val reminder1Days: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.REMINDER_1_DAYS] ?: 21
        }

    val reminder2Days: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.REMINDER_2_DAYS] ?: 11
        }

    val reminder3Days: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.REMINDER_3_DAYS] ?: 1
        }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun saveDateFormat(format: String) {
         context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT] = format
        }
    }
    
    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }
    }

    suspend fun saveReminder1Days(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_1_DAYS] = days
        }
    }

    suspend fun saveReminder2Days(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_2_DAYS] = days
        }
    }

    suspend fun saveReminder3Days(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_3_DAYS] = days
        }
    }
}
