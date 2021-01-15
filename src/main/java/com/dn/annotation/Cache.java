package com.dn.annotation;


import com.dn.serialize.ICacheSerialize;
import com.dn.serialize.support.DefaultICacheSerialize;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    /**
     * 缓存名称
     * @return
     */
    String cacheName() default "";

    /**
     * 缓存有效期
     */
    long maxTime() default 0;

    /**
     * 自定义缓存的序列化机制 和 反序列化机制
     */
    Class<? extends ICacheSerialize> serialize() default DefaultICacheSerialize.class;

}

