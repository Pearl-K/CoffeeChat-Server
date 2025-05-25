package com.example.coffeechat.chat.infrastructure.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicManagerService {

    private final RedisMessageListenerContainer container;
    private final RedisSubscriber redisSubscriber;
    private final Map<Long, ChannelTopic> chatroomTopics = new ConcurrentHashMap<>();

    public void addChatRoomTopic(Long chatroomId) {
        if (chatroomTopics.containsKey(chatroomId)) return;
        ChannelTopic topic = new ChannelTopic("chatroom:" + chatroomId);
        container.addMessageListener(redisSubscriber, topic);
        chatroomTopics.put(chatroomId, topic);
        log.info("💬 [TopicManager] chatroom {} -> 동적 리스너 등록 완료", chatroomId);
    }

    public void removeChatRoomTopic(Long chatroomId) {
        ChannelTopic topic = chatroomTopics.remove(chatroomId);
        container.removeMessageListener(redisSubscriber, topic);
        log.info("💬 [TopicManager] chatroom {} -> 동적 리스너 해제 완료", chatroomId);
    }
}

