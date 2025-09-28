package com.cherry.velocityrpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock代理服务（JDK动态代理）
 *
 * 使用Mock服务来模拟远程服务的行为，以便进行接口的测试、开发和调试。Mock是指模拟对象。
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    /**
     * 调用mock代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据方法的返回值类型，生成特定的默认值对象
        Class<?> methodReturnType = method.getReturnType();
        log.info("mock invoke {}", method.getName());
        return getDefaultObject(methodReturnType);
    }

    /**
     * 生成指定类型的默认值对象
     * @param type
     * @return
     */
    private Object getDefaultObject(Class<?> type) {
        // 基本类型
        if(type.isPrimitive()) {
            if(type == boolean.class) {
                return false;
            } else if(type == short.class) {
                return (short)0;
            } else if(type == int.class) {
                return 0;
            } else if(type == long.class) {
                return 0L;
            } else if(type == float.class) {
                return 0.0f;
            } else if(type == double.class) {
                return 0.0;
            }
            return null;
        } else { // 对象类型
            return null;
        }
    }
}
