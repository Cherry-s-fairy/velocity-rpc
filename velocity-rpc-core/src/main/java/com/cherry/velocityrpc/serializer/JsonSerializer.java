package com.cherry.velocityrpc.serializer;

import com.cherry.velocityrpc.model.RpcRequest;
import com.cherry.velocityrpc.model.RpcResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Json 序列化器
 */
public class JsonSerializer implements Serializer{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 序列化
     * @param object
     * @return
     * @param <T>
     * @throws IOException
     */
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    /**
     * 反序列化
     * @param bytes
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T object = OBJECT_MAPPER.readValue(bytes, type);
        if(object instanceof RpcRequest) {
            return handleRequest((RpcRequest) object, type);
        }
        if(object instanceof RpcResponse) {
            return handleResponse((RpcResponse) object, type);
        }
        return object;
    }

    /**
     * 由于 Object 的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换成原始对象，因此这里做了特殊处理
     * @param rpcRequest rpc请求
     * @param type 类型
     * @return {@link T}
     * @param <T>
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes(); // 方法参数类型数组
        Object[] args = rpcRequest.getArgs(); // 方法参数数组

        // 循环处理每个参数的类型
        for(int i=0; i<parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            // 如果类型不同，则重新处理下一个类型
            if(!clazz.isAssignableFrom(args[i].getClass())) { // 参数和类型的兼容性检查
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]); // 讲args[i]序列化为json数组
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz); // 讲json数组转回args[i]对象
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     * 由于 Object 的原始对象会被擦除，导致反序列化时会被作为 LinkedHashMap 无法转换成原始对象，因此这里做了特殊处理
     * @param rpcResponse
     * @param type
     * @return
     * @param <T>
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        // 处理响应数据
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }
}
