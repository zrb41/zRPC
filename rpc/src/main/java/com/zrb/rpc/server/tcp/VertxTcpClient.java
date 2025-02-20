package com.zrb.rpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.zrb.rpc.RpcApplication;
import com.zrb.rpc.model.RpcRequest;
import com.zrb.rpc.model.RpcResponse;
import com.zrb.rpc.model.ServiceMetaInfo;
import com.zrb.rpc.protocol.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// TCP客户端 - 发送TCP请求
public class VertxTcpClient {
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws InterruptedException, ExecutionException {

        Vertx vertx = Vertx.vertx();
        // 获取TCP客户端
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        // 发送TCP请求
        netClient.connect(serviceMetaInfo.getServicePort(),
                serviceMetaInfo.getServiceHost(),
                new Handler<AsyncResult<NetSocket>>() {
                    @Override
                    public void handle(AsyncResult<NetSocket> result) {
                        if (!result.succeeded()) {
                            System.err.println("Failed to connect to TCP server");
                            return;
                        }
                        NetSocket socket = result.result();
                        // 发送数据
                        // 构造消息
                        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                        header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getConfig().getSerializer()).getKey());
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                        // 生成全局请求 ID
                        header.setRequestId(IdUtil.getSnowflakeNextId());
                        protocolMessage.setHeader(header);
                        protocolMessage.setBody(rpcRequest);

                        // 编码请求
                        try {
                            Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                            socket.write(encodeBuffer);
                        } catch (IOException e) {
                            throw new RuntimeException("协议消息编码错误");
                        }

                        // 接收TCP响应
                        TcpBufferHandlerWrapper bufferHandlerWrapper = new TcpBufferHandlerWrapper(
                                new Handler<Buffer>() {
                                    @Override
                                    public void handle(Buffer buffer) {
                                        try {
                                            ProtocolMessage<RpcResponse> rpcResponseProtocolMessage =
                                                    (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                            responseFuture.complete(rpcResponseProtocolMessage.getBody());
                                        } catch (IOException e) {
                                            throw new RuntimeException("协议消息解码错误");
                                        }
                                    }
                                }
                        );
                        socket.handler(bufferHandlerWrapper);
                    }
                });

        RpcResponse rpcResponse = responseFuture.get();
        // 记得关闭连接
        netClient.close();
        return rpcResponse;
    }
}


