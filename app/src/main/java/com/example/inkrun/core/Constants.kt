package com.example.inkrun.core

object Constants {
    // Screen & timing
    const val TARGET_FPS = 60
    const val FIXED_TIME_STEP = 1.0 / TARGET_FPS
    const val MAX_FRAME_SKIP = 5

    // World units (meters)
    const val WORLD_WIDTH = 40f
    const val WORLD_HEIGHT = 22.5f

    // Player physics
    const val PLAYER_WIDTH = 1.0f
    const val PLAYER_HEIGHT = 1.8f
    const val PLAYER_SPEED = 8f
    const val PLAYER_JUMP_VELOCITY = 12f
    const val PLAYER_GRAVITY = 25f
    const val PLAYER_MAX_FALL_SPEED = 20f
    const val PLAYER_COYOTE_TIME = 0.1f
    const val PLAYER_JUMP_BUFFER = 0.1f

    // Camera
    const val CAMERA_LERP = 10f
    const val CAMERA_DEADZONE_X = 2f
    const val CAMERA_DEADZONE_Y = 1.5f

    // Touch controls
    const val BUTTON_SIZE_DP = 72f
    const val BUTTON_MARGIN_DP = 16f
    const val BUTTON_ALPHA = 0.7f
    const val BUTTON_PRESSED_ALPHA = 1.0f

    // Level
    const val TILE_SIZE = 1f
    const val LEVEL_PARALLAX_SPEED = 0.5f

    // Audio
    const val SAMPLE_RATE = 44100
    const val TONE_DURATION_MS = 100
    const val MASTER_VOLUME = 0.5f

    // Ads
    const val REWARDED_AD_COOLDOWN_MS = 30000
    const val AD_LOAD_TIMEOUT_MS = 10000

    // Test AdMob IDs (replace with real IDs for production)
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Save keys
    const val SAVE_KEY_LEVEL = "current_level"
    const val SAVE_KEY_SOUND_ENABLED = "sound_enabled"
    const val SAVE_KEY_HAPTICS_ENABLED = "haptics_enabled"
    const val SAVE_KEY_AD_CONSENT = "ad_consent"
}