package com.zrb.rpc.bootstrap;

import com.zrb.rpc.RpcApplication;
import com.zrb.rpc.config.RegistryConfig;
import com.zrb.rpc.config.RpcConfig;
import com.zrb.rpc.model.ServiceMetaInfo;
import com.zrb.rpc.model.ServiceRegisterInfo;
import com.zrb.rpc.registry.LocalRegistry;
import com.zrb.rpc.registry.Registry;
import com.zrb.rpc.registry.RegistryFactory;
import com.zrb.rpc.server.tcp.VertxTcpServer;

import java.util.List;

// 服务提供者初始化
public class ProviderBootstrap {

    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {
        // RPC 框架初始化（配置和注册中心）
        RpcApplication.init();
        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getConfig();

        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {

            // 服务名称
            String serviceName = serviceRegisterInfo.getServiceName();
            // 服务实现类
            Class<?> implClass = serviceRegisterInfo.getImplClass();

            // 本地注册
            LocalRegistry.register(serviceName, implClass);

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            // 获取注册中心
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        // 启动TCP服务器
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
    }
}
