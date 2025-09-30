package com.cherry.velocityrpc.protocol;

/**
 * 自定义协议常量类
 */
public interface ProtocolConstant {
    int MESSAGE_HEADER_LENGTH = 17; // 消息头长度
    byte PROTOCOL_MAGIC = 0x1; // 协议魔数
    byte PROTOCOL_VERSION = 0x1; // 协议版本号
}
