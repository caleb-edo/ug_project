package com.example.healthsystem.service;

import com.example.healthsystem.model.Message;
import com.example.healthsystem.model.User;
import com.example.healthsystem.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public List<Message> getMessagesByReceiver(User receiver) {
        return messageRepository.findByReceiverOrderByTimestampDesc(receiver);
    }

    public List<Message> getMessagesBySender(User sender) {
        return messageRepository.findBySenderOrderByTimestampDesc(sender);
    }

    public List<Message> getAllMessagesByUser(User user) {
        return messageRepository.findAllMessagesByUser(user.getId());
    }

    public Message sendMessage(User sender, User receiver, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }
}