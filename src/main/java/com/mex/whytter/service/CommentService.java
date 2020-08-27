package com.mex.whytter.service;

import com.mex.whytter.domain.Comment;
import com.mex.whytter.domain.User;
import com.mex.whytter.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment create(Comment comment, User user) {
        comment.setAuthor(user);
        commentRepository.save(comment);

        return comment;
    }

}
