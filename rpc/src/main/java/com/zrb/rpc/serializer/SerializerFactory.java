package com.zrb.rpc.serializer;

import com.zrb.rpc.spi.SpiLoader;

// 获取序列化器的工厂
public class SerializerFactory {

    // 静态代码块
    // 加载Serializer接口的所有实现类
    static {
        SpiLoader.load(Serializer.class);
    }

    // 默认序列化器
    private static final Serializer DEFAULT_SERIALIZER=new JdkSerializer();

    public static  Serializer getInstance(String key){
        return SpiLoader.getInstance(Serializer.class,key);
    }

}
