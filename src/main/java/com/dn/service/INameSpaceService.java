package com.dn.service;

import java.lang.reflect.Method;

/**
 * 缓存命名规则, 用户可以自定义缓存的命名规则.
 * 只需要在 Spring 中注册这个类型的Bean即可. 比如:
 */
/*
    @Bean
    public INameSpaceService nameSpaceService() {
        return (method, args) -> method.getName();
    }
  */
public interface INameSpaceService {
    /**
     * 缓存命名方式
     *
     * @param method 当前 @Cache 标注的运行的方法
     * @param args   当前@Cache标注的运行的方法传入的参数
     * @return
     */
    String cacheName(Method method, Object[] args);
}
