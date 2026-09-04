package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    
    val threshold: Flow<Int> = dataStore.data.map { preferences ->
        preferences[THRESHOLD_KEY] ?: 60
    }
    
    val voiceEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VOICE_KEY] ?: true
    }
    
    val bodyEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BODY_KEY] ?: true
    }

    suspend fun setThreshold(value: Int) {
        dataStore.edit { preferences -> preferences[THRESHOLD_KEY] = value }
    }

    suspend fun setVoiceEnabled(value: Boolean) {
        dataStore.edit { preferences -> preferences[VOICE_KEY] = value }
    }

    suspend fun setBodyEnabled(value: Boolean) {
        dataStore.edit { preferences -> preferences[BODY_KEY] = value }
    }

    companion object {
        val THRESHOLD_KEY = intPreferencesKey("threshold")
        val VOICE_KEY = booleanPreferencesKey("voice_enabled")
        val BODY_KEY = booleanPreferencesKey("body_enabled")
    }
}
