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

    public static boolean DEBUG = false;
    private static String TAG = "SAAX: ";
    private static XSharedPreferences prefs;

    public static void log(String tag, String msg) {
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
        String targetcls = "com.google.android.gsf.e";
        String targetcsecls = "com.google.android.gms.car.CarSensorEvent";


        if (lpparam.packageName.equals(targetpkg)) {
            if (DEBUG) log(TAG, "Hooked Android Auto package");

            Class<?> CSEcls = XposedHelpers.findClass(targetcsecls, lpparam.classLoader);
            XposedBridge.hookAllConstructors(CSEcls, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int b = (int) param.args[1];
                    switch (b) {
                        case 6:
                            if (DEBUG) log(TAG, "Dropped CSE instance. b was " + b + " (ParkingBrakeData)");
                            param.setResult(null);
                            break;
                        case 7:
                            if (DEBUG) log(TAG, "Dropped CSE instance. b was " + b + " (GearData)");
                            param.setResult(null);
                            break;
                        case 11:
                            if (DEBUG) log(TAG, "Dropped CSE instance. b was " + b + " (DrivingStatusData)");
                            param.setResult(null);
                            break;
                        default:
                    }
                }
            });

            XposedHelpers.findAndHookMethod(targetcls, lpparam.classLoader, "a", String.class, Integer.class,
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

            XposedHelpers.findAndHookMethod(targetcls, lpparam.classLoader, "a", String.class, Float.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            prefs.reload();
                            String s = (String) param.args[0];
                            float f = (float) param.args[1];
                            if (s.equals("gearhead:max_speed_unlimited_browsing") ||
                                    s.equals("gearhead:max_speed_parking_card")) {
                                if (DEBUG) log(TAG, s + " is currently: " + f);
                                float tmpflt = (float) prefs.getInt("pref_maxSpeed", 150) * 0.44704F;
                                param.args[1] = tmpflt;
                                if (DEBUG) log(TAG, "setting " + s + " to " +
                                        tmpflt);
                            }
                        }
                    });

        }

    }
}
