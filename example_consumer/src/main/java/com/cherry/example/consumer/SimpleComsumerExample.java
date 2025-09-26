package com.cherry.example.consumer;


import com.cherry.example.common.model.User;
import com.cherry.example.common.service.UserService;
import com.cherry.velocityrpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例
 *
 * 设置静态代理UserServiceProxy简化消费方调用（不灵活，摒弃
 * 设置动态代理
 */
public class SimpleComsumerExample {
    public static void main(String[] args) {
//        // 静态代理
//        UserService userService = new UserServiceProxy();
        // 动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = new User();
        user.setName("cherry");
        // 调用
        User newUser = userService.getUser(user);
        if(newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
