package com.zrb.zrpcspringbootstarter.bootstrap;

import com.zrb.rpc.RpcApplication;
import com.zrb.rpc.server.tcp.VertxTcpServer;
import com.zrb.zrpcspringbootstarter.annotation.EnableRpc;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

// Rpc框架启动
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取EnableRpc注解的属性值
        boolean needServer = (boolean)importingClassMetadata
                .getAnnotationAttributes(EnableRpc.class.getName())
                .get("needServer");

        // RPC框架初始化
        RpcApplication.init();

        //启动服务器
        if(needServer){
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(RpcApplication.getConfig().getServerPort());
        }else{
            System.out.println("不启动服务器");
        }

    }
}
