# Add project specific ProGuard rules here.

# Keep all model classes for Firebase and Serialization
-keep class com.bravem.app.model.** { *; }

# Keep Room generated classes
-keep class com.bravem.app.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Dao

# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# General attributes
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
