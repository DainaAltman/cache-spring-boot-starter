package com.dn.annotation;


import com.dn.common.CacheEnum;
import com.dn.serialize.ICacheSerialize;
import com.dn.serialize.support.DefaultICacheSerialize;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    /**
     * 缓存名称
     *
     * @return
     */
    String cacheName() default "";

    /**
     * 缓存有效期
     *
     * @return
     */
    long time() default 0;

    /**
     * 设置缓存的最小有效期, 当缓存是随机时间的缓存时,
     * 缓存时间会取 minTime - maxTime 之间的一个随机数作为缓存有效时间
     */
    long minTime() default 0;

    /**
     * 设置缓存的最大有效期, 当缓存是随机时间的缓存时,
     * 缓存时间会取 minTime - maxTime 之间的一个随机数作为缓存有效时间
     */
    long maxTime() default 0;

    /**
     * 默认热点数据延长时间为10秒钟
     *
     * @return
     */
    long heatTime() default 10;

    /**
     * 会话级别的数据, 至少要求时间必须在 600s 以上. 如果
     * 低于 600s, 则使用 600s
     *
     * @return
     */
    long sessionTime() default 1800;

    /**
     * 自定义缓存的 序列化机制 和 反序列化机制 ,默认是将返回值转换为 json,
     * 然后获取缓存的时候, 将缓存中的内容转换为 JavaBean
     */
    Class<? extends ICacheSerialize> serialize() default DefaultICacheSerialize.class;

    /**
     * 缓存的类型, 默认普通缓存
     *
     * @return
     */
    CacheEnum cacheType() default CacheEnum.BASIC;
}

