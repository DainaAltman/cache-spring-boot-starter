package com.dn.common;

/**
 * 缓存常量
 */
public enum CacheEnum {
    /**
     * 基本类型的缓存, 当缓存的有效期过了后. 则直接失效.
     */
    BASIC(0, "基本类型的缓存"),

    /**
     * 随机缓存, 缓存的有效期是按照 minTime - maxTime 之间的一个随机值
     * 来设置缓存时间的. 缓存的有效期过了后, 直接失效
     */
    RANDOM(1, "随机缓存"),

    /**
     * 当缓存的有效期快过了的时候, 如果此时用户来访问了这部分数据. 则缓存的有效期
     * 将延长, 具体延长的值用户可以自定义
     */
    HEAT(2, "热度缓存"),

    /**
     * 当缓存的有效期快过了的时候, 如果此时用户来访问了这部分数据. 则缓存的有效期
     * 将延长固定时间
     */
    SESSION(3, "会话类型的缓存");

    private int type;
    private String desc;

    CacheEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
