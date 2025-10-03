package com.cherry.velocityrpc.bootstrap;

import com.cherry.velocityrpc.RpcApplication;

/**
 * 服务消费者启动类（初始化
 */
public class ConsumerBootstrap {
    /**
     * 初始化
     */
    public static void init() {
        // Rpc框架初始化（配置和注册中心
        RpcApplication.init();
    }
}
