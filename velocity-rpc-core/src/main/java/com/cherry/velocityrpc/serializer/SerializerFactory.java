package com.cherry.velocityrpc.serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化器工厂（工厂模式+单例模式）
 *
 * 序列化器对象可以复用，而不必每次执行序列化操作前都构建一个新的对象
 */
public class SerializerFactory {
    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<>() {{
        put(SerializerKeys.JDK, new JdkSerializer());
        put(SerializerKeys.JSON, new JsonSerializer());
        put(SerializerKeys.KRYO, new KryoSerializer());
        put(SerializerKeys.HESSIAN, new HessianSerializer());
    }};

    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer(); // 默认序列化器

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
    }
}
