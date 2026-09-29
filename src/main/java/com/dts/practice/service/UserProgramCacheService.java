package com.dts.practice.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.dts.practice.config.CaffeineCacheConfig.USER_PROGRAMS_CACHE;

@Service
public class UserProgramCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public UserProgramCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(value = USER_PROGRAMS_CACHE, key = "#userId")
    public String getUserLearningProgram(String userId) {
        // Nếu không có trong Caffeine (Miss Cache), nó sẽ chạy vào hàm này để ra Redis lấy
        System.out.println("Caffeine Cache Miss -> Fetching from Redis for User: " + userId);
        String key = "user:" + userId + ":program";
        Object program = redisTemplate.opsForValue().get(key);
        
        if (program != null) {
            return program.toString();
        }
        
        // Nếu Redis cũng không có (User chưa chọn bao giờ), mặc định là B2 (hoặc A1 tùy nghiệp vụ)
        return "B2"; 
    }
}
