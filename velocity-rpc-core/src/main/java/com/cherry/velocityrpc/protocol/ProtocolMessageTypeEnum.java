package com.cherry.velocityrpc.protocol;

import lombok.Getter;

/**
 * 自定义协议的消息类型枚举
 */
@Getter
public enum ProtocolMessageTypeEnum {
    REQUEST(0), // 请求
    RESPONSE(1), // 响应
    HEART_BEAT(2), // 心跳
    OTHERS(3); // 其他

    private final int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    /**
     * 根据key获取枚举
     * @param key
     * @return
     */
    public static ProtocolMessageTypeEnum getEnumByKey(int key) {
        for(ProtocolMessageTypeEnum anEnum : ProtocolMessageTypeEnum.values()) {
            if(anEnum.key == key)
                return anEnum;
        }
        return null;
    }
}
