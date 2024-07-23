package com.zrb.rpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 本地注册中心
public class LocalRegistry {

    // 存储注册信息，key为服务名称，value为服务的实现类
    private static final Map<String,Class<?>> map = new ConcurrentHashMap<>();

    // 注册服务
    public static void register(String serviceName,Class<?> implClass){
        map.put(serviceName,implClass);
    }

    // 获取服务
    public static Class<?> get(String serviceName){
        return map.get(serviceName);
    }

    // 删除服务
    public static void remove(String serviceName){
        map.remove(serviceName);
    }
}
