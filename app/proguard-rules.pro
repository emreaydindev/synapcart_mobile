# --- SynapCart release rules ---
# kotlinx.serialization, Ktor and DataStore ship their own consumer rules. The DTOs in the
# shared module use generated serializers (no reflection), so no keep rules are needed here.

# Keep line numbers for crash reports, hide source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Ktor references JVM classes that don't exist on Android.
-dontwarn java.lang.management.**
-dontwarn org.slf4j.**
