package com.cherry.velocityrpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求处理器：处理接收到的请求，根据请求参数找到对应的服务和方法，通过反射实现调用，最后封装结果并响应请求
 * RpcResponse：封装调用方法得到的返回值，以及调用信息（如异常情况）等。
 *
 * Rpc响应
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable {
    private Object data; // 响应数据
    private Class<?> dataType; // 响应数据类型
    private String message; // 响应信息
    private Exception exception; // 异常信息
}
