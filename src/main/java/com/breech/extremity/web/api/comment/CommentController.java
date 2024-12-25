package com.breech.extremity.web.api.comment;

import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.dto.UserRegisterInfoDTO;
import com.breech.extremity.model.Comment;
import com.breech.extremity.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @GetMapping("/articleId")
    public GlobalResult<List<Comment>> getAllTopComment() {
        List<Comment> comments = commentService.getAllTopComments();

        return GlobalResultGenerator.genSuccessResult(comments);
    }
    @GetMapping("/commentId")
    public GlobalResult<List<Comment>> getAllComments(@RequestParam("commentId") int commentId) {
        List<Comment> comments = commentService.getAllComments( commentId);

        return GlobalResultGenerator.genSuccessResult(comments);
    }
    @PostMapping("/add")
    public GlobalResult<Boolean> insertComment(@RequestBody Comment comment) {
        log.warn(String.valueOf(comment));
        commentService.addComment(comment);
        return GlobalResultGenerator.genSuccessResult(true);
    }

}
