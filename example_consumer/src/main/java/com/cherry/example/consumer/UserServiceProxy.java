package com.cherry.example.consumer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cherry.example.common.model.User;
import com.cherry.example.common.service.UserService;
import com.cherry.velocityrpc.model.RpcRequest;
import com.cherry.velocityrpc.model.RpcResponse;
import com.cherry.velocityrpc.serializer.JdkSerializer;
import com.cherry.velocityrpc.serializer.Serializer;

import java.io.IOException;

/**
 * 用户服务静态代理
 *
 * 实现UserService接口和getUser()方法，通过构造Http请求调用服务提供者
 * 注意：发送请求之前要将参数序列化
 *
 * 静态代理缺点：静态代理就是为特定类型的接口或对象写一个实现类，但若为每个服务接口都写一个实现类很麻烦！灵活性差！
 */
public class UserServiceProxy implements UserService {
    @Override
    public User getUser(User user) {
        // 指定序列化器
        final Serializer serializer = new JdkSerializer();

        // 构造请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try {
            // 序列化：java对象-->字节数组
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 发送请求
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                    .body(bodyBytes)
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();
                // 反序列化：字节数组-->java对象
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return (User) rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
