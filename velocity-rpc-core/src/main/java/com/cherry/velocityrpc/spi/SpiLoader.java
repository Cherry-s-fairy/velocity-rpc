package com.cherry.velocityrpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.cherry.velocityrpc.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI加载器（自定义实现，支持键值对映射
 *
 * 自定义序列化器，使用SPI机制实现，支持用户自定义序列化器并指定键名。
 * SPI：允许服务提供者通过特定的配置文件将自己的实现注册到系统中，然后系统通过反射机制动态加载这些实现，而不需要修改原始框架的代码，从而实现了系统的解耦、提高了可扩展性。
 * 自定义SPI机制：目的就是读取配置文件，能够得到一个序列化器名称=>序列化器实现类对象的映射
 *
 * 自定义序列化器实现：
 *   1. 指定SPI配置目录
 *      系统内置的SPI机制读取META-INF/rpc目录，再分为系统内置SPI和用户自定义SPI,即目录如下（都存放在resources目录下：
 *      - 用户自定义SPI：META-NF/rpc/custom。用户可以在该目录下新建配置，加载自定义的实现类。
 *      - 系统内置SPI：META-INF/rpc/system。RPC框架自带的实现类，比如我们之前开发好的JdkSerializer。
 *      由此，所有接口的实现类都可以通过SPI动态加载，不用在代码中硬编码Map来维护实现类了。
 *   2. 编写spi加载器 SpiLoader
 *     相当于一个工具类，提供了读取配置并加载实现类的方法。
 *     a. 用Map来存储已加载的配置信息键名=>实现类。
 *     b. 扫描指定路径，读取每个配置文件，获取到键名=>实现类信息并存储在Map中。
 *     c. 定义获取实例方法，根据用户传入的接口和键名，从Map中找到对应的实现类，然后通过反射获取到实现类对象。可以维护一个对象实例缓存，创建过一次的对象从缓存中读取即可。
 */
@Slf4j
public class SpiLoader {
    // 存储已加载的类：接口名 => ( key => 实现类 )
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();
    // 对象实例缓存，避免重复new：类路径 => 对象实例，单例模式
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();
    // 系统SPI目录
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system";
    // 用户自定义spi目录
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom";
    // 扫描路径
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};
    // 动态加载的类列表
    private static final List<Class<?>> Load_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        log.info("加载所有SPI...");
        for(Class<?> aclass : Load_CLASS_LIST) {
            load(aclass);
        }
    }

    /**
     * 加载某个SPI类型（读取spi配置目录
     * @param loadClass 要加载的SPI的类型
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的SPI...", loadClass.getName());

        // 扫描路径，用户自定义的spi优先级高于系统spi
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        for(String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName()); // 不通过文件路径获得
            // 读取每个资源文件
            for(URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if(strArray.length > 1) {
                            String key = strArray[0];
                            String className = strArray[1];
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("SPI resource load error : ", e);
                }
            }
        }

        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if(keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载 %s 类型！", tClassName));
        }
        if(!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 不存在 key=%s 的类型！", tClassName, key));
        }

        // 获取要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        String implClassName = implClass.getName();
        if(!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMsg = String.format("%s 类实例化失败！", implClassName);
                throw new RuntimeException(errorMsg, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }
}
