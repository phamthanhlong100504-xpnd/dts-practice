package com.dts.practice.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import static com.dts.practice.config.CaffeineCacheConfig.USER_PROGRAMS_CACHE;

@Service
public class RedisMessageSubscriber implements MessageListener {

    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public RedisMessageSubscriber(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            // JSON dạng {"userId":"...", "program":"B2"}
            JsonNode jsonNode = objectMapper.readTree(body);
            String userId = jsonNode.get("userId").asText();

            // Nhận lệnh đổi hạng bằng -> Xóa Caffeine Cache của User đó
            if (cacheManager.getCache(USER_PROGRAMS_CACHE) != null) {
                cacheManager.getCache(USER_PROGRAMS_CACHE).evict(userId);
                System.out.println("Evicted Caffeine Cache for user: " + userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
