package com.breech.extremity.service;

import com.breech.extremity.model.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getAllTopComments(long articleId);
    void addComment(Comment comment);
    List<Comment> getAllComments( long rootCommentId,long articleId);
    void deleteComment(long commentId);
    List<Comment> getAllTopCommentsByUser(int userId);
    List<Comment> getAllTopCommentsByComment(long ComentId);
}
