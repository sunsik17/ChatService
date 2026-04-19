package com.example.chatservice.message.presentation.chat.dtos.request;

public record Message(
	String sender,
	String chatRoomId,
	String content
) {

}
