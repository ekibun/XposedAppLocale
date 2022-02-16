package com.flo354.xposed.applocale;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static XSharedPreferences prefs;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.v("XPOSED", "local");
        loadPrefs();

        try {
            XposedHelpers.findAndHookMethod(ContextWrapper.class, "attachBaseContext",
                    Context.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.args[0] == null || !(param.args[0] instanceof Context)) {
                                return;
                            }

                            Context context = (Context) param.args[0];
                            String packageName = context.getPackageName();
                            Locale locale = getPackageSpecificLocale(packageName);
                            String hostPackageName = AndroidAppHelper.currentPackageName();
                            boolean isActiveApp = hostPackageName.equals(packageName);

                            if (packageName == null || locale == null || !isActiveApp) {
                                return;
                            }

                            Resources res = context.getResources();
                            Configuration config = new Configuration(res.getConfiguration());

                            config.setLocale(locale); // config.setLayoutDirection(local);
                            context = context.createConfigurationContext(config);

                            param.args[0] = context;
                        }
                    }
            );
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        Log.v("XPOSED", "local");

        prefs.reload();

        // Override the default Locale if one is defined (not res-related, here)
        Locale packageLocale = getPackageSpecificLocale(lpparam.packageName);
        if (packageLocale != null) {
            Locale.setDefault(packageLocale);
        }
    }

    @Nullable
    private static Locale getPackageSpecificLocale(String packageName) {
        String locale = prefs.getString(packageName, Common.DEFAULT_LOCALE);

        if (locale.contentEquals(Common.DEFAULT_LOCALE)) {
            return null;
        }
        return Locale.forLanguageTag(locale);
    }

    private static void loadPrefs() {
        prefs = new XSharedPreferences(Common.MY_PACKAGE_NAME, Common.PREFS);
    }
}
