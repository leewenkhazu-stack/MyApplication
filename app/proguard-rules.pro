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

# Firebase and Google Identity Services - keep rules
# These libraries use reflection and need to be preserved

# Firebase Auth
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.** { *; }
-keepnames class com.google.firebase.** { *; }

# Google Play Services Auth
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keepnames class com.google.android.gms.** { *; }

# Google Identity Services
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class com.google.android.libraries.gmscore.** { *; }
-keepnames class com.google.android.libraries.identity.** { *; }

# Androidx Credentials
-keep class androidx.credentials.** { *; }
-keepnames class androidx.credentials.** { *; }

# Credential Manager
-keep class androidx.credentials.playservices.** { *; }

# Keep authentication-related classes
-keep class **.*AuthViewModel { *; }
-keep class **.*GoogleSignInManager { *; }
-keep interface **.*AuthViewModelFactory { *; }

# Preserve stack traces for debugging
-keepattributes SourceFile,LineNumberTable

# Rename source file to generic for smaller APK
-renamesourcefileattribute SourceFile
