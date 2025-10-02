package com.cherry.velocityrpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.cherry.velocityrpc.config.RegistryConfig;
import com.cherry.velocityrpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
//import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutionException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Etcd 注册中心
 *
 * 注册中心核心能力：
 *   1. 数据分布式存储：集中的注册信息数据存储、读取和共享；
 *   2. 服务注册：服务提供者上报服务信息到注册中心；
 *   3. 服务发现：服务消费者从注册中心拉取服务信息；
 *   4. 心跳检测：定期检查服务提供者的存活状态；
 *   5. 服务注销：手动剔除节点、或者自动剔除失效节点；
 *   6. 更多优化点：比如注册中心本身的容错、服务消费者缓存等；
 * 技术选型：
 *   主流的注册中心实现中间件有ZooKeeper、Redis等。此处使用etcd
 *
 * Etcd：
 *   Etcd采用Raft一致性算法来保证数据的一致性和可靠性。Raft是一种分布式一致性算法，它确保了分布式系统中的所有节点在任何时间点都能达成一致的数据视图。
 *   Etcd层次化键值对存储数据，能够很灵活地单ky查询、按前缀查询、按范围查询。核心数据结构包括：
 *     - Key(键)：Etcd中的基本数据单元，类似于文件系统中的文件名。每个键都唯一标识一个值，并且可以包含子键，形成类似于路径的层次结构；
 *     - Value(值)：与键关联的数据，可以是任意类型的数据，通常是字符串形式。
 *   Etcd应用较多的核心特性：
 *     - Lease(租约)：用于对键值对进行TTL超时设置，即设置键值对的过期时间。当租约过期时，相关的键值对将被自动删除。
 *     - Watch(监听)：可以监视特定键的变化，当键的值发生变化时，会触发相应的通知。
 *   Etcd常用客户端：
 *     1. kvClient:用于对etcd中的键值对进行操作。通过kvClient可以进行设置值、获取值、删除值、列出目录等操作。
 *     2. leaseClient:用于管理etcd的租约机制。租约是etcd中的一种时间片，用于为键值对分配生存时间，并在租约到期时自动删除相关的键值对。通过leaseClient可以创建、获取、续约和撤销租约。
 *     3. watchClient:用于监视etcd中键的变化，并在键的值发生变化时接收通知。
 *   Etcd数据结构：
 *     key: "test_key"
 *     create_revision: 5
 *     mod_revision: 5
 *     version: 1
 *     value: "test_value"
 *     每个键都有一个与之关联的版本号，用于跟踪键的修改历史。当一个键的值发生变化时，其版本号也会增加。
 *     通过使用etcd的Watch API,可以监视键的变化，并在发生变化时接收通知。
 */
@Slf4j
public class EtcdRegistry implements Registry{
    // etcd测试
//    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        // 创建etcd客户端
//        Client client = Client.builder().endpoints("http://127.0.0.1:2379").build();
//        KV kvClient = client.getKVClient(); // 获取kv客户端
//        ByteSequence key = ByteSequence.from("test_key".getBytes()); // 将字符串转换为 etcd 所需的字节序列格式
//        ByteSequence value = ByteSequence.from("test_value".getBytes());
//
//        kvClient.put(key, value).get();
//        CompletableFuture<GetResponse> getFuture = kvClient.get(key);
//        GetResponse response = getFuture.get();
//        kvClient.delete(key).get();
//    }

    private Client client;
    private KV kvClient;
    // 本机注册的节点的key集合，用于心跳检测维护续期
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();
    // 使用注册中心的缓存服务
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    // 正在监听的key集合
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();
    // 根节点
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * etcd注册中心初始化
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();

        // 开启心跳检测
        heartBeat();
    }

    /**
     * 服务注册（服务端
     * @param serviceMetaInfo
     * @throws Exception
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建Lease和KV客户端
        Lease leaseClient = client.getLeaseClient();
        // 创建30s租约
        long leaseId = leaseClient.grant(30).get().getID();
        // 设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8); // JSON序列化
        // 将键值对与租约关联
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存
        localRegisterNodeKeySet.add(registerKey);
    }

    /**
     * 服务注销（服务端
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));

        // 从本地缓存中删除节点
        localRegisterNodeKeySet.remove(registerKey);
    }

    /**
     * 服务发现（消费端：根据服务名称作为前缀，从etcd中获取服务下的节点列表
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从注册中心的本地缓存中获取服务
        List<ServiceMetaInfo> serviceMetaInfoList = registryServiceCache.readCache();
        if(serviceMetaInfoList != null && serviceMetaInfoList.size() != 0) {
            log.info("从注册中心的本地缓存中获取到了服务");
            return serviceMetaInfoList;
        }

        // 本地缓存没有，再去注册中心拉取
        // 构建前缀搜索，结尾一定要加"/"
        log.info("从注册中心中获取服务");
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";
        try {
            // 前缀查询
            GetOption getOption = GetOption.builder().isPrefix(true).build();
            List<KeyValue> keyValues = kvClient.get(
                    ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                    getOption)
                .get()
                .getKvs();
            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfos = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);
                        // 监听key的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());

            // 写入到注册中心的服务本地缓存
            registryServiceCache.writeCache(serviceMetaInfos);

            return serviceMetaInfos;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    /**
     * 心跳检测，服务续签
     */
    @Override
    public void heartBeat() {
        // 设置的TTL是30s，所以10s续签一次，有1次容错机会
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的key
                for(String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        // 如果该节点已经过期，需要重启才能注册
                        if(CollUtil.isEmpty(keyValues)) {
                            continue;
                        }
                        // 节点未过期，重新注册
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 监听（消费端，何时更新服务缓存
     * @param serviceNodeKey
     */
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，现在开启监听
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if(newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for(WatchEvent event : response.getEvents()) {
                    switch (event.getEventType()) {
                        // key删除时触发更新，清理缓存
                        case DELETE:
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

    /**
     * 注册中心销毁
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线");

        //下线节点
        for(String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key,StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }

        // 释放资源
        if(kvClient != null) {
            kvClient.close();
        }
        if(client != null) {
            client.close();
        }
    }
}
