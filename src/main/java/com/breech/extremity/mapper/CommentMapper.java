package com.breech.extremity.mapper;

import com.breech.extremity.core.mapper.Mapper;
import com.breech.extremity.model.Comment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentMapper extends Mapper<Comment> {
    List<Comment> selectAllTopComments(@Param("articleId")long artilceId);
    List<Comment> selectAllComments(@Param("id") long id,@Param("articleId")long atilceId );
    Comment selectById(@Param("id") long id);
    void insertComment(Comment comment);
    List<Comment> selectAllTopCommentsByUser(@Param("userId") long id);
    List<Comment>selectComments(@Param("commentId")long commentId);
}

