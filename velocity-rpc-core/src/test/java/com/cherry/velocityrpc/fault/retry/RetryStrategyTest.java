package com.cherry.velocityrpc.fault.retry;

import com.cherry.velocityrpc.model.RpcResponse;
import org.junit.Test;

/**
 * 充实策略测试
 */
public class RetryStrategyTest {
    RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();

    @Test
    public void doRetry() {
        try {
            RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败！");
            });
            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("充实多次失败！");
            e.printStackTrace();
        }
    }
}
