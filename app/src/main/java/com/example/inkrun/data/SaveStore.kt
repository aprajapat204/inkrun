package com.example.inkrun.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.rxjava3.preferencesDataStore
import com.example.inkrun.core.Constants
import io.reactivex.rxjava3.core.Flowable

class SaveStore(private val context: Context) {

    private val Context.dataStore by preferencesDataStore("inkrun_save")

    // Keys
    private val LEVEL_KEY = stringPreferencesKey(Constants.SAVE_KEY_LEVEL)
    private val SOUND_KEY = booleanPreferencesKey(Constants.SAVE_KEY_SOUND_ENABLED)
    private val HAPTICS_KEY = booleanPreferencesKey(Constants.SAVE_KEY_HAPTICS_ENABLED)
    private val CONSENT_KEY = booleanPreferencesKey(Constants.SAVE_KEY_AD_CONSENT)
    private val HIGH_SCORE_KEY = intPreferencesKey("high_score")

    // Getters (synchronous for simple access, but prefer Flow)
    suspend fun getCurrentLevel(): String {
        return context.dataStore.data.first().get(LEVEL_KEY) ?: "w01/l01_s1"
    }

    suspend fun setCurrentLevel(level: String) {
        context.dataStore.edit { it[LEVEL_KEY] = level }
    }

    suspend fun getSoundEnabled(): Boolean {
        return context.dataStore.data.first().get(SOUND_KEY) ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_KEY] = enabled }
    }

    suspend fun getHapticsEnabled(): Boolean {
        return context.dataStore.data.first().get(HAPTICS_KEY) ?: true
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_KEY] = enabled }
    }

    suspend fun getAdConsent(): Boolean {
        return context.dataStore.data.first().get(CONSENT_KEY) ?: false
    }

    suspend fun setAdConsent(given: Boolean) {
        context.dataStore.edit { it[CONSENT_KEY] = given }
    }

    suspend fun getHighScore(): Int {
        return context.dataStore.data.first().get(HIGH_SCORE_KEY) ?: 0
    }

    suspend fun setHighScore(score: Int) {
        context.dataStore.edit { it[HIGH_SCORE_KEY] = score }
    }

    // Reactive getters for UI observation
    fun observeCurrentLevel(): Flowable<String> {
        return context.dataStore.data
            .map { prefs -> prefs[LEVEL_KEY] ?: "w01/l01_s1" }
            .distinctUntilChanged()
    }

    fun observeSoundEnabled(): Flowable<Boolean> {
        return context.dataStore.data
            .map { prefs -> prefs[SOUND_KEY] ?: true }
            .distinctUntilChanged()
    }

    fun observeHapticsEnabled(): Flowable<Boolean> {
        return context.dataStore.data
            .map { prefs -> prefs[HAPTICS_KEY] ?: true }
            .distinctUntilChanged()
    }
}