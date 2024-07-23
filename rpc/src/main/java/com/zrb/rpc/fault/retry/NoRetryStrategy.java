package com.zrb.rpc.fault.retry;

import com.zrb.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

// 不重试策略
public class NoRetryStrategy implements RetryStrategy{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
