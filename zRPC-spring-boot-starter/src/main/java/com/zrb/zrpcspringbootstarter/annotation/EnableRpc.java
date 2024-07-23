package com.zrb.zrpcspringbootstarter.annotation;

import com.zrb.zrpcspringbootstarter.bootstrap.RpcConsumerBootstrap;
import com.zrb.zrpcspringbootstarter.bootstrap.RpcInitBootstrap;
import com.zrb.zrpcspringbootstarter.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 启用 Rpc 注解
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {
    // 是否需要服务器
    boolean needServer() default true;
}
