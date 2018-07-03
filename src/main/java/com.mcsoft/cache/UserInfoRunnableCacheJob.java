package com.mcsoft.cache;

import com.mcsoft.bean.UserInfo;
import com.mcsoft.util.TimeUnit;

/**
 * 获取用户信息缓存任务
 * Created by Mc on 2018/7/3.
 */
public class UserInfoRunnableCacheJob extends RunnableCacheJob<UserInfo> {

    /**
     * 更新目标对象，可以从外部传来，则会自动更新外部对象
     * 也可以不设定，则需要调用getTargetInfoCache手动获取
     */
    private UserInfo targetInfoCache;
    /**
     * 这个锁用来保证首次访问缓存数据程序阻塞
     */
    private final Object lock = new Object();
    private volatile boolean first_query = true;//保证首次访问阻塞读

    UserInfoRunnableCacheJob(UserInfoRunnableCacheJobBuilder builder) {
        super(builder);
        targetInfoCache = builder.getTargetInfoCache();
    }

    @Override
    public void run() {
        Thread timer = null;
        while (!isStoped) {
            // Timer逻辑：
            // 第一次访问时不设定Timer
            // 当Timer线程开始计时时，不进入获取缓存的逻辑
            // 当Timer线程停止时，开始查询
            // 当获取缓存的耗时操作结束时，如果Timer已经停止了，那么就会再次获取缓存
            // 这样就保证了数据至少间隔了设定时间去获取数据
            // 如果直接用sleep方法，则会在耗时操作后再间隔一个设定时间才会去重新获取数据，数据时效性会打折扣
            if (null == timer || !timer.isAlive()) {
                timer = createTimer();
                timer.start();
                //下面是耗时操作
                targetInfoCache = getData();
                //首次访问获取到数据后解除阻塞
                if (first_query) {
                    synchronized (lock) {
                        first_query = false;
                        lock.notifyAll();
                    }
                }
            }
        }

    }

    /**
     * 获取用户数据缓存，首次访问会因为获取数据的延迟而阻塞
     * 当有缓存数据后，则不会再阻塞，直接返回缓存数据。
     *
     * @return {@link UserInfo}
     */
    public UserInfo getTargetInfoCache() {
        //第一次访问且有锁时，则阻塞
        if (first_query) {
            //使用wait来阻塞线程，不用while因为while会占用CPU时间片
            synchronized (lock) {
                try {
                    // 双重校验，防止当前代码块在获取锁的时候触发notifyAll，线程就会永远阻塞在wait上
                    // 双重校验逻辑解决的问题:
                    // 当前线程判断if(first_query)成功，但另一个线程已经拿到lock的锁，本线程阻塞在同步，
                    // 然后另一个线程触发lock.notifyAll，first_query设为false，本线程进入同步代码块
                    // 触发lock.wait，但由于first_query为false，另一个线程永远不会再触发notifyAll了，
                    // 本线程就永远阻塞了。
                    // 可以用wait(long)来设定时间，但不知道耗时操作实际执行时间，可能发生那边没查完
                    // ，这边就返回空结果的情况。
                    if (first_query) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return targetInfoCache;
        }
        return targetInfoCache;
    }

    /**
     * 用户信息缓存任务Builder
     */
    public static class UserInfoRunnableCacheJobBuilder extends RunnableCacheJobBuilder<UserInfo> {
        /**
         * 更新目标对象
         */
        private UserInfo targetInfoCache;

        UserInfoRunnableCacheJobBuilder() {
            super();
        }

        public static UserInfoRunnableCacheJobBuilder newBuilder() {
            return new UserInfoRunnableCacheJobBuilder();
        }

        public UserInfoRunnableCacheJobBuilder setDelayTime(int delayTime, TimeUnit timeUnit) {
            super.setDelayTime(delayTime, timeUnit);
            return this;
        }

        public UserInfoRunnableCacheJobBuilder setDataProvider(DataProvider<UserInfo> dataProvider) {
            super.setDataProvider(dataProvider);
            return this;
        }

        public UserInfoRunnableCacheJobBuilder setTargetCache(UserInfo targetInfoCache) {
            this.targetInfoCache = targetInfoCache;
            return this;
        }

        @Override
        public UserInfoRunnableCacheJob build() {
            return new UserInfoRunnableCacheJob(this);
        }

        UserInfo getTargetInfoCache() {
            return targetInfoCache;
        }

        void validate() {
            super.validate();
            if (null == targetInfoCache) {
                targetInfoCache = new UserInfo();
            }
        }
    }
}
