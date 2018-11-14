package com.mason.statisticstime.app.utils;

import android.support.annotation.NonNull;
import android.util.Log;



public class StatisticalTimeUtils {

    private static final String TAG = "MASON";
    private static StatisticalTimeUtils instance;
//    private static SharePrefUtils mSharePrefUtils;

    public static StatisticalTimeUtils getInstance() {
        if (instance == null) {
            instance = new StatisticalTimeUtils();
            // TODO: 2018/11/14
//            mSharePrefUtils = SharePrefUtils.getInstance(ApplicationData.mApplication, SharePrefUtils.SHAREPREFFILENAME, MODE_PRIVATE);
            StatisticalTimeExceptionIntercept.getInstance().register();
        }
        return instance;
    }

    private long activeTs = 0;//计算使用时间的时间戳
    private long foregroundTs = 0;//处于前台的时间戳
    private long reduceTime = 0;//消费的时间


    public void saveActiveTs() {
        activeTs = System.currentTimeMillis();
    }

    public void saveReduceTs(long appUseReduceTime) {
        ReduceTimeEntity reduceTime = getReduceTime();
        this.reduceTime = reduceTime.reduceTime;
        setReduceTime(appUseReduceTime + this.reduceTime);
    }

    public ReduceTimeEntity getReduceTime() {
        String reduceTimeSp = "0,0";
        try {
            // TODO: 2018/11/14 获取本地缓存的数据
//            reduceTimeSp = mSharePrefUtils.getStringValue(SharePreConstants.STATISTICAL_TIME_REDUCE_TIME, "0,0");
        } catch (Exception ex) {//不能删除此异常
            ex.printStackTrace();
        }
        return ReduceTimeEntity.convert(reduceTimeSp);
    }

    public void setReduceTime(long reduceTime) {
        this.reduceTime = reduceTime;
        String result = System.currentTimeMillis() + "," + reduceTime;
        // TODO: 2018/11/14 设置本地缓存的数据
//        mSharePrefUtils.setValue(SharePreConstants.STATISTICAL_TIME_REDUCE_TIME, result);
    }

    /**
     * 退出程序才会调用,包括异常退出
     */
    public void clearAll() {
        saveReduceTs(activeTs - foregroundTs);
        Log.d(TAG, "clearAll: " + reduceTime / 1000);
        activeTs = 0;
        foregroundTs = 0;
        uploadUserReduceTime();
    }

    public void saveForegroundTs() {
        foregroundTs = System.currentTimeMillis();
    }

    public void uploadUserReduceTime() {
        final ReduceTimeEntity reduceTime = getReduceTime();
        if (reduceTime.reduceTime > 5000) {//使用时间大于5秒上传
            // TODO: 2018/11/14 这里时自己的上传逻辑

        }
    }

    public static class ReduceTimeEntity {
        private long saveTs;//结算时间戳
        private long reduceTime;//使用事件

        public ReduceTimeEntity(long saveTs, long reduceTime) {
            this.saveTs = saveTs;
            this.reduceTime = reduceTime;
        }

        public long getSaveTs() {
            return saveTs;
        }

        public void setSaveTs(long saveTs) {
            this.saveTs = saveTs;
        }

        public long getReduceTime() {
            return reduceTime;
        }

        public void setReduceTime(long reduceTime) {
            this.reduceTime = reduceTime;
        }

        @Override
        public String toString() {
            return saveTs + "," + reduceTime;
        }

        public static ReduceTimeEntity convert(@NonNull String result) {
            String[] split = result.split(",");
            long saveTs = 0;
            long reduceTime = 0;
            if (split.length > 0) {
                saveTs = Long.valueOf(split[0]);
                reduceTime = Long.valueOf(split[1]);
            }
            return new ReduceTimeEntity(saveTs, reduceTime);
        }
    }

}
