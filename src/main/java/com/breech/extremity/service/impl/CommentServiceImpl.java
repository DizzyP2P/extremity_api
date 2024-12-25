package com.breech.extremity.service.impl;

import com.breech.extremity.mapper.CommentMapper;
import com.breech.extremity.model.Comment;
import com.breech.extremity.service.CommentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Resource
    private CommentMapper commentMapper;

    @Override
    public List<Comment> getAllTopComments() {
        return commentMapper.selectAllTopComments();
    }

    @Override
    public void addComment(Comment comment){
        commentMapper.insertComment(comment);
    }
    @Override
    public List<Comment> getAllComments( long rootCommentId){
        return commentMapper.selectAllComments(rootCommentId);
    }

    @Override
    public void deleteComment(long commentId){
        Comment comment = commentMapper.selectById(commentId);
        if(comment.getRootCommentId()==0){
            List<Comment> comments = commentMapper.selectAllComments(commentId);
            for(Comment c : comments){
                commentMapper.deleteByPrimaryKey(c.getId());
            }
        }
        commentMapper.deleteByPrimaryKey(commentId);
    }

}
