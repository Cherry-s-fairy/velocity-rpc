package com.cherry.velocityrpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalRegistry {
    // 存储注册信息
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();
    // ConcurrentHashMap线程安全， key=服务名称，value=服务的实现类，可以根据要调用的服务名称获取他对应的实现类，通过反射进行方法调用

    /**
     * 注册服务
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass) {
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务
     * @param serviceName
     * @return
     */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * 删除服务
     * @param serviceName
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
