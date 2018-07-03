package com.mcsoft.cache;

import com.mcsoft.util.TimeUnit;

/**
 * 多线程执行可缓存任务
 * 缓存和获取数据的逻辑分成两个线程，取数据只能取出缓存数据，获取数据的逻辑单独在一个线程里重复更新缓存
 * Created by Mc on 2018/7/3.
 */
public abstract class RunnableCacheJob<T> implements Runnable {

    boolean isStoped = false;//任务结束标识，用于while循环时判断

    private TimeUnit timeUnit;
    private int delayTime;//延迟时间，默认60秒
    private DataProvider<T> dataProvider;//数据提供器

    RunnableCacheJob(RunnableCacheJobBuilder<T> builder) {
        timeUnit = builder.getTimeUnit();
        delayTime = builder.getDelayTime();
        dataProvider = builder.getDataProvider();
    }

    T getData() {
        return dataProvider.getData();
    }

    public void stopJob(){
        this.isStoped = true;
    }

    /**
     * 一个简单的计时器，利用Thread.sleep()和while(!isAlive())实现定时重复执行
     */
    Thread createTimer() {
        final long delayTimeMs = this.timeUnit.toMillisecond(this.delayTime);
        return new Thread(() -> {
            try {
                Thread.sleep(delayTimeMs);
            } catch (InterruptedException e) {
                //do nothing
            }
        });
    }


    /**
     * 数据提供器接口，需要调用方自己实现
     *
     * @param <T>
     */
    public interface DataProvider<T> {
        /**
         * 返回数据接口
         *
         * @return {@link T}
         */
        T getData();
    }

    /**
     * 缓存任务Builder
     */
    public abstract static class RunnableCacheJobBuilder<T> {
        private TimeUnit timeUnit;//时间单位，默认秒
        private int delayTime;//延迟时间，默认60秒
        private DataProvider<T> dataProvider;//数据提供器

        RunnableCacheJobBuilder() {
        }

        /**
         * 新建Builder，需要实现类自行实现
         *
         * @return null
         */
        public static RunnableCacheJobBuilder newBuilder() {
            return null;
        }

        public abstract RunnableCacheJob build();

        public RunnableCacheJobBuilder setDelayTime(int delayTime, TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            this.delayTime = delayTime;
            return this;
        }

        public RunnableCacheJobBuilder setDataProvider(DataProvider<T> dataProvider) {
            this.dataProvider = dataProvider;
            return this;
        }

        TimeUnit getTimeUnit() {
            return timeUnit;
        }

        int getDelayTime() {
            return delayTime;
        }

        DataProvider<T> getDataProvider() {
            return dataProvider;
        }

        void validate() {
            if (null == timeUnit) {
                timeUnit = TimeUnit.S;
            }
            if (0 == delayTime) {
                delayTime = 60;
            }
            if (null == dataProvider) {
                throw new IllegalStateException("未设定缓存任务的DataProvider");
            }
        }
    }
}
