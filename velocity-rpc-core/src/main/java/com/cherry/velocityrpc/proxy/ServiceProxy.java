package com.cherry.velocityrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cherry.velocityrpc.RpcApplication;
import com.cherry.velocityrpc.config.RpcConfig;
import com.cherry.velocityrpc.constant.RpcConstant;
import com.cherry.velocityrpc.model.RpcRequest;
import com.cherry.velocityrpc.model.RpcResponse;
import com.cherry.velocityrpc.model.ServiceMetaInfo;
import com.cherry.velocityrpc.registry.Registry;
import com.cherry.velocityrpc.registry.RegistryFactory;
import com.cherry.velocityrpc.serializer.Serializer;
import com.cherry.velocityrpc.serializer.JdkSerializer;
import com.cherry.velocityrpc.serializer.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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

        String serviceName = method.getDeclaringClass().getName();

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 从注册中心获取服务提供者的请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfos)) {
                throw new RuntimeException("暂无服务地址");
            }
            // 暂时先获取第一个
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfos.get(0);
            // 发送请求
            // todo 注意，这里的地址是硬编码，需要使用注册中心和服务发现机制解决
            try(HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
