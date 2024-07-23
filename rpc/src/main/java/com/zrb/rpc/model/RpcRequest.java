package com.zrb.rpc.model;

import com.zrb.rpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// 请求处理器
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest implements Serializable {

    // 服务名称 - 接口的全限定类名
    private String serviceName;

    // 方法名称 - 接口中的方法
    private String methodName;

    // 服务版本
    private String serviceVersion= RpcConstant.DEFAULT_SERVICE_VERSION;

    // 参数类型 -方法的参数类型
    private Class<?>[] parameterTypes;

    // 参数列表 - 方法中的参数
    private Object[] args;
}
