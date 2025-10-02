package com.cherry.velocityrpc.loadbalancer;

import com.cherry.velocityrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer{
    private final Random random = new Random();

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        int size = serviceMetaInfoList.size();
        if(size == 0) {
            return null;
        } else if(size == 1) {
            return serviceMetaInfoList.get(0);
        } else {
            return serviceMetaInfoList.get(random.nextInt(size));
        }
    }
}
