-keep public class com.hidebrowserad.HookEntry {
    public void handleLoadPackage(de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam);
}
-keepclassmembers class com.hidebrowserad.HookEntry { *; }

-dontwarn de.robv.android.xposed.**
-keep interface de.robv.android.xposed.** { *; }
-keep class de.robv.android.xposed.** { *; }
-keep class * extends de.robv.android.xposed.IXposedHookLoadPackage { *; }
-keep class * extends de.robv.android.xposed.IXposedHookZygoteInit { *; }
-keep class * extends de.robv.android.xposed.XC_MethodHook { *; }
-keep class * extends de.robv.android.xposed.XC_LoadPackage { *; }

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontpreverify
-allowaccessmodification
-repackageclasses ''
