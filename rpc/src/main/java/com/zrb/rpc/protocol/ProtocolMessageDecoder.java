package com.zrb.rpc.protocol;

import com.zrb.rpc.model.RpcRequest;
import com.zrb.rpc.model.RpcResponse;
import com.zrb.rpc.serializer.Serializer;
import com.zrb.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

// 解码器 - 将Buffer对象转化为ProtocolMessage对象
public class ProtocolMessageDecoder {

    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {

        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        // 校验魔数
        if (magic!=ProtocolConstant.PROTOCOL_MAGIC){
            throw new RuntimeException("magic非法");
        }
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getLong(5));
        header.setBodyLength(buffer.getInt(13));
        // 解决粘包问题，读取指定长度的数据
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        // 反序列化消息体
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if(serializerEnum==null){
            throw new RuntimeException("ProtocolMessageSerializerEnum不存在");
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum messageTypeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if(messageTypeEnum==null){
            throw new RuntimeException("序列化消息的类型不存在");
        }
        switch (messageTypeEnum) {
            case REQUEST:
                RpcRequest request = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, request);
            case RESPONSE:
                RpcResponse response = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, response);
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("暂不支持该类型的消息");
        }
    }


}
