package com.mex.whytter.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.mex.whytter.domain.Message;
import com.mex.whytter.domain.Views;
import com.mex.whytter.dto.EventType;
import com.mex.whytter.dto.ObjectType;
import com.mex.whytter.repository.MessageRepository;
import com.mex.whytter.util.WebSocketSender;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

@RestController
@RequestMapping("message")
public class MessageController {
    private final MessageRepository messageRepository;
    private final BiConsumer<EventType, Message> webSocketSender;

    @Autowired
    public MessageController(MessageRepository messageRepository, WebSocketSender webSocketSender) {
        this.messageRepository = messageRepository;
        this.webSocketSender = webSocketSender.getSender(ObjectType.MESSAGE, Views.IdName.class);
    }

    @GetMapping
    @JsonView(Views.IdName.class)
    public List<Message> list() {
        return messageRepository.findAll();
    }

    @GetMapping("{id}")
    @JsonView(Views.FullMessage.class)
    public Message getOne(@PathVariable("id") Message message) {
        return message;
    }

    @PostMapping
    public Message create(@RequestBody Message message) {
        message.setDateTime(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(message);

        webSocketSender.accept(EventType.CREATE, updatedMessage);

        return updatedMessage;
    }

    @PutMapping("{id}")
    public Message update(@PathVariable("id") Message messageDB, @RequestBody Message message) {
        BeanUtils.copyProperties(message, messageDB, "id");

        Message updatedMessage = messageRepository.save(messageDB);

        webSocketSender.accept(EventType.UPDATE, updatedMessage);

        return updatedMessage;
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Message message) {
        messageRepository.delete(message);
        webSocketSender.accept(EventType.REMOVE, message);
    }

}
