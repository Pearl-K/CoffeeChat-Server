package com.example.coffeechat.user.application;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(3);

    public void updateOnlineStatus(Long userId) {
        String key = "user:" + userId + ":status";
        redisTemplate.opsForValue().set(key, "true", TTL);
    }
}

