package com.zrb.rpc.fault.retry;

import com.github.rholder.retry.*;
import com.zrb.rpc.model.RpcResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

// 固定间隔重试策略
public class FixedIntervalRetryStrategy implements RetryStrategy {
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder
                .<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        System.out.println("重试次数" + attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }

}
