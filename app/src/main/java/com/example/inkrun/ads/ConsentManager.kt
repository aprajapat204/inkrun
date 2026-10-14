package com.example.inkrun.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class ConsentManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("inkrun_consent", Context.MODE_PRIVATE)
    private var consentForm: ConsentForm? = null
    private var consentInfo: ConsentInformation? = null

    interface ConsentCallback {
        fun onConsentObtained()
        fun onConsentFailed(error: String)
    }

    fun requestConsentUpdate(activity: Activity, callback: ConsentCallback) {
        val debugSettings = ConsentDebugSettings.Builder()
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("TEST_DEVICE_ID") // Add your test device ID
            .build()

        val params = ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInfo = UserMessagingPlatform.getConsentInformation(context)

        consentInfo?.requestConsentInfoUpdate(params) { error ->
            if (error != null) {
                Log.e("ConsentManager", "Consent info update failed: $error")
                callback.onConsentFailed(error.message)
                return@requestConsentInfoUpdate
            }

            loadForm(activity, callback)
        }
    }

    private fun loadForm(activity: Activity, callback: ConsentCallback) {
        UserMessagingPlatform.loadConsentForm(activity) { form, error ->
            if (error != null) {
                Log.e("ConsentManager", "Consent form load failed: $error")
                callback.onConsentFailed(error.message)
                return@loadConsentForm
            }

            consentForm = form

            if (consentInfo?.isConsentFormAvailable == true) {
                showForm(activity, callback)
            } else {
                // Consent not required
                saveConsentStatus(true)
                callback.onConsentObtained()
            }
        }
    }

    private fun showForm(activity: Activity, callback: ConsentCallback) {
        consentForm?.show(activity) { error ->
            if (error != null) {
                Log.e("ConsentManager", "Consent form show failed: $error")
                callback.onConsentFailed(error.message)
            } else {
                // Consent obtained
                saveConsentStatus(true)
                callback.onConsentObtained()
            }

            consentForm = null
        }
    }

    fun resetConsent() {
        consentInfo?.reset()
        saveConsentStatus(false)
    }

    fun hasConsent(): Boolean {
        return prefs.getBoolean("consent_given", false)
    }

    private fun saveConsentStatus(given: Boolean) {
        prefs.edit().putBoolean("consent_given", given).apply()
    }

    fun canRequestAds(): Boolean {
        val consentInfo = UserMessagingPlatform.getConsentInformation(context)
        return consentInfo.canRequestAds()
    }
}