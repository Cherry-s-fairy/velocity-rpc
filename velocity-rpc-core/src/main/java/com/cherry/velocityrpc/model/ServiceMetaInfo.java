package com.cherry.velocityrpc.model;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 服务元信息（注册信息定义
 */
@Data
public class ServiceMetaInfo {
    private String serviceName; // 服务名称
    private String serviceVersion = "1.0"; // 服务版本号
    private String serviceHost; // 服务域名
    private Integer servicePort; // 服务端口号
    private String serviceGroup = "default"; // 服务分组（暂未实现

    /**
     * 获取服务键名
     * @return
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
        // 后续可扩展服务分组
//        return String.format("%s:%s:%s", serviceName, serviceVersion, serviceGroup);
    }

    /**
     * 获取服务注册节点键名
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost,servicePort);
    }

    /**
     * 获取完整服务地址
     * @return
     */
    public String getServiceAddress() {
        if(!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
