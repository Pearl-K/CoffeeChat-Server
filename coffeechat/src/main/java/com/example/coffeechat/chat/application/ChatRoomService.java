package com.example.coffeechat.chat.application;

import com.example.coffeechat.chat.domain.ChatRoomRepository;
import com.example.coffeechat.chat.domain.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    private ChatRoom getChatRoom(Long chatroomId) {
        return chatRoomRepository.findById(chatroomId)
                .orElseThrow(RuntimeException::new);
    }
}