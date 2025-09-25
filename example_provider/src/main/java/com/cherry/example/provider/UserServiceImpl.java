package com.cherry.example.provider;

import com.cherry.example.common.model.User;
import com.cherry.example.common.service.UserService;

/**
 * 用户服务实现类：实现公共模块中的用户服务接口
 */
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        return user;
    }
}
