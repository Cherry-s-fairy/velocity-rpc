package com.cherry.example.consumer;


import com.cherry.example.common.model.User;
import com.cherry.example.common.service.UserService;

/**
 * 简易服务消费者示例
 */
public class SimpleComsumerExample {
    public static void main(String[] args) {
        // todo 需要获取UserService的实现类对象
        // 之后目标：通过RPC框架快速得到一个支持远程调用服务提供者的代理对象，像调用本地方法一样调用UserService的方法
        UserService userService = null;
        User user = new User();
        user.setName("Cherry");
        // 调用
        User newUser = userService.getUser(user);
        if(newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
