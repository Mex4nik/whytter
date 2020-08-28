package com.mex.whytter.service;

import com.mex.whytter.domain.Comment;
import com.mex.whytter.domain.User;
import com.mex.whytter.domain.Views;
import com.mex.whytter.dto.EventType;
import com.mex.whytter.dto.ObjectType;
import com.mex.whytter.repository.CommentRepository;
import com.mex.whytter.util.WebSocketSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BiConsumer<EventType, Comment> webSocketSender;

    @Autowired
    public CommentService(CommentRepository commentRepository, WebSocketSender webSocketSender) {
        this.commentRepository = commentRepository;
        this.webSocketSender = webSocketSender.getSender(ObjectType.COMMENT, Views.FullComment.class);
    }

    public Comment create(Comment comment, User user) {
        comment.setAuthor(user);
        Comment commentDB = commentRepository.save(comment);

        webSocketSender.accept(EventType.CREATE, commentDB);

        return commentDB;
    }

}
