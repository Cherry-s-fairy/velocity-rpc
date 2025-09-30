package com.cherry.velocityrpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义协议消息结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {
    // 协议消息头
    @Data
    public static class Header {
        private byte magic; // 魔数，保证安全性
        private byte version; // 版本号
        private byte serializer; // 序列化器
        private byte type; // 消息类型（请求？响应？
        private byte status; // 状态
        private long requestId; // 请求id
        private int bodyLength; // 消息体长度
    }

    private Header header; // 消息头
    private T body; // 消息体（请求或响应对象
}
