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

# Stringee ProGuard rules - THEO TÀI LIỆU CHÍNH THỨC
# WebRTC
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**
-keepclassmembers class org.webrtc.** { *; }

# JNI
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class org.jni_zero.** { *; }

# Stringee
-dontwarn com.stringee.**
-keep class com.stringee.** { *; }
# Apache HTTP Client - Fix for Stringee SDK
-dontwarn org.apache.http.**
-keep class org.apache.http.** { *; }
-dontwarn android.net.http.AndroidHttpClient
-keep class android.net.http.AndroidHttpClient { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep model classes
-keep class com.example.doannt118.model.** { *; }

# Keep Stringee callbacks
-keep class com.example.doannt118.stringee.** { *; }

# Volley
-keep class com.android.volley.** { *; }
-dontwarn com.android.volley.**

# Mail
-dontwarn java.awt.**
-dontwarn java.beans.Beans
-keep class javax.mail.** { *; }
-keep class com.sun.mail.** { *; }
-dontwarn javax.activation.**
-dontwarn javax.mail.**
-dontwarn com.sun.mail.**