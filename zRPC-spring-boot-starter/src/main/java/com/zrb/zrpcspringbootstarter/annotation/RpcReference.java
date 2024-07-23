package com.zrb.zrpcspringbootstarter.annotation;

import com.zrb.rpc.constant.RpcConstant;
import com.zrb.rpc.fault.retry.RetryStrategyKeys;
import com.zrb.rpc.fault.tolerant.TolerantStrategyKeys;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 消费者注解，用于注入服务
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    // 服务接口类
    Class<?> interfaceClass() default void.class;

    // 版本
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    // 负载均衡器
    String retryStrategy() default RetryStrategyKeys.NO;

    // 容错策略
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_FAST;

    // 模拟调用
    boolean mock() default false;
}
