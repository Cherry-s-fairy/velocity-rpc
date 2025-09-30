package com.cherry.velocityrpc.server.tcp;

import io.vertx.core.Vertx;

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
                // 发送数据
                socket.write("Hello, server!");
                // 接收响应
                socket.handler(buffer -> {
                    System.out.println("Receive response from server : " + buffer.toString());
                });
            } else {
                System.out.println("Failed to connect to TCP server!");
            }
        });
    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }
}
