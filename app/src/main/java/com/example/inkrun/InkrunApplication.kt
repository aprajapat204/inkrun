package com.example.inkrun

import android.app.Application
import com.example.inkrun.ads.ConsentManager
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform

class InkrunApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize UMP for consent management
        val consentInfo = UserMessagingPlatform.getConsentInformation(this)

        // Check if consent is needed
        if (!consentInfo.isConsentFormAvailable) {
            // Request consent update on first launch
            ConsentManager(this).requestConsentUpdate(
                // We'll handle this in MainActivity after UI is ready
                object : android.app.Activity() {} // Placeholder, will be replaced
            ) { }
        }
    }
}