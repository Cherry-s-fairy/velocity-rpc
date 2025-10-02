package com.cherry.example.provider;

import com.cherry.example.common.service.UserService;
import com.cherry.velocityrpc.RpcApplication;
import com.cherry.velocityrpc.config.RegistryConfig;
import com.cherry.velocityrpc.config.RpcConfig;
import com.cherry.velocityrpc.model.ServiceMetaInfo;
import com.cherry.velocityrpc.registry.LocalRegistry;
import com.cherry.velocityrpc.registry.Registry;
import com.cherry.velocityrpc.registry.RegistryFactory;
import com.cherry.velocityrpc.server.HttpServer;
import com.cherry.velocityrpc.server.VertxHttpServer;
import com.cherry.velocityrpc.server.tcp.TcpServerHandler;
import com.cherry.velocityrpc.server.tcp.VertxTcpServer;

/**
 * 测试全局配置对象加载，能够根据配置动态地在不同端口启动web服务
 */
public class ProviderExample {
    public static void main(String[] args) {
        // Rpc框架初始化
        RpcApplication.init();
        // 注册服务到本地注册器
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);
        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        // 启动web服务
//        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());

        // 启动Tcp服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }
}
