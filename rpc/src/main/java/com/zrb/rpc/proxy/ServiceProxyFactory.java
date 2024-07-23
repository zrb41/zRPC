package com.zrb.rpc.proxy;

import com.zrb.rpc.RpcApplication;

import java.lang.reflect.Proxy;

// 工厂方法，用于获取代理对象
public class ServiceProxyFactory {
    // 根据服务类获取代理对象
    public static <T> T getProxy(Class<T> serviceClass){

        // 如果是模拟调用
        if(RpcApplication.getConfig().getMock()){
            return getMockProxy(serviceClass);
        }

        return (T)Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy()
        );
    }

    public static <T> T getMockProxy(Class<T> serviceClass){
        return (T)Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new MockServiceProxy()
        );
    }


}
