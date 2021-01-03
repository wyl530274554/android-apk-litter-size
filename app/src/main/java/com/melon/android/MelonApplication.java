package com.melon.android;

import android.app.Application;

import com.melon.android.tool.MelonExceptionHandler;

public class MelonApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new MelonExceptionHandler(getApplicationContext()));
    }
}
