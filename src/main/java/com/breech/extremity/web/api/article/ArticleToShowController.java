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
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") Integer page,  // 将参数类型改为 Integer
            @RequestParam(defaultValue = "12") Integer rows) {  // 将参数类型改为 Integer

        // 创建查询条件
        Condition cd = new Condition(Article.class);
        Example.Criteria criteria = cd.createCriteria();
        // 判断是否传递了 type 参数，决定是否加上 articleType 筛选条件
        if (StringUtils.isBlank(type)) {
            criteria.andEqualTo("articleStatus", "1");
        } else {
            criteria.andEqualTo("articleStatus", "1").andEqualTo("articleType", type);
        }
        List<Article> articles = articleService.findByCondition(cd);
        log.warn(articles.toString());
        // 使用 PageHelper 分页，注意调用时要在查询之前调用
        PageHelper.startPage(page, rows);  // 先启动分页

         articles = articleService.findByCondition(cd);
        log.warn(articles.toString());

        // 创建 PageInfo 以便于分页结果
        PageInfo<Article> pageInfo = new PageInfo<>(articles);
        // 返回分页结果
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
        // 使用 LocalDateTime 和 DateTimeFormatter 格式化时间戳
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 将时间戳转换为 LocalDateTime 对象
        LocalDateTime createdTime = Instant.ofEpochMilli(article.getCreatedTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime updatedTime = Instant.ofEpochMilli(article.getUpdatedTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        if(article.getFinalShowTime()!=null){
            LocalDateTime finalShowTime = Instant.ofEpochMilli(article.getFinalShowTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            info.put("finalShowTime", finalShowTime.format(formatter));
        }
        info.put("createdTime", createdTime.format(formatter));
        info.put("updatedTime", updatedTime.format(formatter));
        User us = userService.findById(String.valueOf(article.getArticleAuthorId()));
        info.put("userId",us.getIdUser());
        info.put("userName",us.getNickname());
        info.put("avatarUrl",us.getAvatarUrl());
        return GlobalResultGenerator.genSuccessResult(info);
    }

}