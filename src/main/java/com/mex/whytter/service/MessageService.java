package com.mex.whytter.service;

import com.mex.whytter.domain.Message;
import com.mex.whytter.domain.User;
import com.mex.whytter.domain.UserSubscription;
import com.mex.whytter.domain.Views;
import com.mex.whytter.dto.EventType;
import com.mex.whytter.dto.MessagePageDto;
import com.mex.whytter.dto.ObjectType;
import com.mex.whytter.repository.MessageRepository;
import com.mex.whytter.repository.UserSubscriptionRepository;
import com.mex.whytter.util.WebSocketSender;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final BiConsumer<EventType, Message> webSocketSender;

    @Autowired
    public MessageService(
            MessageRepository messageRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            WebSocketSender webSocketSender
    ) {
        this.messageRepository = messageRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.webSocketSender = webSocketSender.getSender(ObjectType.MESSAGE, Views.IdName.class);
    }

    public MessagePageDto findForUser(Pageable pageable, User user) {
        List<User> channels = userSubscriptionRepository.findBySubscriber(user)
                .stream()
                .map(UserSubscription::getChannel)
                .collect(Collectors.toList());

        channels.add(user);

        Page<Message> page = messageRepository.findByAuthorIn(channels, pageable);
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
