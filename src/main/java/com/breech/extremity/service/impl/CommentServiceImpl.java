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
    public List<Comment> getAllTopComments(long articleId) {
        return commentMapper.selectAllTopComments(articleId);
    }

    @Override
    public void addComment(Comment comment){
        commentMapper.insertComment(comment);
    }
    @Override
    public List<Comment> getAllComments( long rootCommentId,long articleId){
        return commentMapper.selectAllComments(rootCommentId,articleId);
    }

    @Override
    public void deleteComment(long commentId){
        Comment comment = commentMapper.selectById(commentId);
        if(comment.getRootCommentId()==0){
            List<Comment> comments = commentMapper.selectAllComments(commentId,1);
            for(Comment c : comments){
                commentMapper.deleteByPrimaryKey(c.getId());
            }
        }
        commentMapper.deleteByPrimaryKey(commentId);
    }

    @Override
    public List<Comment> getAllTopCommentsByUser(int userId){
        return commentMapper.selectAllTopCommentsByUser(userId);
    }
    @Override
    public   List<Comment> getAllTopCommentsByComment(long ComentId){
        return commentMapper.selectComments(ComentId);
    }
}
