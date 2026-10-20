package com.hk.dropapp.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedHelper {
    public static final String VIBRATION = "Vibration.shared";
    public static final String SOUND = "Sound.shared";
    public static final String LANG = "Lang.shared";



    public static void setLang(Context context, String value){
        getDefaultShared(context).edit().putString(LANG,value).apply();
    }
    public static String getLang(Context context){
        return getDefaultShared(context).getString(LANG,null);
    }
    public static boolean getSound(Context context){
        return getDefaultShared(context).getBoolean(SOUND,true);
    }
    public static void setSound(Context context,boolean value){
        getDefaultShared(context).edit().putBoolean(SOUND,value).apply();
    }
    public static boolean getVibration(Context context){
        return getDefaultShared(context).getBoolean(VIBRATION,true);
    }
    public static void setVibration(Context context,boolean value){
        getDefaultShared(context).edit().putBoolean(VIBRATION,value).apply();
    }


    private static SharedPreferences getDefaultShared(Context context){
        return context.getSharedPreferences("com.hk.dropapp",Context.MODE_PRIVATE);
    }
}
