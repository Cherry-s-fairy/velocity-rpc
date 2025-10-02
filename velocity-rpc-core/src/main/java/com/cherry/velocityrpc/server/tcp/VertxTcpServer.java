package com.cherry.velocityrpc.server.tcp;

import com.cherry.velocityrpc.server.HttpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Vertx TCP 服务器
 */
@Slf4j
public class VertxTcpServer implements HttpServer {
    private byte[] handleRequest(byte[] requestData) {
        // 在这里编写处理请求的逻辑，根据requestData构造响应数据并返回
        // 这里只是一个示例，实际逻辑需要根据具体的业务需求来实现
        return "Hello, client".getBytes();
    }

    @Override
    public void doStart(int port) {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();
        // 创建TCP服务器
        NetServer server = vertx.createNetServer();

        // 处理请求
        server.connectHandler(new TcpServerHandler());
//        // 处理请求
//        server.connectHandler(socket -> {
//            String testMessage = "Hello server1! Hello server2! Hello server3!";
//            int messageLength = testMessage.length();
//            // 构造parser
//            RecordParser parser = RecordParser.newFixed(messageLength); // 为Parser指定每次读取固定值长度的内容
//            parser.setOutput(new Handler<Buffer>() {
//                @Override
//                public void handle(Buffer buffer) {
//                    String str = new String(buffer.getBytes());
//                    System.out.println(str);
//                    if(testMessage.equals(str)) {
//                        System.out.println("Good!");
//                    }
//                }
//            });

//            // 先固定header，再从header中得到bodyLength
//            RecordParser parser = RecordParser.newFixed(8);
//            parser.setOutput(new Handler<Buffer>() {
//                // 初始化
//                int size = -1;
//                Buffer resultBuffer = Buffer.buffer();
//                @Override
//                public void handle(Buffer buffer) {
//                    if(size == -1) {
//                        // 读取消息体长度
//                        size = buffer.getInt(4);
//                        parser.fixedSizeMode(size);
//                        // 写入头信息到结果
//                        resultBuffer.appendBuffer(buffer);
//                    } else {
//                        // 写入体信息到结果
//                        resultBuffer.appendBuffer(buffer);
//                        System.out.println(resultBuffer.toString());
//                        // 重置一轮
//                        parser.fixedSizeMode(8);
//                        size = -1;
//                        resultBuffer = Buffer.buffer();
//                    }
//
//                }
//            });

//            socket.handler(parser);

//            // 处理链接
//            socket.handler(buffer -> {
//                System.out.println("Receive datda from client : " + buffer.toString());
//                // 处理接收到的字节数据
//                byte[] requestData = buffer.getBytes();
//                // 自定义处理逻辑，如解析请求，调用服务，构建响应等
//                byte[] responseData = handleRequest(requestData);
//                // 发送响应
//                socket.write(Buffer.buffer(responseData));
//            });
//        });


        // 启动TCP服务器并监听指定端口
        server.listen(port, result -> {
            if(result.succeeded()) {
                log.info("TCP server started on port " + port);
            } else {
                log.error("Failed to start TCP server : " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpServer().doStart(8888);
    }
}
