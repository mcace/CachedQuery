package com.mcsoft;

import com.mcsoft.bean.UserInfo;
import com.mcsoft.controller.QueryController;
import com.mcsoft.service.UserService;
import com.mcsoft.service.UserServiceImpl;

/**
 * 测试类
 * Created by Mc on 2018/7/3.
 */
public class QueryControllerTest {

    public static void main(String[] args) throws InterruptedException {
        UserService service = new UserServiceImpl();
        QueryController queryController = new QueryController(service);
        UserInfo info;
        long delay = 800;//延迟800毫秒
        int circle_time = 20;//循环次数
        long last_time = 0;

        for (int i = 1; i < circle_time + 1; i++) {
            for (int j = 0; j < 12; j++) {
                System.out.print("* ");
            }
            System.out.println();
            System.out.println("当前查询次数：" + i);
            System.out.println("当前时间：" + System.currentTimeMillis());
            long currentTime = System.currentTimeMillis();
            info = queryController.getUserInfo();
            long query_delay = System.currentTimeMillis() - currentTime;
            if (query_delay > 0) {
                System.out.println("查询延迟：" + query_delay);
            }
            System.out.println("查询结果：" + info.getTime());
            long result_diff = info.getTime() - last_time;
            if (result_diff > 0) {
                System.out.println("查询结果和上次差异：" + result_diff);
            }
            last_time = info.getTime();
            for (int j = 0; j < 12; j++) {
                System.out.print("* ");
            }
            System.out.println();
            Thread.sleep(delay);
        }

        queryController.stopJob();
    }

}
