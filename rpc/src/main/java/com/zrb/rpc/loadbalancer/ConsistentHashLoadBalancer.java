package com.zrb.rpc.loadbalancer;

import com.zrb.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// 一致性Hash负载均衡器
public class ConsistentHashLoadBalancer implements LoadBalancer{

    // 一致性Hash环，存放虚拟节点
    private final TreeMap<Integer,ServiceMetaInfo> virtualNode=new TreeMap<>();

    // 虚拟节点个数
    private static final int VIRTUAL_NODE_NUM=100;
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList.size()==0){
            return null;
        }

        // 构建虚拟节点环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for(int i=0;i<VIRTUAL_NODE_NUM;i++){
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNode.put(hash,serviceMetaInfo);
            }
        }

        // 获取调用请求的hash值
        // 相同的请求总会达到同一台服务器上
        int hash = getHash(requestParams);

        // 选择最接近且大于等于调用请求hash值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNode.ceilingEntry(hash);
        if(entry==null){
            // 如果没有大于等于调用请求hash值的虚拟节点，则返回环首部的节点
            entry=virtualNode.firstEntry();
        }
        return entry.getValue();

    }

    // hash算法
    private int getHash(Object key){
        return key.hashCode();
    }
}
