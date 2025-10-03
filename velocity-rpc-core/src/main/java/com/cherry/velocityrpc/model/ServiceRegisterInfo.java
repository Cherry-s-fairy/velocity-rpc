package com.cherry.velocityrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务注册信息类
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegisterInfo<T> {
    private String serviceName; // 服务名称
    private Class<? extends T> implClass; // 实现类
}
