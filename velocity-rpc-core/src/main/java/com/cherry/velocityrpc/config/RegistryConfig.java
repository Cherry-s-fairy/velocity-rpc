package com.cherry.velocityrpc.config;

import lombok.Data;

/**
 * Rpc框架注册中心配置
 */
@Data
public class RegistryConfig {
    private String registry = "etcd"; // 注册中心类别
    private String address = "http://localhost:2380"; // 注册中心地址
    private String username; // 用户名
    private String password; // 密码
    private Long timeout = 10000L; // 超时时间
}
