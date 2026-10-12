package com.fourspicehotpot.backend.infrastructure.redis;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// common/service/RedisService.java
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisService {

    RedisTemplate<String, String> redisTemplate;

    public void set(String key, String value, long ttlDays) {
        redisTemplate.opsForValue().set(key, value, ttlDays, TimeUnit.DAYS);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}