package com.zrb.rpc;

import com.zrb.rpc.config.RegistryConfig;
import com.zrb.rpc.config.RpcConfig;
import com.zrb.rpc.constant.RpcConstant;
import com.zrb.rpc.registry.Registry;
import com.zrb.rpc.registry.RegistryFactory;
import com.zrb.rpc.utils.ConfigUtils;

// Rpc框架启动的入口
// 只要导了zRPC框架的maven坐标，任何项目都可以是使用
public class RpcApplication {

    // 配置文件
    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig newRpcConfig){
        rpcConfig=newRpcConfig;
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);

        // 创建并注册shutdown Hook，JVM退出时执行操作
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    public static void init(){
        // 假如A项目调用了该方法，则加载A项目中的配置文件
        // 而不是加载zRPC项目中的配置文件
        RpcConfig newRpcConfig;
        try{
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        }catch (Exception e){
            // 配置加载失败，使用默认值
            newRpcConfig=new RpcConfig();
        }
        init(newRpcConfig);
    }

    // 双锁机制 - 单例模式
    public static RpcConfig getConfig(){
        // double check lock
        if(rpcConfig==null){
            synchronized (RpcApplication.class){
                if(rpcConfig==null){
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
