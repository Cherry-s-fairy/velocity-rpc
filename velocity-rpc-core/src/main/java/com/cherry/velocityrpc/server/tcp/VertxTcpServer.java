package com.cherry.velocityrpc.server.tcp;

import com.cherry.velocityrpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
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
        server.connectHandler(socket -> {
            // 处理链接
            socket.handler(buffer -> {
                System.out.println("Receive datda from client : " + buffer.toString());
                // 处理接收到的字节数据
                byte[] requestData = buffer.getBytes();
                // 自定义处理逻辑，如解析请求，调用服务，构建响应等
                byte[] responseData = handleRequest(requestData);
                // 发送响应
                socket.write(Buffer.buffer(responseData));
            });
        });

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
