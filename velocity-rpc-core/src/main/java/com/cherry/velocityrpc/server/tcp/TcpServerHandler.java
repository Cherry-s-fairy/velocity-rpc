package com.cherry.velocityrpc.server.tcp;

import com.cherry.velocityrpc.model.RpcRequest;
import com.cherry.velocityrpc.model.RpcResponse;
import com.cherry.velocityrpc.protocol.ProtocolMessage;
import com.cherry.velocityrpc.protocol.ProtocolMessageDecoder;
import com.cherry.velocityrpc.protocol.ProtocolMessageEncoder;
import com.cherry.velocityrpc.protocol.ProtocolMessageTypeEnum;
import com.cherry.velocityrpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Tcp 请求处理器
 */
public class TcpServerHandler implements Handler<NetSocket> {
    @Override
    public void handle(NetSocket netSocket) {
        // 处理链接
        netSocket.handler(buffer -> {
            // 接受请求并解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (Exception e) {
                throw new RuntimeException("协议消息解码失败！", e);
            }
            RpcRequest request = protocolMessage.getBody();

            // 处理请求并构造响应结果对象
            RpcResponse response = new RpcResponse();
            try {
                // 获取要调用的服务实现类
                Class<?> implClass = LocalRegistry.get(request.getServiceName());
                Method method = implClass.getMethod(request.getMethodName(), request.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), request.getArgs());

                // 封装返回结果
                response.setData(result);
                response.setDataType(method.getReturnType());
                response.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                response.setMessage(e.getMessage());
                response.setException(e);
            }

            // 发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, response);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码失败！");
            }
        });
    }
}
