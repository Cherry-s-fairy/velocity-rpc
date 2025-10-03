package com.cherry.velocityrpc.fault.retry;

import com.cherry.velocityrpc.model.RpcResponse;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔的重试策略
 *
 * 使用Guava-Retrying提供的RetryerBuilder 能够很方便地指定重试条件、重试等待策略、重试停止策略、重试工作等。
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy{
    /**
     * 固定时间间隔重试
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)                                   // 重试条件：当出现Exception异常时重试
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS)) // 等待策略：固定时间间隔3s
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))          // 停止策略： 超过最大重试次数(3)停止
                .withRetryListener(new RetryListener() {                                    // 重试工作：监听重试并打印
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数：{}", attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}
