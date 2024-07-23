package com.zrb.rpc.registry;

import com.zrb.rpc.config.RegistryConfig;
import com.zrb.rpc.model.ServiceMetaInfo;

import java.util.List;

public interface Registry {

    // 初始化
    void init(RegistryConfig registryConfig);

    // 服务提供者: 注册服务
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    // 服务提供者: 注销服务
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    // 消费者: 发现服务
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    // 服务销毁
    void destroy();

    // 服务提供者: 心跳检测
    void heartBeat();

    // 消费端: 监测
    void watch(String serviceNodeKey);
}
