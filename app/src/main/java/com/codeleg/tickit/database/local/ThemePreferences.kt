package com.codeleg.tickit.database.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.codeleg.tickit.utils.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ThemePreferences {

    private val Context.dataStore by preferencesDataStore("theme_prefs")

    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val DYNAMIC_COLORS =
        booleanPreferencesKey(ThemeKeys.DYNAMIC_COLORS)

    suspend fun setTheme(context: Context, mode: ThemeMode) {
        context.dataStore.edit {
            it[THEME_MODE] = mode.name
        }
    }

    fun getTheme(context: Context): Flow<ThemeMode> =
        context.dataStore.data.map {
            ThemeMode.valueOf(
                it[THEME_MODE] ?: ThemeMode.SYSTEM.name
            )
        }

    /** ✅ SINGLE place to sync both */
    suspend fun setDynamicColors(context: Context, enabled: Boolean) {

        // DataStore (UI & reactive)
        context.dataStore.edit {
            it[DYNAMIC_COLORS] = enabled
        }

        // SharedPreferences (Application startup)
        context.getSharedPreferences(
            ThemeKeys.PREF_FILE,
            Context.MODE_PRIVATE
        ).edit()
            .putBoolean(ThemeKeys.DYNAMIC_COLORS, enabled)
            .apply()
    }

    fun isDynamicColorsEnabled(context: Context): Flow<Boolean> =
        context.dataStore.data.map {
            it[DYNAMIC_COLORS] ?: true
        }
}



object ThemeKeys {
    const val PREF_FILE = "theme_prefs"
    const val DYNAMIC_COLORS = "dynamic_colors"
}

