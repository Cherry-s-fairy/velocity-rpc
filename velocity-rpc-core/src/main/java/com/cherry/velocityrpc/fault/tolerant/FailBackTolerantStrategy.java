package com.cherry.velocityrpc.fault.tolerant;

import com.cherry.velocityrpc.model.RpcResponse;

import java.util.Map;

/**
 * 降级容错策略
 */
public class FailBackTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // todo 自行实现，获取降级服务并调用
        return null;
    }
}
