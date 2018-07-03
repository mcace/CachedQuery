package com.mcsoft.service;

import com.mcsoft.bean.UserInfo;

/**
 * {@link UserService}实现类
 * Created by Mc on 2018/7/3.
 */
public class UserServiceImpl implements UserService {
    @Override
    public UserInfo getUserInfo() {
        UserInfo info = new UserInfo();
        try {
            //模拟查询的延迟
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long time = System.currentTimeMillis();
        info.setTime(time);
        return info;
    }
}
