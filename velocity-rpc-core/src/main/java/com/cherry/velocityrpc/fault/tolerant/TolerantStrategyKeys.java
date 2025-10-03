package com.cherry.velocityrpc.fault.tolerant;

/**
 * 容错策略键名常量
 */
public interface TolerantStrategyKeys {
    String FAIL_BACK = "failBack"; // 故障恢复
    String FAIL_FAST = "failFast"; // 快速失败
    String FAIL_OVER = "failOver"; // 故障转移
    String FAIL_SAFE = "failSafe"; // 静默处理
}
