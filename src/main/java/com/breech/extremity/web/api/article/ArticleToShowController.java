package com.breech.extremity.web.api.article;

import com.breech.extremity.auth.TokenManager;
import com.breech.extremity.core.exception.BusinessException;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.dto.UserRolesDTO;
import com.breech.extremity.model.Article;
import com.breech.extremity.model.ArticleContent;
import com.breech.extremity.model.Attachment;
import com.breech.extremity.model.User;
import com.breech.extremity.service.ArticleContentService;
import com.breech.extremity.service.ArticleService;
import com.breech.extremity.service.AttachmentService;
import com.breech.extremity.service.UserService;
import com.breech.extremity.util.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/articleView")
public class ArticleToShowController {
    @Resource
    private ArticleService articleService;
    @Autowired
    private ObjectMapper jacksonObjectMapper;
    @Resource
    private ArticleContentService articleContentService;
    @Resource
    private AttachmentService attachmentService;
    @Resource
    private UserService userService;

    @Resource
    private TokenManager tokenManager;
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${resource.file-download-url}")
    private String fileDownloadUrl;

    @Value("${resource.image-download-url}")
    private String imageDownloadUrl;

    @Value("${resource.image-url-prefix}")
    private String urlPrefix;


    @GetMapping("/attachment/{id}")
    @Transactional
    public GlobalResult getAttachments(@PathVariable("id") String draftId) {
        Article article = articleService.findById(draftId);

        if(article.getArticleStatus().equals("1")){
            return GlobalResultGenerator.genSuccessResult(article);
        }

        Condition cd = new Condition(Attachment.class);
        cd.createCriteria().andEqualTo("articleId", Long.valueOf(draftId));
        List<Attachment> res = attachmentService.findByCondition(cd);
        return GlobalResultGenerator.genSuccessResult(res);
    }

    @GetMapping("/bycatagory")
    public GlobalResult<PageInfo<Article>> getArticlesByCatagory(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer rows) {
        Condition cd = new Condition(Article.class);
        Example.Criteria criteria = cd.createCriteria();
        criteria.andEqualTo("articleStatus", "1").andEqualTo("articleType", type);
        List<Article> articles = articleService.findByCondition(cd);
        PageHelper.startPage(page, rows);
        PageInfo<Article> pageInfo = new PageInfo<>(articles);
        return GlobalResultGenerator.genSuccessResult(pageInfo);
    }

    @GetMapping("/DraftWithAllInfo/{Id}")
    //preview的时候要用
    public GlobalResult getDraftWithAllInfo(@PathVariable("Id") String draftId) {
        Article article = articleService.findById(draftId);
        if(!article.getArticleStatus().equals("1")){
            throw new BusinessException("未授权");
        }
        ArticleContent at = articleContentService.findById(draftId);
        Map<String,Object> info = objectMapper.convertValue(article, Map.class);
        try{
            if(at.getArticleContent()==null){
                info.put("articleContent"," ");
            }else{
                info.put("articleContent",jacksonObjectMapper.readTree(at.getArticleContent()));
            }
        }
        catch (JsonProcessingException e){
            throw new BusinessException(e.getMessage());
        }
        User us = userService.findById(String.valueOf(article.getArticleAuthorId()));
        info.put("userId",us.getIdUser());
        info.put("userName",us.getNickname());
        info.put("avatarUrl",us.getAvatarUrl());
        return GlobalResultGenerator.genSuccessResult(info);
    }

}