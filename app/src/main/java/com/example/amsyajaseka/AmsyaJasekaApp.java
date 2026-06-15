package com.example.amsyajaseka;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class AmsyaJasekaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Follow system setting for Dark/Light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
