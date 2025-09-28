package com.cherry.velocityrpc.server;

import com.cherry.velocityrpc.RpcApplication;
import com.cherry.velocityrpc.model.RpcRequest;
import com.cherry.velocityrpc.model.RpcResponse;
import com.cherry.velocityrpc.registry.LocalRegistry;
import com.cherry.velocityrpc.serializer.JdkSerializer;
import com.cherry.velocityrpc.serializer.Serializer;
import com.cherry.velocityrpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Http请求处理器
 *
 * 业务流程：
 *  1. 反序列化请求为对象，并从请求对象中获取参数
 *  2. 根据服务名称从本地服务器中获取对应的服务实现类
 *  3. 通过反射机制调用方法，得到返回结果
 *  4. 对结果进行封装和序列化，并写入到响应中
 */

// Vert.x中通过实现Handler<HttpServerRequest>接口来自定义请求处理器的。并且可以通过request.bodyHandler异步处理请求。
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        // 记录日志
        System.out.println("Received request: " + request.method() + " " + request.uri());

        // 异步处理Http请求
        request.bodyHandler(body -> {
            // 处理响应
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 构造响应返回结果
            RpcResponse rpcResponse = new RpcResponse();
            // 如果请求为null直接返回
            if(rpcRequest == null) {
                rpcResponse.setMessage("RPC request is null!");
                doResponse(request, rpcResponse, serializer);
                return;
            }
            try {
                // 获取要调用的服务的实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            // 响应
            doResponse(request, rpcResponse, serializer);
        });
    }

    /**
     * 响应
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");
        try {
            // 序列化
            byte[] serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
