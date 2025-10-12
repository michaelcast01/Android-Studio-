# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========= OPTIMIZACIONES AVANZADAS =========

# Mantener las clases necesarias para Retrofit y serialización
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.example.tiendasuplementacion.model.** { *; }
-keep class com.example.tiendasuplementacion.interfaces.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Optimizaciones de Kotlin
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.**

# Optimizaciones de Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Optimizaciones de Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Optimizaciones de Coil (Image Loading)
-keep class coil.** { *; }
-dontwarn coil.**

# Optimizaciones de OkHttp3
-dontwarn okhttp3.internal.platform.**
-dontwarn org.codehaus.mojo.animal_sniffer.*
-keep class okhttp3.** { *; }

# Remover warnings innecesarios
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn com.itextpdf.**

# Optimizaciones agresivas para reducir tamaño
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remover logging en release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Obfuscation más agresiva
-obfuscate
-repackageclasses