package com.cherry.velocityrpc.bootstrap;

import com.cherry.velocityrpc.RpcApplication;
import com.cherry.velocityrpc.config.RegistryConfig;
import com.cherry.velocityrpc.config.RpcConfig;
import com.cherry.velocityrpc.model.ServiceMetaInfo;
import com.cherry.velocityrpc.model.ServiceRegisterInfo;
import com.cherry.velocityrpc.registry.LocalRegistry;
import com.cherry.velocityrpc.registry.Registry;
import com.cherry.velocityrpc.registry.RegistryFactory;
import com.cherry.velocityrpc.server.tcp.VertxTcpClient;
import com.cherry.velocityrpc.server.tcp.VertxTcpServer;
import io.vertx.core.Vertx;

import java.util.List;

/**
 * 服务提供者启动类（初始化
 */
public class ProviderBootstrap {
    /**
     * 初始化
     * @param serviceRegisterInfoList
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // Rpc框架初始化（配置和注册中心
        RpcApplication.init();
        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 注册服务
        for(ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + "服务注册失败！", e);
            }
        }

        // 启动服务器
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }
}
