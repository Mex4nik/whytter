package com.mex.whytter.service;

import com.mex.whytter.domain.Message;
import com.mex.whytter.domain.User;
import com.mex.whytter.domain.Views;
import com.mex.whytter.dto.EventType;
import com.mex.whytter.dto.MessagePageDto;
import com.mex.whytter.dto.ObjectType;
import com.mex.whytter.repository.MessageRepository;
import com.mex.whytter.util.WebSocketSender;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final BiConsumer<EventType, Message> webSocketSender;

    @Autowired
    public MessageService(MessageRepository messageRepository, WebSocketSender webSocketSender) {
        this.messageRepository = messageRepository;
        this.webSocketSender = webSocketSender.getSender(ObjectType.MESSAGE, Views.IdName.class);
    }

    public MessagePageDto findAll(Pageable pageable) {
        Page<Message> page = messageRepository.findAll(pageable);
        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages()
        );
    }

    public Message create(Message message, User user) {
        message.setDateTime(LocalDateTime.now());
        message.setAuthor(user);
        Message updatedMessage = messageRepository.save(message);

        webSocketSender.accept(EventType.CREATE, updatedMessage);

        return updatedMessage;
    }

    public Message update(Message messageDB, Message message, User user) {
        BeanUtils.copyProperties(message, messageDB, "id");

        messageDB.setAuthor(user);
        Message updatedMessage = messageRepository.save(messageDB);

        webSocketSender.accept(EventType.UPDATE, updatedMessage);

        return updatedMessage;
    }

    public void delete(Message message) {
        messageRepository.delete(message);
        webSocketSender.accept(EventType.REMOVE, message);
    }

}
