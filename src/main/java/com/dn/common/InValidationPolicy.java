package com.dn.common;

/**
 * 缓存失效策略,
 */
public enum InValidationPolicy {
    // 立即失效, 当缓存标记为失效后. 必须等待缓存组中所有缓存被完全清除后
    // 程序才能写入数据到缓存中. 从而保证缓存中的数据是最新的
    IMMEDIATE_FAILURE,

    // 延迟失效, 程序会以异步的方式去删除缓存. 如果此时有新的缓存被添加进来
    // 新的缓存会覆盖旧的缓存. 而程序去删除缓存的时候会根据缓存的加入时间是否
    // 大于缓存清除缓存开始的时间来确认是否需要删除这个缓存.
    DELAYED_INVALIDATION
}
