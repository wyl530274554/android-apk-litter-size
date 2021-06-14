package com.melon.android;

import android.app.Application;

import com.melon.android.tool.MelonExceptionHandler;

public class MelonApplication extends Application {
    public static MelonApplication appContext;
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        Thread.setDefaultUncaughtExceptionHandler(new MelonExceptionHandler(getApplicationContext()));
    }
}
