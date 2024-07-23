package com.zrb.rpc.fault.retry;

import com.zrb.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

// 重试策略
public interface RetryStrategy {
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
