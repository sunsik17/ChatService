package com.example.chatservice.message.presentation.chatmessage.dtos.request;

public record Message(
	String sender,
	String chatRoomId,
	String content
) {

}
