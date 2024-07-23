package com.zrb.rpc.server;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        Vertx vertx = Vertx.vertx();
        // 创建http服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();
        // 设置请求处理
        server.requestHandler(new HttpServerHandler());
        // 启动服务器并监听端口
        server.listen(port, new Handler<AsyncResult<io.vertx.core.http.HttpServer>>() {
            @Override
            public void handle(AsyncResult<io.vertx.core.http.HttpServer> result) {
                if(result.succeeded()){
                    System.out.println("Server is now listening on port: "+ port);
                }else{
                    System.err.println("Failed to start server"+result.cause());
                }
            }
        });

    }
}
