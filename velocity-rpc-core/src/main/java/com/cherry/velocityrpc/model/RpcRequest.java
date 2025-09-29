package com.cherry.velocityrpc.model;

import com.cherry.velocityrpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求处理器：处理接收到的请求，根据请求参数找到对应的服务和方法，通过反射实现调用，最后封装结果并响应请求
 * RpcRequest：封装调用所需要的信息，如：服务名称，方法名称，调用参数的类型列表，参数列表等。
 * 这都是java反射机制所需要的参数
 *
 * 处理RPC请求
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    private String serviceName; // 服务名称
    private String methodName; // 方法名称
    private Class<?>[] parameterTypes; // 参数类型列表
    private Object[] args; // 参数列表

    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION; // 服务版本（注册中心
}
