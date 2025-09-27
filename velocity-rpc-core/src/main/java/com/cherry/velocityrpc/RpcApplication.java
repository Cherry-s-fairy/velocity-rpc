package com.cherry.velocityrpc;

import lombok.extern.slf4j.Slf4j;
import com.cherry.velocityrpc.config.RpcConfig;
import com.cherry.velocityrpc.constant.RpcConstant;
import com.cherry.velocityrpc.utils.ConfigUtils;

/**
 * RPC框架应用，相当于 holder，存放了项目全局用到的变量。双检锁单例模式实现
 *
 * RPC框架中需要维护一个全局的配置对象。在引入RPC框架的项目启动时，从配置文件中读取配置并创建对象实例.
 * 之后就可以集中地从这个对象中获取配置信息，而不用每次加载配置时再重新读取配置、并创建新的对象，减少了性开销。
 *
 * 使用设计模式中的单例模式，使用holder来维护全局配置对象实例。
 */
@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化
     *
     * 双锁单例模式经典实现，在获取配置时才调用t方法实现懒加载。
     */
    public static void init() {
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 配置类加载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 框架初始化，支持传入自定义配置
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}", newRpcConfig.toString());
    }

    /**
     * 获取配置
     * @return
     */
    public static RpcConfig getRpcConfig() {
        if(rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if(rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}

// 以后RPC框架内只需要写一行代码，就能正确加载到配置：RpcConfig rpc = RpcApplication.getRpcConfig();