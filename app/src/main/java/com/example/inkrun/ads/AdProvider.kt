package com.example.inkrun.ads

interface AdProvider {
    fun initialize()
    fun loadRewardedAd()
    fun showRewardedAd(onComplete: (Boolean) -> Unit)
    fun isRewardedAdReady(): Boolean
    fun destroy()
}