package com.dn.aop;

import com.dn.annotation.Cache;
import com.dn.serialize.ICacheSerialize;
import com.dn.service.ICacheService;
import com.dn.service.INameSpaceService;
import com.dn.utils.MethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Slf4j
@Aspect
public class CacheAOP {
    /**
     * 缓存业务服务
     */
    private ICacheService cacheService;

    /**
     * 缓存命名规则服务
     */
    private INameSpaceService nameSpaceService;

    public CacheAOP(ICacheService cacheService, INameSpaceService nameSpaceService) {
        this.cacheService = cacheService;
        this.nameSpaceService = nameSpaceService;
    }

    @Around("@annotation(com.dn.annotation.Cache)")
    public Object handleCache(ProceedingJoinPoint jp) throws Throwable {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        Object[] args = jp.getArgs();

        // 获取这个方法运行状态下对应的唯一id
        String cacheName = nameSpaceService.cacheName(method, args);
        log.info("缓存名称" + cacheName);

        // 获取 Cache 注解
        Cache cache = method.getAnnotation(Cache.class);
        if (!StringUtils.isEmpty(cache.cacheName())) {
            cacheName = cache.cacheName();
        }
        String json = cacheService.get(cacheName);

        // 拿到 json 序列化和反序列化策略
        Class<? extends ICacheSerialize> cacheSerialize = cache.serialize();
        // 实例化类
        ICacheSerialize cacheHandler = cacheSerialize.newInstance();

        // 命中缓存
        if (!StringUtils.isEmpty(json)) {
            log.info("命中缓存");

            // 进行反序列化机制, 传入反序列化前的 json 串, 以及方法返回类型, 和方法返回泛型
            return cacheHandler.deserialization(json, method.getReturnType(), MethodUtils.getMethodGenericTypes(method));
        }
        log.info("没有命中缓存");
        // 执行原来的代码逻辑
        Object proceed = jp.proceed();

        // 如果方法没有返回值, 那就没必要序列化了
        if (!void.class.isAssignableFrom(signature.getReturnType())) {
            log.info("此方法需要被缓存");
            long maxTime = cache.maxTime();

            // 无限时间
            if (maxTime <= 0) {
                cacheService.set(cacheName, cacheHandler.serialize(proceed));
            } else {
                // 有限时间
                cacheService.set(cacheName, cacheHandler.serialize(proceed), maxTime);
            }
        }

        return proceed;
    }
}
