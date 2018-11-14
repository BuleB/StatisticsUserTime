package com.mason.statisticstime.app.utils;

import android.util.Log;

public class StatisticalTimeExceptionIntercept implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "MASON";
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private static StatisticalTimeExceptionIntercept instance;

    public static StatisticalTimeExceptionIntercept getInstance(){
        if (instance==null) {
            instance = new StatisticalTimeExceptionIntercept();
        }
        return instance;
    }

    public void register() {

        if (Thread.getDefaultUncaughtExceptionHandler() == this) {
            return;
        }

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.d(TAG, "uncaughtException: ");
        StatisticalTimeUtils.getInstance().saveActiveTs();
        StatisticalTimeUtils.getInstance().clearAll();
         if (mDefaultHandler != null && mDefaultHandler != Thread.getDefaultUncaughtExceptionHandler()) {
            mDefaultHandler.uncaughtException(t, e);
        }
    }

}
