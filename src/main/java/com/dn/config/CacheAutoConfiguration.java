package com.dn.config;


import com.dn.annotation.Cache;
import com.dn.aop.CacheAOP;
import com.dn.service.ICacheService;
import com.dn.service.INameSpaceService;
import com.dn.service.support.DefaultNameSpaceService;
import com.dn.utils.MethodUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

/**
 * 用户必须配置 ICacheService 的实例才能使用这个 cache-aop 依赖
 * 因为不同的用户, 可能需要的缓存数据存储策略不同, 可能存在 Redis 的缓存, MemoryCache 的缓存.
 * MongoDB 的缓存, ElasticSearch 的缓存...
 *
 * 我这里不能写死, 所以直接把具体的缓存业务逻辑交给用户实现. 我这里只实现数据库数据的
 * json序列化 和 json 反序列化.
 */
@Configuration
@ConditionalOnBean(ICacheService.class)
@EnableAspectJAutoProxy
@Slf4j
public class CacheAutoConfiguration {

    @Autowired
    private ICacheService cacheService;

    @Autowired
    private INameSpaceService nameSpaceService;

    /**
     * 当用户没有配置 cache 的命名规则, 则使用默认的命名规则
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(INameSpaceService.class)
    public INameSpaceService nameSpaceService() {
        return new DefaultNameSpaceService();
    }

    @Bean
    public ApplicationListener<ContextRefreshedEvent> cacheEvent() {
        return event -> {
            GenericApplicationContext app = (GenericApplicationContext) event.getApplicationContext();
            String[] beanDefinitionNames = app.getBeanDefinitionNames();

            for (String beanDefinitionName : beanDefinitionNames) {
                BeanDefinition beanDefinition = app.getBeanDefinition(beanDefinitionName);
                // 获取 Spring Bean 对应的原始 Class 类型
                try {
                    // 存在一些 FactoryBean 没有 beanClassName 属性, 我们需要过滤掉
                    if (beanDefinition.getBeanClassName() != null) {
                        Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
                        Method[] methods = beanClass.getMethods();

                        for (Method method : methods) {
                            // 从方法上寻找 @Cache 注解
                            if (AnnotationUtils.findAnnotation(method, Cache.class) != null) {
                                String methodName = MethodUtils.getMethodId(method);
                                // 初始化一遍获取 Bean 的泛型, 后面再次获取的时候就会快很多
                                MethodUtils.getMethodGenericTypes(method);
                                log.info("扫描到方法 {} 存在 @Cache 注解", methodName);
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    log.warn("这个类无法被加载", e);
                }
            }
        };
    }

    /**
     * 配置 aop
     * @return
     */
    @Bean
    public CacheAOP cacheAOP() {
        return new CacheAOP(cacheService, nameSpaceService);
    }
}
