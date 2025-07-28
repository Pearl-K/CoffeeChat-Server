package com.example.coffeechat.chat.domain;

public enum MessageStatus {
    PENDING, SENT, RECEIVED, FAILED, RETRYING, DEAD_LETTER;
}
