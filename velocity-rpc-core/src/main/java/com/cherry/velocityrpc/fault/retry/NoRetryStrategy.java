package com.cherry.velocityrpc.fault.retry;

import com.cherry.velocityrpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试，即只执行一次
 */
public class NoRetryStrategy implements RetryStrategy{
    /**
     * 不重试
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
