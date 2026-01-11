############################################################
# KeymapKit - R8/ProGuard rules (minimal, safe)
############################################################

# Keep Kotlin metadata used by reflection in some libraries.
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Compose: keep source info optional (R8 removes by default in release)
# No special keep rules required for pure Compose UI apps.

# If you ever use @Keep annotations, keep them:
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Avoid warnings for optional tooling / preview (not packaged in release)
-dontwarn androidx.compose.ui.tooling.**
-dontwarn kotlin.Metadata