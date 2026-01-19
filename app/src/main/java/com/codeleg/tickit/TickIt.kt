package com.codeleg.tickit

import android.app.Application
import com.codeleg.tickit.database.local.ThemeKeys
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.FirebaseDatabase

class TickIt : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val enabled = getSharedPreferences(
            ThemeKeys.PREF_FILE,
            MODE_PRIVATE
        ).getBoolean(
            ThemeKeys.DYNAMIC_COLORS,
            true
        )

        if (enabled) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}

