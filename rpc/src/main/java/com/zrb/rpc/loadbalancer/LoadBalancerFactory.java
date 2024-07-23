package com.zrb.rpc.loadbalancer;

import com.zrb.rpc.spi.SpiLoader;

// 负载均衡工厂
public class LoadBalancerFactory {

    static {
        SpiLoader.load(LoadBalancer.class);
    }

    // 默认负载均衡器
    private static final LoadBalancer DEFAULT_LOAD_BALANCER=new RoundRobinLoadBalancer();

    // 获取实例
    public static LoadBalancer getInstance(String key){
        return SpiLoader.getInstance(LoadBalancer.class,key);
    }
}
