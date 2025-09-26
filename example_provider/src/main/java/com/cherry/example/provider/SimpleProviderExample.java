package com.cherry.example.provider;

import com.cherry.example.common.service.UserService;
import com.cherry.velocityrpc.registry.LocalRegistry;
import com.cherry.velocityrpc.server.HttpServer;
import com.cherry.velocityrpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class SimpleProviderExample {
    public static void main(String[] args) {
        // 在本地注册器中注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动web服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
