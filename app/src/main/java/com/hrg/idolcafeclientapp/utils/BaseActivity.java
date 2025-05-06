package com.hrg.idolcafeclientapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context base) {
        SharedPreferences prefs = base.getSharedPreferences("settings", MODE_PRIVATE);
        String lang = prefs.getString("lang", "es"); // por defecto español
        Context context = LocaleHelper.wrap(base);
        super.attachBaseContext(context);
    }
}
