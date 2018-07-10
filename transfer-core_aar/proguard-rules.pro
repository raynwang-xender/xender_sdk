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


-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep class com.android*.** {*; }
-keep class com.google*.** {*; }
-keep class org.*.** {*; }
-keep class net.sourceforge.pinyin4j.** {*; }
-keep class com.hp.hpl.sparta.** {*; }
-keep class demo.** {*; }


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService


-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}



-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keep class cn.xender.basicservice.Offer
-keep class cn.xender.basicservice.Relation
-keep class * implements cn.xender.core.ValueObject
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.support.v4.app.FragmentActivity
-keep class * extends android.support.v4.app.Fragment
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.preference.Preference
-keep class * extends android.view
-keep class * extends android.widget

-keep public final class cn.xender.*.R

-keep class cn.xender.*.R$*{
	public static final int *;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


-keep class * extends android.view.View {
public <init>(android.content.Context);
public <init>(android.content.Context, android.util.AttributeSet);
public <init>(android.content.Context, android.util.AttributeSet, int);
public void set*(...);
}

-keep public class cn.xender.core.**{*;}



-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-dontskipnonpubliclibraryclasses
-dontoptimize
-printmapping map.txt
-printseeds seed.txt
-ignorewarnings
-dontusemixedcaseclassnames

