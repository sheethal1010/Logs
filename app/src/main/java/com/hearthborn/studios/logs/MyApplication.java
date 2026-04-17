package com.hearthborn.studios.logs;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataMigrationUtil.performMigrations(this);
    }
}