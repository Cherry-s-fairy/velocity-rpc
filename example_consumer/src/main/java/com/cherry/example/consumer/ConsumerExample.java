package com.cherry.example.consumer;

import com.cherry.example.common.model.User;
import com.cherry.example.common.service.UserService;
import com.cherry.velocityrpc.config.RpcConfig;
import com.cherry.velocityrpc.proxy.ServiceProxyFactory;
import com.cherry.velocityrpc.utils.ConfigUtils;

/**
 * 测试配置文件读取（application.properties）
 */
public class ConsumerExample {
    public static void main(String[] args) {
//        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
//        System.out.println(rpc);
        for(int i=0; i<3; i++) {
            // 获取代理
            UserService userService = ServiceProxyFactory.getProxy(UserService.class);

            User user = new User();
            user.setName("cherry");
            User newUser = userService.getUser(user);

            if(newUser != null) {
                System.out.println(newUser.getName());
            } else {
                System.out.println("user == null");
            }

            long number = userService.getNumber();
            System.out.println(number);
        }
    }
}
