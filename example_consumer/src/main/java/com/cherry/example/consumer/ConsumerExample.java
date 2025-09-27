package com.cherry.example.consumer;

import com.cherry.velocityrpc.config.RpcConfig;
import com.cherry.velocityrpc.utils.ConfigUtils;

/**
 * 服务消费者示例
 */
public class ConsumerExample {
    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}
