package com.cherry.velocityrpc.constant;

import com.cherry.velocityrpc.serializer.SerializerKeys;

/**
 * 存储RPC框架相关常量
 */
public interface RpcConstant {
    boolean DEFAULT_MOCK = false; // 默认是否启用mock
    String DEFAULT_CONFIG_PREFIX = "rpc"; // 默认配置文件加载前缀
    String DEFAULT_NAME = "velocity_rpc"; // 默认名称
    String DEFAULT_SERVICE_VERSION = "1.0"; // 默认服务版本(注册中心
    String DEFAULT_SERVICE_HOST = "localhost"; // 默认服务地址
    Integer DEFAULT_SERVICE_PORT = 8888; // 默认服务端口号
    String DEFAULT_SERIALIZER = SerializerKeys.JDK; // 默认序列化器
}
