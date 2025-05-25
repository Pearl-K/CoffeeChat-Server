package com.example.coffeechat.chat.presentation;

import com.example.coffeechat.chat.application.ChatMessageService;
import com.example.coffeechat.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/{chatroomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getChatMessages(
            @PathVariable Long chatroomId,
            Pageable pageable
    ) {
        Page<ChatMessageResponse> responsePage = chatMessageService
                .getChatMessagesByChatroomId(chatroomId, pageable);
        return ResponseEntity.ok(responsePage);
    }
}
