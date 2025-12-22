package com.codeleg.tickit

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.FirebaseDatabase

class TickIt: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        DynamicColors.applyToActivitiesIfAvailable(this)
        
    }
}