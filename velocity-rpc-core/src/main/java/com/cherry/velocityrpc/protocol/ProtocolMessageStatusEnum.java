package com.cherry.velocityrpc.protocol;

import lombok.Getter;

/**
 * 自定义协议的消息的状态枚举
 */
@Getter
public enum ProtocolMessageStatusEnum {
    OK("ok", 20), // 成功
    BAD_REQUEST("badRequest", 40), // 请求失败
    BAD_RESPONSE("badResponse", 50); // 响应失败

    private final String text;
    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for(ProtocolMessageStatusEnum anEnum : ProtocolMessageStatusEnum.values()) {
            if(anEnum.value == value)
                return anEnum;
        }
        return null;
    }
}
