package com.dr8.xposed.saax;

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
//      String targetcls = "com.google.android.b.b";
//      String targetcls = "cvu"; 2.4.72280*
//      String targetcls = "cyi"; 2.4.72290*
//      String targetcls = "dgi"; 2.5.72860*
//        String targetcls = "dlg"; 2.6.57340*
        String targetcls = "dof";

//      String targetcls2 = "com.google.android.projection.gearhead.sdk.b";
//      String targetcls2 = "awk"; 2.4.72280*
//      String targetcls2 = "awc"; 2.4.72290*
//      String targetcls2 = "awr"; 2.5.72860*
//        String targetcls2 = "axw"; 2.6.57340*
        String targetcls2 = "azd";

        String targetpkg = "com.google.android.projection.gearhead";
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

            XposedHelpers.findAndHookMethod(targetcls, lpparam.classLoader, "j", String.class, boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            String s = (String) param.args[0];
                            boolean i = (boolean) param.args[1];
                            if (s.equals("gearhead:passenger_mode_feature_enabled")) {
                                if (DEBUG) log(TAG, s + " is currently: " + i);
                                param.args[1] = prefs.getBoolean("pref_passMode", true);
                                if (DEBUG) log(TAG, "setting " + s + " to " +
                                        prefs.getBoolean("pref_passMode", true));
                            }
                        }
                    });

            XposedHelpers.findAndHookMethod(targetcls2, lpparam.classLoader, "a", String.class, Boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            String s = (String) param.args[0];
                            boolean i = (boolean) param.args[1];
                            if (s.equals("gearhead:content_rate_limit_enabled")) {
                                if (DEBUG) log(TAG, s + " is currently: " + i);
                                if (prefs.getBoolean("pref_ratelimit", false)) {
                                    param.args[1] = false;
                                    if (DEBUG) log(TAG, "setting " + s + " to false");

                                } else {
                                    param.args[1] = true;
                                    if (DEBUG) log(TAG, "setting " + s + " to true");
                                }
                            }
                            if (s.equals("gearhead:feature_vanagon_unlimited_browse_speed_bump")) {
                                if (DEBUG) log(TAG, s + " is currently: " + i);
                                param.args[1] = false;
                                if (DEBUG) log(TAG, s + " is now: false");
                            }
                        }
                    });


            XposedHelpers.findAndHookMethod(targetcls2, lpparam.classLoader, "a", String.class, Float.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            String s = (String) param.args[0];
                            float i = (float) param.args[1];
                            if (s.equals("gearhead:max_parked_speed_wheel_sensor") || s.equals("gearhead:max_parked_speed_gps_sensor")) {
                                if (DEBUG) log(TAG, s + " is currently: " + i);
                                float tmpflt = (float) prefs.getInt("pref_maxSensorSpeed", 150) * 0.44704F;
                                param.args[1] = tmpflt;
                                if (DEBUG) log(TAG, "setting " + s + " to " + tmpflt);
                            }

                        }
                    });
        }
    }
}