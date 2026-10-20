package com.hk.dropapp.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.hk.dropapp.BuildConfig;

public class SharedHelper {
    public static final String LANG = "Lang.shared";
    public static final String SOUND = "Sound.shared";
    public static final String VIBRATION = "Vibration.shared";
    public static final String AMOUNT_UNIT = "AmountUnit.shared";
    public static final String DOSE_UNIT = "DoseUnit.shared";
    public static final String DROP_INDEX = "DropIndex.shared";

    public static void setLang(Context context, String value) {
        getDefaultShared(context).edit().putString(LANG, value).apply();
    }

    public static String getLang(Context context) {
        return getDefaultShared(context).getString(LANG, null);
    }

    public static boolean getSound(Context context) {
        return getDefaultShared(context).getBoolean(SOUND, true);
    }

    public static void setSound(Context context, boolean value) {
        getDefaultShared(context).edit().putBoolean(SOUND, value).apply();
    }

    public static boolean getVibration(Context context) {
        return getDefaultShared(context).getBoolean(VIBRATION, true);
    }

    public static void setVibration(Context context, boolean value) {
        getDefaultShared(context).edit().putBoolean(VIBRATION, value).apply();
    }

    public static int getInt(Context context, String key, int def) {
        return getDefaultShared(context).getInt(key, def);
    }

    public static void setInt(Context context, String key, int value) {
        getDefaultShared(context).edit().putInt(key, value).apply();
    }

    private static SharedPreferences getDefaultShared(Context context) {
        return context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    }
}
