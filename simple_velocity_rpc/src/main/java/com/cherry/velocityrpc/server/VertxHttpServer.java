package com.cherry.velocityrpc.server;

import io.vertx.core.Vertx;

/**
 * Vert.x Http 服务器
 */
public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();
        // 创建Http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        // 监听端口并处理请求
        server.requestHandler(request -> {
            // 处理http请求
            System.out.println("Receive Request: " + request.method() + " " + request.uri());
            // 发送http响应
            request.response()
                    .putHeader("content-type", "text/plain")
                    .end("Hello from Vert.x HTTP server!");
        });

        // 启动Http服务器并监听指定端口
        server.listen(port, result -> {
            if(result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.err.println("Failed to start server: " + result.cause());
            }
        });
    }
}
