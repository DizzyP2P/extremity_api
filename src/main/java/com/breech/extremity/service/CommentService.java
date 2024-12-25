package com.breech.extremity.service;

import com.breech.extremity.model.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getAllTopComments();
    void addComment(Comment comment);
    List<Comment> getAllComments( long rootCommentId);
    void deleteComment(long commentId);
}
