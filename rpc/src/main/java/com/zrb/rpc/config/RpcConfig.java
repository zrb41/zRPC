package com.zrb.rpc.config;

import com.zrb.rpc.fault.retry.RetryStrategyKeys;
import com.zrb.rpc.fault.tolerant.TolerantStrategyKeys;
import com.zrb.rpc.loadbalancer.LoadBalancerKeys;
import com.zrb.rpc.serializer.SerializerKeys;
import lombok.Data;

// 配置类
@Data
public class RpcConfig {

    //private String name="zRPC";

    // 服务提供者 - 域名
    private String serverHost="localhost";

    // 服务提供者 - 端口号
    private Integer serverPort=8989;

    // 消费者 - 是否开启模拟调用
    private Boolean mock=false;

    // 消费者 & 服务提供者 - 序列化器
    private String serializer= SerializerKeys.JDK;

    // 消费者 & 服务提供者 - 注册中心配置
    private RegistryConfig registryConfig=new RegistryConfig();

    // 消费者 - 负载均衡器
    private String loadBalancer = LoadBalancerKeys.RANDOM;

    // 消费者 - 重试策略
    private String retryStrategy= RetryStrategyKeys.NO;

    // 消费者 - 容错策略
    private String tolerantStrategy= TolerantStrategyKeys.FAIL_FAST;

}
