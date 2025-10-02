package com.cherry.velocityrpc.config;

import com.cherry.velocityrpc.loadbalancer.LoadBalancerKeys;
import com.cherry.velocityrpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架全局配置信息
 */
@Data
public class RpcConfig {
    private boolean mock = false; // mock模拟调用

    private String name = "velocity-rpc"; // 名称
    private String version = "1.0"; // 版本号
    private String serverHost = "localhost"; // 服务器主机名
    private Integer serverPort = 8888; // 服务器端口号

    private String serializer = SerializerKeys.JDK; // 序列化器

    private RegistryConfig registryConfig = new RegistryConfig(); // 注册中心配置

    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN; // 默认负载均衡器配置
}
