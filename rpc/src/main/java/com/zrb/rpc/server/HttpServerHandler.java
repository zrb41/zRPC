package com.zrb.rpc.server;

import com.zrb.rpc.RpcApplication;
import com.zrb.rpc.model.RpcRequest;
import com.zrb.rpc.model.RpcResponse;
import com.zrb.rpc.registry.LocalRegistry;
import com.zrb.rpc.serializer.Serializer;
import com.zrb.rpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

// 请求处理器
@Slf4j
public class HttpServerHandler implements Handler<HttpServerRequest> {
    @Override
    public void handle(HttpServerRequest request) {

        // 使用 工厂+读取配置文件 获取序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getConfig().getSerializer());

        // 请求体的处理
        request.bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer buffer) {
                byte[] bytes = buffer.getBytes();
                RpcRequest rpcRequest = null;
                try {
                    // 反序列化为rpcRequest
                    rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RpcResponse rpcResponse = new RpcResponse();
                if(rpcRequest==null){
                    rpcResponse.setMessage("rpcRequest is null");
                    doResponse(request,rpcResponse,serializer);
                    return;
                }

                try {
                    // 从本地注册中心获取要调用的服务的实现类
                    Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                    Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                    // 构建一个新的对象去执行
                    Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), rpcRequest.getArgs());
                    rpcResponse.setData(result);
                    rpcResponse.setDataType(method.getReturnType());
                    rpcResponse.setMessage("ok");
                } catch (Exception e) {
                    e.printStackTrace();
                    rpcResponse.setMessage(e.getMessage());
                    rpcResponse.setException(e);
                }

                doResponse(request,rpcResponse,serializer);
            }
        });


    }

    // 请求处理完毕，返回结果
    public void doResponse(HttpServerRequest request,RpcResponse rpcResponse,Serializer serializer){

        HttpServerResponse httpServerResponse = request.response().putHeader("content-type", "application/json");
        try {
            byte[] serializered = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serializered));
        } catch (Exception e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }

    }
}
