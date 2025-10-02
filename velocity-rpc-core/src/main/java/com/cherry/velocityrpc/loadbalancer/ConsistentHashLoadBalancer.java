package com.cherry.velocityrpc.loadbalancer;

import com.cherry.velocityrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性Hash负载均衡器
 *
 * 每次调用负载均衡器时，都会重新构造Hash环，这是为了能够即时处理节点的变化
 */
public class ConsistentHashLoadBalancer implements LoadBalancer{
    // 一致性Hash环，存放虚拟节点
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();
    // 虚拟节点数
    private static final int VALUE_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // 构建虚拟节点环
        for(ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i=0; i<VALUE_NODE_NUM; i++) {
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }

        // 获取调用请求的hash值
        int hash = getHash(requestParams);
        // 选择最接近且>=调用请求hash值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        // 如果没有>=调用请求hash值的虚拟节点，则返回首部节点
        if(entry == null) {
            entry = virtualNodes.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * Hash算法
     * @param key
     * @return
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}
