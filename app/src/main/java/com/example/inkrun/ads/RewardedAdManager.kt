package com.example.inkrun.ads

import android.util.Log

class RewardedAdManager(private val adProvider: AdProvider) {

    private var state = State.IDLE
    private var pendingReward: ((Boolean) -> Unit)? = null
    private var lastShowTime = 0L

    enum class State {
        IDLE, LOADING, READY, SHOWING, COOLDOWN
    }

    fun initialize() {
        adProvider.initialize()
        state = State.LOADING
    }

    fun requestAd() {
        when (state) {
            State.IDLE, State.COOLDOWN -> {
                state = State.LOADING
                adProvider.loadRewardedAd()
            }
            State.LOADING -> {
                // Already loading
            }
            State.READY -> {
                // Already ready
            }
            State.SHOWING -> {
                // Currently showing
            }
        }
    }

    fun onAdLoaded(success: Boolean) {
        when (state) {
            State.LOADING -> {
                if (success) {
                    state = State.READY
                    Log.d("RewardedAdManager", "Ad ready")
                } else {
                    state = State.IDLE
                    Log.d("RewardedAdManager", "Ad load failed")
                }
            }
            else -> {
                // Ignore stale callbacks
            }
        }
    }

    fun showAd(onReward: (Boolean) -> Unit): Boolean {
        when (state) {
            State.READY -> {
                val now = System.currentTimeMillis()
                if (now - lastShowTime < 3000) { // Prevent rapid shows
                    onReward(false)
                    return false
                }

                state = State.SHOWING
                pendingReward = onReward
                adProvider.showRewardedAd { success ->
                    handleShowComplete(success)
                }
                return true
            }
            State.LOADING -> {
                // Not ready yet, queue the request
                pendingReward = onReward
                return false
            }
            else -> {
                onReward(false)
                return false
            }
        }
    }

    private fun handleShowComplete(success: Boolean) {
        lastShowTime = System.currentTimeMillis()
        state = State.COOLDOWN

        pendingReward?.invoke(success)
        pendingReward = null

        // Start cooldown and reload
        // In a real app, you'd use a Handler or Coroutine for this
        // For simplicity, we'll just reload immediately
        adProvider.loadRewardedAd()
    }

    fun onAdDismissed() {
        // Called when ad is dismissed (handled by provider callback)
    }

    fun isReady(): Boolean = state == State.READY
    fun getState(): State = state

    fun destroy() {
        adProvider.destroy()
        state = State.IDLE
        pendingReward = null
    }
}