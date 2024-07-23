package com.zrb.rpc.fault.tolerant;

import com.zrb.rpc.model.RpcResponse;

import java.util.Map;

public interface TolerantStrategy {
    RpcResponse doTolerant(Map<String,Object> context,Exception e);
}
