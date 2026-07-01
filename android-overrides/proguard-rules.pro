# Add project specific ProGuard rules here.

# Capacitor / WebView bridge — keep JS interface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.getcapacitor.** { *; }
-keep class com.meditrack.retailorder.** { *; }

# Keep native <-> web bridge annotations
-keepattributes JavascriptInterface
-keepattributes *Annotation*

# Standard Android
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-dontwarn org.jetbrains.annotations.**
