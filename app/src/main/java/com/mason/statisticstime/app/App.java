package com.mason.statisticstime.app;

import android.app.Application;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: 2018/11/14 统计初始化回调
//        registerActivityLifecycleCallbacks(ActivityLifecycleHelper.getInstance());
    }
}
