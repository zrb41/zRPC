package com.zrb.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProtocolMessageStatusEnum {

    OK("ok",20),
    BAD_REQUEST("badRequest",40),
    BAD_RESPONSE("badResponse",50);

    private final String text;
    private final int value;


    // 根据value获取枚举
    public static ProtocolMessageStatusEnum getEnumByValue(int value){
        for(ProtocolMessageStatusEnum anEnum:values()){
            if(anEnum.value==value){
                return anEnum;
            }
        }
        return null;
    }
}
