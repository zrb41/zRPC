package com.zrb.rpc.server.tcp;

import com.zrb.rpc.server.HttpServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

// TCP服务端
public class VertxTcpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();
        // 创建TCP服务器
        NetServer server = vertx.createNetServer();
        // 处理请求
        server.connectHandler(new TcpServerHandler());

        // 启动TCP服务器并监听指定端口
        server.listen(port, new Handler<AsyncResult<NetServer>>() {
            @Override
            public void handle(AsyncResult<NetServer> result) {
                if(result.succeeded()){
                    System.out.println("TCP server started on port"+port);
                }else{
                    System.err.println("Failed to start TCP server"+result.cause());
                }
            }
        });
    }

}
