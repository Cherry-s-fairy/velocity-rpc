package com.cherry.example.provider;

import com.cherry.example.common.service.UserService;
import com.cherry.velocityrpc.RpcApplication;
import com.cherry.velocityrpc.registry.LocalRegistry;
import com.cherry.velocityrpc.server.HttpServer;
import com.cherry.velocityrpc.server.VertxHttpServer;

/**
 * 测试全局配置对象加载，能够根据配置动态地在不同端口启动web服务
 */
public class ProviderExample {
    public static void main(String[] args) {
        // Rpc框架初始化
        RpcApplication.init();
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        // 启动web服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
