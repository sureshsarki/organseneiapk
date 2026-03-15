# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep all classes in our package
-keep class com.organsensei.earsensei.** { *; }

# Keep database helper
-keepclassmembers class com.organsensei.earsensei.DatabaseHelper {
    public <init>(...);
}

# Keep service
-keepclassmembers class com.organsensei.earsensei.VolumeMonitorService {
    public <init>(...);
}
