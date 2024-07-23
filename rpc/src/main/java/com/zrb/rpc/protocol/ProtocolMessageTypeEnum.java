package com.zrb.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 协议消息的类型枚举
@AllArgsConstructor
@Getter
public enum ProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int key;

    // 根据key获取枚举
    public static ProtocolMessageTypeEnum getEnumByKey(int key){
        for (ProtocolMessageTypeEnum anEnum : values()) {
            if(anEnum.key==key){
                return anEnum;
            }
        }
        return null;
    }
}
