package com.zrb.rpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.zrb.rpc.config.RegistryConfig;
import com.zrb.rpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class EtcdRegistry implements Registry {

    // 客户端
    private Client client;

    // KV客户端
    private KV kvClient;

    // 根节点
    private static final String ETCD_ROOT_PATH="/rpc/";

    // 本机注册的节点key集合，用于维护&续期
    // 给服务提供者使用的
    private final Set<String> localRegisterNodeKeySet=new HashSet<>();

    // 注册中心服务缓存
    // 给消费者用的
    private final RegistryServiceCache registryServiceCache=new RegistryServiceCache();

    // 正在监听的key集合
    // 给消费者用的
    private final Set<String> watchingKeySet=new ConcurrentHashSet<>();

    @Override
    public void init(RegistryConfig registryConfig) {
        // 创建客户端和KV客户端
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();

        // 开启心跳检测
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建lease客户端
        Lease leaseClient = client.getLeaseClient();
        // 创建一个120秒的租约
        long leaseId = leaseClient.grant(120).get().getID();

        // 设置要存储的键值对
        String registerKey=ETCD_ROOT_PATH+serviceMetaInfo.getServiceNodeKey();
        // key = /rpc/服务名称:版本/服务域名:端口号
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        // value = serviceMetaInfo
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key,value,putOption).get();

        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey=ETCD_ROOT_PATH+serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey,StandardCharsets.UTF_8));
        // 将节点从本地缓存中移除
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    // 服务发现，给消费者用的
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {

        // 优先从缓存获取服务
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if(cachedServiceMetaInfoList!=null){
            return cachedServiceMetaInfoList;
        }

        // 前缀搜索，结尾一定要加'/'
        String searchPrefix=ETCD_ROOT_PATH+serviceKey+"/";

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get()
                    .getKvs();

            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key=keyValue.getKey().toString(StandardCharsets.UTF_8);
                        // 监听key的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());

            // 写入服务缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void destroy() {
        System.out.println("当前节点下线");
        for(String key:localRegisterNodeKeySet){
            try{
                kvClient.delete(ByteSequence.from(key,StandardCharsets.UTF_8)).get();
            }catch (Exception e){
                throw new RuntimeException("节点下线失败");
            }
        }

        // 关闭kv客户端
        if(kvClient!=null){
            kvClient.close();
        }
        // 关闭客户端
        if(client!=null){
            client.close();
        }
    }

    @Override
    public void heartBeat() {
        // 每10秒续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的key
                for(String key:localRegisterNodeKeySet){
                    try{
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        // 如果该节点已过期，需要重启节点后才能重写注册
                        if(CollUtil.isEmpty(keyValues)){
                            continue;
                        }
                        // 如果节点未过期，重新注册（续签）
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);

                    }catch (Exception e){
                        throw new RuntimeException("续签失败");
                    }
                }
            }
        });

        // 支持秒别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        // 获取watchClient客户端
        Watch watchClient = client.getWatchClient();
        // 之前还未监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if(newWatch){
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8)
                    , new Consumer<WatchResponse>() {
                        @Override
                        public void accept(WatchResponse watchResponse) {
                            for(WatchEvent event:watchResponse.getEvents()){
                                switch (event.getEventType()){
                                    // 删除时触发
                                    case DELETE:
                                        // 清理注册服务缓存
                                        registryServiceCache.clearCache();
                                        break;
                                    case PUT:
                                    default:
                                        break;
                                }
                            }
                        }
                    });
        }
    }
}
