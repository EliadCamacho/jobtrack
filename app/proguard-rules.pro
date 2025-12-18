# JobTrack ProGuard rules

# Keep Kotlin metadata & serialization
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod,Signature
-keepclassmembers class kotlin.Metadata { *; }

# Keep kotlinx.serialization generated stuff
-keepclassmembers class ** {
    *** Companion;
}
-keepclassmembers class **$Companion {
    ** serializer(...);
}
-if @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class <1> {
    static <1> serializer(...);
}

# Keep Room generated classes (usually safe even without, but helps)
-keep class androidx.room.RoomDatabase { *; }
-dontwarn javax.annotation.**
