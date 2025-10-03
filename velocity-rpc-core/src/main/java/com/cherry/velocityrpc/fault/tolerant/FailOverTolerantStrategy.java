package com.cherry.velocityrpc.fault.tolerant;

import com.cherry.velocityrpc.model.RpcResponse;

import java.util.Map;

/**
 * 转移容错策略
 */
public class FailOverTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // todo 自行实现，获取其他服务节点并调用
        return null;
    }
}
