package com.cherry.velocityrpc.registry;

import com.cherry.velocityrpc.spi.SpiLoader;

/**
 * 注册中心工厂，用于获取注册中心对象
 *
 * 一个成熟的RPC框架可能会支特多个注册中心，让开发者够填写配置来指定使用的注册中心，并且支特自定义注册中心，让上框架更易用、更利于扩展。
 * 使用工厂创建对象、使用SPI动态加载自定义的注册中心。
 */
public class RegistryFactory {
    static {
        SpiLoader.load(Registry.class);
    }

    // 默认注册中心
    private static final Registry DEFAULT_REGISTRY = new EtcdRegistry();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Registry getInstance(String key) {
        return SpiLoader.getInstance(Registry.class, key);
    }
}
