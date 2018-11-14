package com.mason.statisticstime.app.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

public class ActivityLifecycleHelper implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = "MASON";

    private StatisticalTimeUtils persistentMgr;

    private int foregroundActivityCount = 0;
    private boolean isChangingConfigActivity = false;
    private boolean willSwitchToForeground = false;
    private boolean isForegroundNow = false;

    private String lastPausedActivityName;
    private int lastPausedActivityHashCode;
    private long lastPausedTime;
    private long appUseReduceTime = 0;


    private static ActivityLifecycleHelper activityLifecycleHelper;

    public static ActivityLifecycleHelper getInstance() {
        if (activityLifecycleHelper == null) {
            activityLifecycleHelper = new ActivityLifecycleHelper();
        }
        return activityLifecycleHelper;
    }


    public ActivityLifecycleHelper() {
        persistentMgr = StatisticalTimeUtils.getInstance();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (foregroundActivityCount == 0 || !isForegroundNow) {
            willSwitchToForeground = true;
        }

        if (isChangingConfigActivity) {
            isChangingConfigActivity = false;
            return;
        }

        foregroundActivityCount += 1;

    }

    @Override
    public void onActivityResumed(Activity activity) {
        persistentMgr.saveActiveTs();

        addAppUseReduceTimeIfNeeded(activity);

        if (willSwitchToForeground && isInteractive(activity)) {//进入前台
            isForegroundNow = true;
            persistentMgr.saveForegroundTs();
//            Log.i(TAG, "switch to foreground[" + foregroundTs + "]");
        }
        if (isForegroundNow) {
            willSwitchToForeground = false;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        persistentMgr.saveActiveTs();

        lastPausedActivityName = getActivityName(activity);
        lastPausedActivityHashCode = activity.hashCode();
        lastPausedTime = System.currentTimeMillis();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        addAppUseReduceTimeIfNeeded(activity);

        if (activity.isChangingConfigurations()) {
            isChangingConfigActivity = true;
            return;
        }

        foregroundActivityCount -= 1;
        if (foregroundActivityCount == 0) {//进入后台
            isForegroundNow = false;

            persistentMgr.clearAll();

//            Log.i(TAG, "switch to background (reduce time[" + appUseReduceTime + "])");
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    private void addAppUseReduceTimeIfNeeded(Activity activity) {
        if (getActivityName(activity).equals(lastPausedActivityName)
                && activity.hashCode() == lastPausedActivityHashCode) {
            long now = System.currentTimeMillis();
            if (now - lastPausedTime > 1000) {
                appUseReduceTime += now - lastPausedTime;
            }
        }

        lastPausedActivityHashCode = -1;
        lastPausedActivityName = null;
        lastPausedTime = 0;
        if (appUseReduceTime > 0) {
            persistentMgr.saveReduceTs(appUseReduceTime);
        }
    }

    private boolean isInteractive(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                return pm.isInteractive();
            } else {
                return pm.isScreenOn();
            }
        } else {
            return false;
        }

    }

    private String getActivityName(final Activity activity) {
        return activity.getClass().getCanonicalName();
    }


}
