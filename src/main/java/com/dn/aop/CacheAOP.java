package com.dn.aop;

import com.dn.annotation.Cache;
import com.dn.common.CacheEnum;
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
import java.util.Random;

@Slf4j
@Aspect
public class CacheAOP {
    INameSpaceService nameSpaceService;
    ICacheService cacheService;
    Random random = new Random();

    public CacheAOP(ICacheService cacheService, INameSpaceService nameSpaceService) {
        this.nameSpaceService = nameSpaceService;
        this.cacheService = cacheService;
    }

    @Around("@annotation(com.dn.annotation.Cache)")
    public Object handleCache(ProceedingJoinPoint jp) throws Throwable {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        Object[] args = jp.getArgs();

        // 获取 Cache 注解
        Cache cache = method.getAnnotation(Cache.class);
        String cacheName = cache.cacheName();
        if (StringUtils.isEmpty(cacheName)) {
            // 获取这个方法运行状态下对应的唯一id
            cacheName = nameSpaceService.cacheName(method, args);
        }

        if (log.isDebugEnabled() || log.isInfoEnabled()) {
            log.info("类 [{}] 下的方法 [{}] 对应的缓存名称为: {}", jp.getTarget().getClass().getName(), method.getName(), cacheName);
        }
        String json = cacheService.get(cacheName);

        // 拿到 json 序列化和反序列化策略
        Class<? extends ICacheSerialize> cacheSerialize = cache.serialize();
        // 实例化类
        ICacheSerialize cacheHandler = cacheSerialize.newInstance();

        Class methodReturnType = MethodUtils.getMethodReturnType(method);

        // 命中缓存
        if (!StringUtils.isEmpty(json)) {
            log.info("命中缓存");

            // 有效期重置
            long ttl = cacheService.ttl(cacheName);
            // 如果本身存在时间有效期
            if (cache.time() > 0) {
                switch (cache.cacheType()) {
                    case HEAT:
                        cacheService.set(cacheName, json, ttl + cache.heatTime());
                        break;
                    case SESSION:
                        cacheService.set(cacheName, json, ttl + cache.sessionTime());
                        break;
                }
            }

            // 进行反序列化机制, 传入反序列化前的 json 串, 以及方法返回类型, 和方法返回泛型
            return cacheHandler.deserialization(json, methodReturnType, MethodUtils.getMethodGenericTypes(method));
        }
        if (log.isDebugEnabled() || log.isInfoEnabled()) {
            log.info("没有命中缓存");
        }
        // 执行原来的代码逻辑
        Object proceed = jp.proceed();

        // 如果方法没有返回值, 那就没必要序列化了
        if (!void.class.isAssignableFrom(methodReturnType)) {
            if (log.isDebugEnabled() || log.isInfoEnabled()) {
                log.info("类 [{}] 下的方法 [{}] 需要被缓存", jp.getTarget().getClass().getName(), method.getName());
            }

            long ttl = cache.time();

            if (cache.cacheType() == CacheEnum.RANDOM) {
                ttl = Math.min(1, cache.minTime()) + Math.min(1, random.nextInt((int) (cache.maxTime() - cache.minTime())));
            }

            // 无限时间
            if (ttl <= 0) {
                cacheService.set(cacheName, cacheHandler.serialize(proceed));
            } else {
                // 有限时间
                cacheService.set(cacheName, cacheHandler.serialize(proceed), ttl);
            }
        }

        return proceed;
    }
}
