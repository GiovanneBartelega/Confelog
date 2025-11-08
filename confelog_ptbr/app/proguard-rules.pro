# Add project specific ProGuard rules here.
# You can find general rules for common libraries at
# https://www.guardsquare.com/manual/configuration/project

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.flow.** {
    *;
}
-keep class kotlin.coroutines.Continuation {
    *;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers,allowobfuscation class * extends androidx.room.RoomDatabase {
    <init>(...);
    public static **.Builder builder(...);
    public static ** getInstance(...);
}
-keep class * extends androidx.room.TypeConverter
-keepclassmembers,allowobfuscation class * extends androidx.room.TypeConverter {
    <init>(...);
}
-keep,allowobfuscation @androidx.room.Entity class *
-keep,allowobfuscation @androidx.room.Dao interface *
-keep,allowobfuscation @androidx.room.Database class *
