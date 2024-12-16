package com.coursee.free.activities;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.onesignal.OneSignal;

public class MyApplication extends Application {

    Activity activity;
    public static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId("YOUR-ONESIGNAL-APP-ID"); // Замените на ваш APP_ID

        // Опционально: настройка логирования
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // Опционально: настройка уведомлений
        OneSignal.setNotificationWillShowInForegroundHandler(notificationReceivedEvent -> {
            notificationReceivedEvent.complete(notificationReceivedEvent.getNotification());
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }
}