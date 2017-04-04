package com.dr8.xposed.saax;

import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Xposed implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static boolean DEBUG = false;
    private static String TAG = "SAAX: ";
    private static XSharedPreferences prefs;

    private static void log(String tag, String msg) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String formattedDate = df.format(c.getTime());
        XposedBridge.log("[" + formattedDate + "] " + tag + ": " + msg);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        prefs = new XSharedPreferences("com.dr8.xposed.saax", "com.dr8.xposed.saax_preferences");
        prefs.makeWorldReadable();
        DEBUG = prefs.getBoolean("debug", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String targetpkg = "com.google.android.projection.gearhead";
        String targetcls = "com.google.android.b.b";
        String targetcls2 = "com.google.android.projection.gearhead.sdk.b";

        if (lpparam.packageName.equals(targetpkg)) {
            if (DEBUG) log(TAG, "Hooked Android Auto package");

            XposedHelpers.findAndHookMethod(targetcls, lpparam.classLoader, "c", String.class, Integer.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            String s = (String) param.args[0];
                            int i = (int) param.args[1];
                            if (s.equals("gearhead:max_forward_clicks")) {
                                if (DEBUG) log(TAG, s + " is currently: " + i);
                                param.args[1] = prefs.getInt("pref_maxTaps", 6);
                                if (DEBUG) log(TAG, "setting " + s + " to " +
                                        prefs.getInt("pref_maxTaps", 6));
                            }
                        }
                    });

            XposedHelpers.findAndHookMethod(targetcls2, lpparam.classLoader, "E", Bundle.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            Bundle b = (Bundle) param.args[0];
                            int pages = b.getInt("com.google.android.projection.gearhead.sdk.MAX_PAGES");
                            if (DEBUG) log(TAG, "MAX_PAGES is " + pages + ", setting to " + prefs.getInt("pref_maxPages",
                                    5));
                            b.putInt("com.google.android.projection.gearhead.sdk.MAX_PAGES", prefs.getInt("pref_maxPages",
                                    5));
                        }

                    });

        }

    }
}
