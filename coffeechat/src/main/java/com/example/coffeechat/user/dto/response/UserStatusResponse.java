package com.example.coffeechat.user.dto.response;

public record UserStatusResponse(
        Long userId,
        Boolean status
) {
}

