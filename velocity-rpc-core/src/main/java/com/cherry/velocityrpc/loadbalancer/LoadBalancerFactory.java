package com.cherry.velocityrpc.loadbalancer;

import com.cherry.velocityrpc.spi.SpiLoader;

/**
 * 负载均衡器工厂（获取负载均衡器对象
 *
 * 使用工厂模式，支特根据key从SPI获取负载均衡器对象实例。
 */
public class LoadBalancerFactory {
    static {
        SpiLoader.load(LoadBalancer.class);
    }

    // 默认负载均衡器
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }
}
