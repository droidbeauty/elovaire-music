# A separate instrumentation APK needs APIs that production code does not call.
-dontshrink
-keep class androidx.tracing.** { *; }
# Unused desktop paths remain present when shrinking is disabled for instrumentation.
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn javax.naming.**
-dontwarn javax.swing.**
