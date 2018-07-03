package com.mcsoft.controller;

import com.mcsoft.bean.UserInfo;
import com.mcsoft.cache.RunnableCacheJob;
import com.mcsoft.cache.UserInfoRunnableCacheJob;
import com.mcsoft.service.UserService;
import com.mcsoft.util.TimeUnit;

/**
 * 延迟查询示例Controller，实际应用中使用Spring管理Bean
 * 利用Spring默认的单例配置，可以在多线程环境下保证每个线程都是读到同一个缓存对象
 * Created by Mc on 2018/7/3.
 */
public class QueryController {
    //缓存任务，在Controller里用于获取数据
    private final UserInfoRunnableCacheJob cacheJob;

    private UserService userService;

    public QueryController(UserService userService) {
        this.userService = userService;
    }

    {
        //创建缓存获取线程，设定耗时操作的DataProvider。
        UserInfo cachedUserInfo = new UserInfo();
        RunnableCacheJob.DataProvider<UserInfo> dataProvider = new RunnableCacheJob.DataProvider<UserInfo>() {
            @Override
            public UserInfo getData() {
                return userService.getUserInfo();
            }
        };
        cacheJob = UserInfoRunnableCacheJob.UserInfoRunnableCacheJobBuilder.newBuilder()
                .setTargetCache(cachedUserInfo)
                .setDelayTime(6, TimeUnit.S).setDataProvider(dataProvider).build();

        Thread cacheJobThread = new Thread(cacheJob, "UserInfoCacheJob");
        cacheJobThread.start();
    }

    /**
     * 获取用户数据，实践中需要指定URL
     *
     * @return 用户数据
     */
    public UserInfo getUserInfo() {
        return cacheJob.getTargetInfoCache();
    }

    /**
     * 停止任务
     */
    public void stopJob(){
        cacheJob.stopJob();
    }
}
