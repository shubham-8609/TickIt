package com.codeleg.tickit.database.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codeleg.tickit.utils.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ThemePreferences {

    private val Context.dataStore by preferencesDataStore("theme_prefs")

    private val THEME_MODE = stringPreferencesKey("theme_mode")

    suspend fun setTheme(context: Context, mode: ThemeMode) {
        context.dataStore.edit {
            it[THEME_MODE] = mode.name
        }
    }

    fun getTheme(context: Context): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            ThemeMode.valueOf(
                prefs[THEME_MODE] ?: ThemeMode.SYSTEM.name
            )
        }
    }
}
