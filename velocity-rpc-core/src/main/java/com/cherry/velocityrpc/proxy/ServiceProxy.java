package com.cherry.velocityrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cherry.velocityrpc.RpcApplication;
import com.cherry.velocityrpc.config.RpcConfig;
import com.cherry.velocityrpc.constant.RpcConstant;
import com.cherry.velocityrpc.loadbalancer.LoadBalancer;
import com.cherry.velocityrpc.loadbalancer.LoadBalancerFactory;
import com.cherry.velocityrpc.model.RpcRequest;
import com.cherry.velocityrpc.model.RpcResponse;
import com.cherry.velocityrpc.model.ServiceMetaInfo;
import com.cherry.velocityrpc.protocol.*;
import com.cherry.velocityrpc.registry.Registry;
import com.cherry.velocityrpc.registry.RegistryFactory;
import com.cherry.velocityrpc.serializer.Serializer;
import com.cherry.velocityrpc.serializer.SerializerFactory;
import com.cherry.velocityrpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 服务代理 (JDK 动态代理)
 *
 * 动态代理：根据要生成的对象的类型，自动生成一个代理对象。常用实现方式：
 *   1. JDK动态代理：只对接口进行代理，但性能好；
 *   2. 基于字节码生成的动态代理，如CGLB：可以代理任何类型，性能略低于JDK动态代理。
 *
 * 当用户调用某个接口的方法时，会改为调用invoke方法。再invoke方法中获取要调用的方法信息、参数列表等，以此构造Rpc请求对象。
 */
public class ServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
         final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        /**
         * 1. HttpServer版本
         */
//        try {
//            // 序列化
//            byte[] bodyBytes = serializer.serialize(rpcRequest);
//            // 从注册中心获取服务提供者的请求地址
//            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
//            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
//            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
//            serviceMetaInfo.setServiceName(serviceName);
//            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
//            List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
//            if(CollUtil.isEmpty(serviceMetaInfos)) {
//                throw new RuntimeException("暂无服务地址");
//            }
//            // 暂时先获取第一个
//            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfos.get(0);
//            // 发送请求
//            try(HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
//                    .body(bodyBytes)
//                    .execute()) {
//                byte[] result = httpResponse.bodyBytes();
//                // 反序列化
//                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
//                return rpcResponse.getData();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        /**
         * 2. 自定义协议之Tcp版本
         */
        // 从注册中心获取提供者请求地址
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry()); // 获取注册中心
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if(serviceMetaInfos == null) {
            throw new RuntimeException("暂无服务地址");
        }
//        // 暂时先获取第一个服务（后面实现负载均衡
//        ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfos.get(0);

        // 负载均衡获取服务信息
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        // 将调用方法名（请求路径）作为负载均衡参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfos);

        // 解决粘包和半包
        RpcResponse response = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
        System.out.println(serviceMetaInfos.get(0).toString());
        return response.getData();
//        // 发送tcp（rpc）请求
//        Vertx vertx = Vertx.vertx();
//        NetClient netClient = vertx.createNetClient();
//        CompletableFuture<Object> responseFuture = new CompletableFuture<>(); // 转Vert.x的异步为同步
//
//        netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceHost(), result -> {
//            if(result.succeeded()) {
//                System.out.println("Connected to tcp server.");
//                io.vertx.core.net.NetSocket socket = result.result();
//                // 发送数据，构造消息
//                ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>(); // 协议消息
//                ProtocolMessage.Header header = new ProtocolMessage.Header(); // 消息头
//                // 构造消息头
//                header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
//                header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
//                header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
//                header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
//                header.setRequestId(IdUtil.getSnowflakeNextId());
//                // 合成消息
//                protocolMessage.setHeader(header);
//                protocolMessage.setBody(rpcRequest);
//                // 对消息进行编码
//                try {
//                    Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
//                    socket.write(encodeBuffer);
//                } catch (Exception e) {
//                    throw new RuntimeException("协议消息编码失败！");
//                }
//
//                // 接收响应
//                socket.handler(buffer -> {
//                    try {
//                        // 响应完成
//                        ProtocolMessage<RpcResponse> responseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
//                        responseFuture.complete(responseProtocolMessage.getBody());
//                    } catch (IOException e) {
//                        throw new RuntimeException("协议消息解码失败！");
//                    }
//                });
//            } else {
//                System.err.println("Failed to connect to Tcp server.");
//            }
//        });
//        // 构造响应结果，阻塞，直到响应完成才会继续向下执行
//        RpcResponse response = (RpcResponse) responseFuture.get();
//        // 关闭连接
//        netClient.close();
//        // 返回响应
//        return response.getData();
    }
}
