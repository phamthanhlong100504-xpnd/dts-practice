package com.dts.practice.messaging;

import com.dts.practice.dto.event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Listens for user events from Identity Service.
 * When a user updates their profile or is deleted, this consumer
 * can sync local data (e.g., exam history display names).
 *
 * Compliant with KAFKA-005 (idempotent consumer),
 * KAFKA-003 (explicit retry handling via KafkaConfig error handler),
 * CONSUMER-004 (duplicate-safe processing).
 */
@Slf4j
@Component
public class UserEventConsumer {

    /**
     * Simple in-memory set to track recently processed event IDs for idempotency.
     * In production, use a Redis set or DB table with TTL.
     */
    private final Set<String> processedEventIds = Collections.synchronizedSet(
            Collections.newSetFromMap(new java.util.LinkedHashMap<String, Boolean>(1000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, Boolean> eldest) {
                    return size() > 10_000;
                }
            })
    );

    @KafkaListener(topics = "${spring.kafka.topics.user-events:user-events}",
            groupId = "#{T(java.util.UUID).randomUUID().toString()}",
            autoStartup = "${spring.kafka.listener.auto-startup:false}")
    public void onUserEvent(@Payload(required = false) UserEvent event) {
        if (event == null) {
            log.warn("Received null event, skipping");
            return;
        }

        String eventType = event.getEventType();
        String userId = event.getUserId();

        if (eventType == null || userId == null) {
            log.warn("Received event with missing type or userId: type={}, userId={}", eventType, userId);
            return;
        }

        // Idempotency: skip duplicate event
        String eventKey = userId + ":" + eventType + ":" + event.getTimestamp();
        if (!processedEventIds.add(eventKey)) {
            log.debug("Duplicate event skipped: key={}", eventKey);
            return;
        }

        log.info("Processing user event: type={}, userId={}, username={}",
                eventType, userId, event.getUsername());

        switch (eventType) {
            case "USER_REGISTERED":
                handleUserRegistered(userId, event.getUsername());
                break;
            case "USER_UPDATED":
                handleUserUpdated(userId, event.getUsername());
                break;
            case "USER_DELETED":
                handleUserDeleted(userId);
                break;
            default:
                log.debug("Unhandled event type: {}", eventType);
        }
    }

    private void handleUserRegistered(String userId, String username) {
        log.info("User registered: userId={}, username={}", userId, username);
    }

    private void handleUserUpdated(String userId, String username) {
        log.info("User profile updated: userId={}, username={}", userId, username);
    }

    private void handleUserDeleted(String userId) {
        log.info("User deleted: userId={}", userId);
    }
}
