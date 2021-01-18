package com.dn.config;

import com.dn.service.ICacheService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class CacheServiceAutoConfiguration {

    /**
     * 当 StringRedisTemplate 这个 Bean 存在时, 自动创建 ICacheService
     * @param redisTemplate
     * @return
     */
    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public ICacheService redisCacheService(StringRedisTemplate redisTemplate) {
        return new ICacheService() {
            @Override
            public void set(String key, String val, long timeout) {
                redisTemplate.opsForValue().set(key, val, timeout);
            }

            @Override
            public void set(String key, String val) {
                redisTemplate.opsForValue().set(key, val);
            }

            @Override
            public String get(String key) {
                return redisTemplate.opsForValue().get(key);
            }

            @Override
            public long ttl(String key) {
                return redisTemplate.opsForValue().getOperations().getExpire("redisKey");
            }
        };
    }

}
