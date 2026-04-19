package com.example.chatservice.message.presentation.chat;

import com.example.chatservice.message.presentation.chat.dtos.request.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

	private final SimpMessageSendingOperations messagingTemplate;

	@MessageMapping("/chatrooms")
	public void sendMessage(Message message) {
		log.info("Received message: {}", message);
		messagingTemplate.convertAndSend("/sub/chatrooms/" + message.chatRoomId(), message);
	}

}
