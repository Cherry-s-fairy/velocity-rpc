package com.cherry.velocityrpc.fault.retry;

import com.cherry.velocityrpc.spi.SpiLoader;

/**
 * 重试策略工厂，用于获得重试器对象
 */
public class RetryStrategyFactory {
    static {
        SpiLoader.load(RetryStrategy.class);
    }

    // 默认重试器
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
