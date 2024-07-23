package com.zrb.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import com.zrb.rpc.RpcApplication;
import com.zrb.rpc.config.RegistryConfig;
import com.zrb.rpc.config.RpcConfig;
import com.zrb.rpc.constant.RpcConstant;
import com.zrb.rpc.fault.retry.RetryStrategy;
import com.zrb.rpc.fault.retry.RetryStrategyFactory;
import com.zrb.rpc.fault.tolerant.TolerantStrategy;
import com.zrb.rpc.fault.tolerant.TolerantStrategyFactory;
import com.zrb.rpc.loadbalancer.LoadBalancer;
import com.zrb.rpc.loadbalancer.LoadBalancerFactory;
import com.zrb.rpc.model.RpcRequest;
import com.zrb.rpc.model.RpcResponse;
import com.zrb.rpc.model.ServiceMetaInfo;
import com.zrb.rpc.registry.Registry;
import com.zrb.rpc.registry.RegistryFactory;
import com.zrb.rpc.serializer.Serializer;
import com.zrb.rpc.serializer.SerializerFactory;
import com.zrb.rpc.server.tcp.VertxTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public class ServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 使用 工厂+读取配置文件 获取序列化器
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getConfig().getSerializer());

        // 构造rpc请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 将rpc请求序列化
        byte[] bodyBytes = serializer.serialize(rpcRequest);

        RpcResponse rpcResponse=null;
        try {
            // 从注册中心获取服务提供者的地址
            RpcConfig rpcConfig = RpcApplication.getConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            // 获取注册中心
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            // 服务发现 - 前缀搜索
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }

            // 获取负载均衡器
            LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(RpcApplication.getConfig().getLoadBalancer());
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("methodName",rpcRequest.getMethodName());
            // 根据负载均衡器获取服务
            ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
            //System.out.println("选择的服务提供者是"+selectedServiceMetaInfo.toString());

            // 发送TCP请求
            // 获取重试策略
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(RpcApplication.getConfig().getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(new Callable<RpcResponse>() {
                @Override
                public RpcResponse call() throws Exception {
                    return VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
                }
            });

            // hutool工具包下的HttpRequest
            // 发送HTTP请求
            /*
            HttpResponse httpResponse = HttpRequest
                    .post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bodyBytes)
                    .execute();

            // 将响应结果反序列化为rpcResponse
            byte[] bytes = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserialize(bytes, RpcResponse.class);
             */

        } catch (Exception e) {
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(RpcApplication.getConfig().getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }
        return rpcResponse.getData();
    }
}
