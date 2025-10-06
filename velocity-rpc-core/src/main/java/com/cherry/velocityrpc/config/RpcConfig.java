package com.cherry.velocityrpc.config;

import com.cherry.velocityrpc.constant.RpcConstant;
import com.cherry.velocityrpc.fault.retry.RetryStrategyKeys;
import com.cherry.velocityrpc.fault.tolerant.TolerantStrategyKeys;
import com.cherry.velocityrpc.loadbalancer.LoadBalancerKeys;
import lombok.Data;

/**
 * RPC 框架全局配置信息
 */
@Data
public class RpcConfig {
    private boolean mock = RpcConstant.DEFAULT_MOCK; // mock模拟调用

    private String name = RpcConstant.DEFAULT_NAME; // 名称
    private String version = RpcConstant.DEFAULT_SERVICE_VERSION; // 版本号
    private String serverHost = RpcConstant.DEFAULT_SERVICE_HOST; // 服务器主机名
    private Integer serverPort = RpcConstant.DEFAULT_SERVICE_PORT; // 服务器端口号

    private String serializer = RpcConstant.DEFAULT_SERIALIZER; // 序列化器

    private RegistryConfig registryConfig = new RegistryConfig(); // 注册中心配置

    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN; // 默认负载均衡器配置

    private String retryStrategy = RetryStrategyKeys.NO; // 默认重试策略

    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST; // 默认容错策略
}
