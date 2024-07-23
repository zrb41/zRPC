package com.zrb.rpc.protocol;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// 协议消息的序列化器枚举
@Getter
@AllArgsConstructor
public enum ProtocolMessageSerializerEnum {

    JDK(0,"jdk"),
    JSON(1,"json"),
    KRYO(2,"kryo"),
    HESSIAN(3,"hessian");

    private final int key;
    private final String value;

    // 获取值列表
    public static List<String> getValues(){
        List<String> list = Arrays.stream(values())
                .map(anEnum -> anEnum.value)
                .collect(Collectors.toList());
        return list;
    }

    public static ProtocolMessageSerializerEnum getEnumByKey(int key){
        for(ProtocolMessageSerializerEnum anEnum:values()){
            if(anEnum.key==key){
                return anEnum;
            }
        }
        return null;
    }

    // 根据value获取枚举
    public static ProtocolMessageSerializerEnum getEnumByValue(String value){
        if(ObjectUtil.isEmpty(value)){
            return null;
        }
        for (ProtocolMessageSerializerEnum anEnum : values()) {
            if(anEnum.value.equals(value)){
                return anEnum;
            }
        }
        return null;
    }

}
