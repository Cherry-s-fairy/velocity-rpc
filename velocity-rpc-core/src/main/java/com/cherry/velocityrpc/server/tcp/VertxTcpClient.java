package com.cherry.velocityrpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.cherry.velocityrpc.RpcApplication;
import com.cherry.velocityrpc.model.RpcRequest;
import com.cherry.velocityrpc.model.RpcResponse;
import com.cherry.velocityrpc.model.ServiceMetaInfo;
import com.cherry.velocityrpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Vertx TCP 请求客户端
 */
public class VertxTcpClient {
    /**
     * TCP通信测试
     */
    public void start() {
        // 创建verts实例
        Vertx vertx = Vertx.vertx();

        vertx.createNetClient().connect(8888, "localhost", result -> {
            if(result.succeeded()) {
                System.out.println("Connected to Tcp server");
                io.vertx.core.net.NetSocket socket = result.result();
//                // 发送数据
//                socket.write("Hello, server!");
                // 测试粘包和半包
                for(int i=0; i<1000; i++) {
                    socket.write("Hello server1! Hello server2! Hello server3!");

//                    // 发送变长数据测试
//                    Buffer buffer = Buffer.buffer();
//                    String str = "Hello server";
//                    buffer.appendInt(0);
//                    buffer.appendInt(str.getBytes().length);
//                    buffer.appendBytes(str.getBytes());
//                    socket.write(buffer);
                }
                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("Receive response from server : " + buffer.toString());
                });
            } else {
                System.out.println("Failed to connect to TCP server!");
            }
        });
    }

    public static RpcResponse doRequest(RpcRequest request, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        // 发送tcp请求
         Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(), result -> {
            if(!result.succeeded()) {
                System.err.println("Failed to connect to tcp server!");
                return;
            }

            System.out.println("Connected to tcp server.");
            NetSocket socket = result.result();
            // 构造消息并发送
            ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
            header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
            header.setRequestId(IdUtil.getSnowflakeNextId()); // 生成全局请求id
            protocolMessage.setHeader(header);
            protocolMessage.setBody(request);

            // 将请求编码，以传输
            try {
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                socket.write(encodeBuffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误！");
            }

            // 接收响应
            TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                try {
                    ProtocolMessage<RpcResponse> responseProtocolMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                    responseFuture.complete(responseProtocolMessage.getBody());
                } catch (IOException e) {
                    throw new RuntimeException("协议消息解码错误！");
                }
            });

            socket.handler(bufferHandlerWrapper);
        });
        RpcResponse rpcResponse = responseFuture.get();
        // 关闭连接
        netClient.close();
        return rpcResponse;
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
