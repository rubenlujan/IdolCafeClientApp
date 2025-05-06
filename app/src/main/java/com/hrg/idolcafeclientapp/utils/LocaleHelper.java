package com.hrg.idolcafeclientapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {

    public static Context wrap(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lang = prefs.getString("lang", "es");

        Locale newLocale = new Locale(lang);
        Locale.setDefault(newLocale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(newLocale);

        return context.createConfigurationContext(config);
    }
}
