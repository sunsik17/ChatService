package com.example.chatservice.message.domain.chatroom;

import com.example.chatservice.message.domain.chatmessage.ChatMessage;
import com.example.chatservice.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String lastMessage;
    private String name;

    @ElementCollection
    private List<UUID> members;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChatMessage> messages;
}
