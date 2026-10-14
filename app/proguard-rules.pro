# ProGuard rules for Inkrun

# Keep the application class
-keep class com.example.inkrun.InkrunApplication { *; }

# Keep the main activity
-keep class com.example.inkrun.MainActivity { *; }

# Keep all enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep DataStore preferences keys
-keep class androidx.datastore.preferences.core.PreferencesKeys { *; }

# Keep AdMob classes
-keep class com.google.android.gms.ads.** { *; }

# Keep UMP classes
-keep class com.google.android.ump.** { *; }

# Keep RxJava
-keep class io.reactivex.** { *; }
-dontwarn io.reactivex.**

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }