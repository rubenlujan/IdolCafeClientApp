package com.hrg.idolcafeclientapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class GenericMethods {
    public static String getAppSettingsStringValue(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                "AppSettings",
                Context.MODE_PRIVATE);

        return  sharedPref.getString(key, "");
    }
    public static int getAppSettingsIntValue(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                "AppSettings",
                Context.MODE_PRIVATE);

        return sharedPref.getInt(key, 0);
    }
}
