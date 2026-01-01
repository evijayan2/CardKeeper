# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/vijay/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Retrofit / OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Coroutines
-dontwarn kotlinx.coroutines.**

# Compose
-keep class androidx.compose.** { *; }

# Serialization (if used later)
-keepattributes EnclosingMethod

# Room
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Data Classes (Preserve for potential reflection/serialization)
-keep class com.vijay.cardkeeper.data.entity.** { *; }
-keep class com.vijay.cardkeeper.data.model.** { *; }

# Navigation (if using nav args)
-keepnames class * extends androidx.navigation.Navigator
-keepnames class * extends androidx.lifecycle.ViewModel 

# Bouncy Castle (Crypto)
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }

# SQLCipher
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.sqlcipher.**
