# Tuneora ProGuard Rules

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    long PASS_MS;
}

# --- Dagger Hilt ---
-keep class com.maheswara660.tuneora.** { *; }
-keep class dagger.hilt.internal.GeneratedComponentManagerHolder { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * implements dagger.hilt.internal.GeneratedComponentManagerHolder { *; }
-keep class * implements dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper$OnContextAvailableListener { *; }

# --- Room ---
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.RoomDatabase
-keep class * implements androidx.room.RoomOpenHelper
-dontwarn androidx.room.paging.**

# --- Media3 / ExoPlayer ---
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.extractor.** { *; }
-dontwarn androidx.media3.**

# --- Coil 3 ---
-keep class io.coilkt.coil3.** { *; }
-dontwarn io.coilkt.coil3.**

# --- Timber ---
-keep class timber.log.** { *; }
-dontwarn timber.log.**

# --- Generic Android ---
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod

# Preserve generated files
-keep class com.maheswara660.tuneora.BuildConfig { *; }