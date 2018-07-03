package com.mcsoft.util;

/**
 * 时间单位
 * Created by Mc on 2018/7/3.
 */
public enum TimeUnit {
    MS(1), S(1000), M(60 * 1000), H(60 * 60 * 1000);

    private int unitTimes;

    TimeUnit(int unitTimes) {
        this.unitTimes = unitTimes;
    }

    public long toMillisecond(int time) {
        return unitTimes * time;
    }
}
