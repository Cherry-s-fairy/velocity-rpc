package com.cherry.example.common.service;

import com.cherry.example.common.model.User;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);

    /**
     * 默认方法，测试Mock动态代理
     * @return
     */
    default short getNumber() {
        return (short) 1;
    }
}
