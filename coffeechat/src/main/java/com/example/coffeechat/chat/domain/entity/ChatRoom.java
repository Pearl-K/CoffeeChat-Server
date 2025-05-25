package com.example.coffeechat.chat.domain.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "chatrooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private ChatRoom(String title) {
        this.title = title;
    }

    public static ChatRoom create(String title) {
        return new ChatRoom(title);
    }

    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }
}

