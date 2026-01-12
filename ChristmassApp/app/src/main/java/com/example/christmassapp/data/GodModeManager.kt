package com.example.christmassapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "god_settings")

class GodModeManager(private val context: Context) {
    companion object {
        val APOCALYPSE_MODE = booleanPreferencesKey("apocalypse_mode")
    }

    val apocalypseFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[APOCALYPSE_MODE] ?: false }

    suspend fun setApocalypseMode(enabled: Boolean) {
        context.dataStore.edit { it[APOCALYPSE_MODE] = enabled }
    }
}