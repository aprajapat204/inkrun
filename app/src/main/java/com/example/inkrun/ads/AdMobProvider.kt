package com.example.inkrun.ads

import android.app.Activity
import android.util.Log
import com.example.inkrun.core.Constants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobProvider(private val activity: Activity) : AdProvider {

    private var rewardedAd: RewardedAd? = null
    private var loadCallback: ((Boolean) -> Unit)? = null
    private var showCallback: ((Boolean) -> Unit)? = null
    private var initialized = false

    override fun initialize() {
        if (initialized) return

        MobileAds.initialize(activity) { status ->
            Log.d("AdMobProvider", "MobileAds initialized: ${status.adapterStatuses.size} adapters")
        }
        initialized = true
        loadRewardedAd()
    }

    override fun loadRewardedAd() {
        if (!initialized) {
            initialize()
        }

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            activity,
            Constants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            loadRewardedAd() // Preload next ad
                            showCallback?.invoke(true)
                            showCallback = null
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e("AdMobProvider", "Failed to show rewarded ad: $error")
                            rewardedAd = null
                            loadRewardedAd()
                            showCallback?.invoke(false)
                            showCallback = null
                        }
                    }
                    loadCallback?.invoke(true)
                    loadCallback = null
                    Log.d("AdMobProvider", "Rewarded ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdMobProvider", "Failed to load rewarded ad: $error")
                    rewardedAd = null
                    loadCallback?.invoke(false)
                    loadCallback = null
                }
            }
        )
    }

    override fun showRewardedAd(onComplete: (Boolean) -> Unit) {
        showCallback = onComplete

        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem: RewardItem ->
                Log.d("AdMobProvider", "Rewarded ad completed, reward: ${rewardItem.amount} ${rewardItem.type}")
                // Callback will be invoked in onAdDismissedFullScreenContent
            }
        } else {
            Log.w("AdMobProvider", "Rewarded ad not ready")
            onComplete(false)
            showCallback = null
            loadRewardedAd()
        }
    }

    override fun isRewardedAdReady(): Boolean {
        return rewardedAd != null
    }

    override fun destroy() {
        rewardedAd = null
        loadCallback = null
        showCallback = null
    }
}