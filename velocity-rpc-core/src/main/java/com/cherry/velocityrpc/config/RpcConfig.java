package com.cherry.velocityrpc.config;

import lombok.Data;

/**
 * RPC 框架全局配置信息
 */
@Data
public class RpcConfig {
    private String name = "velocity-rpc"; // 名称
    private String version = "1.0"; // 版本号
    private String serverHost = "http://localhost"; // 服务器主机名
    private Integer serverPort = 8080; // 服务器端口号
}
