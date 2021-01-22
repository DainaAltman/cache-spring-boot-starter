package com.dn.annotation;

import com.dn.common.InValidationPolicy;

import java.lang.annotation.*;

/**
 * 清楚缓存, 当我们执行 Update,Insert,Delete 方法的时候. 如果
 * 在方法上标注 @CacheClear 注解, 那么对应缓存所属组中的所有缓存
 * 都将失效
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheClear {

    /**
     * 缓存所属组名称, 默认使用当前 @CacheClear 标记方法的所属类作为组名称
     * @return
     */
    String cacheGroup() default "";

    /**
     * 缓存失效策略, 默认为延迟失效
     * @return
     */
    InValidationPolicy policy() default InValidationPolicy.DELAYED_INVALIDATION;
}
