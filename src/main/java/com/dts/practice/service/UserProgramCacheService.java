package com.dts.practice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.dts.practice.config.CaffeineCacheConfig.USER_PROGRAMS_CACHE;

@Service
public class UserProgramCacheService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public UserProgramCacheService() {
    }

    public UserProgramCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(value = USER_PROGRAMS_CACHE, key = "#userId")
    public String getUserLearningProgram(String userId) {
        if (redisTemplate != null) {
            try {
                String key = "user:" + userId + ":program";
                Object program = redisTemplate.opsForValue().get(key);
                if (program != null) {
                    return program.toString();
                }
            } catch (Exception e) {
                System.err.println("Could not query Redis for user program: " + e.getMessage());
            }
        }
        return "B2";
    }
}