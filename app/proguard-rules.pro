# --- SynapCart release rules ---

# Gson DTOs: field names map to backend JSON through @SerializedName, and Gson
# instantiates these classes reflectively, so keep the classes and their fields.
-keep class com.neilb.synapcart.data.model.** { *; }
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod

# Gson TypeToken generics
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Retrofit suspend functions and generic return types
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep line numbers for crash reports, hide source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
