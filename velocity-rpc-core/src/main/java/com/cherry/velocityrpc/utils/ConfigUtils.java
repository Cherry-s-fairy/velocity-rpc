package com.cherry.velocityrpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * 配置工具类：读取配置文件并返回配置对象，简化调用
 */
public class ConfigUtils {
    /**
     * 加载配置对象
     * @param tClass 配置对象类的类型，eg. Config.class
     * @param prefix 配置项的前缀，eg. 配置文件中有rpc.port=8080，则前缀可以是rpc
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     * @param tClass 目标配置类的class对象
     * @param prefix 配置文件中的前缀
     * @param environment 环境标识，例如"dev", "test"(测试环境, "prod"(生产环境等。
     * @return
     * @param <T>
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if(StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        // 默认配置文件是application.properties，如果指定环境，生成 application-{environment}.properties
        // 例如：environment = "dev" → application-dev.properties
        Props props = new Props(configFileBuilder.toString()); // 使用Hutool的Props类加载该配置文件
        return props.toBean(tClass, prefix); // 调用Props的toBean方法，将配置文件中指定前缀的配置项映射到tClass对应的JavaBean中
    }
}
