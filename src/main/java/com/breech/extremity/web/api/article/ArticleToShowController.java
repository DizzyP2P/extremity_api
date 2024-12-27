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
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Condition;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
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
    public GlobalResult<PageInfo<Article>> getArticlesByCategory(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer rows,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        // 创建查询条件
        Condition cd = new Condition(Article.class);
        Example.Criteria criteria = cd.createCriteria();

        // 判断是否传递了 type 参数，决定是否加上 articleType 筛选条件
        if (StringUtils.isBlank(type)) {
            criteria.andEqualTo("articleStatus", "1");
        } else {
            criteria.andEqualTo("articleStatus", "1").andEqualTo("articleType", type);
        }

        // 判断是否传递了 startTime 和 endTime 参数，若有则加入时间范围筛选
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            try {
                startTime += " 00:00:00"; // 加上起始时间
                endTime += " 23:59:59"; // 加上结束时间
                // 使用包含时间部分的解析格式
                Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
                Date endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);

                // 使用 andBetween 直接添加时间范围条件
                criteria.andBetween("updatedTime", startDate, endDate);
            } catch (ParseException e) {
                return GlobalResultGenerator.genErrorResult("Invalid time format, expected yyyy-MM-dd HH:mm:ss");
            }
        } else {
            // 只传递了 startTime 或 endTime 的情况
            if (StringUtils.isNotBlank(startTime)) {
                try {
                    startTime += " 00:00:00"; // 加上起始时间
                    Date startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
                    criteria.andGreaterThanOrEqualTo("updatedTime", startDate);
                } catch (ParseException e) {
                    return GlobalResultGenerator.genErrorResult("Invalid startTime format, expected yyyy-MM-dd HH:mm:ss");
                }
            }
            if (StringUtils.isNotBlank(endTime)) {
                try {
                    endTime += " 23:59:59"; // 加上结束时间
                    Date endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);
                    criteria.andLessThanOrEqualTo("updatedTime", endDate);
                } catch (ParseException e) {
                    return GlobalResultGenerator.genErrorResult("Invalid endTime format, expected yyyy-MM-dd HH:mm:ss");
                }
            }
        }

        // 设置排序规则
        cd.setOrderByClause("updated_time DESC");

        // 使用 PageHelper 分页，注意调用时要在查询之前调用
        PageHelper.startPage(page, rows);  // 先启动分页
        List<Article> articles = articleService.findByCondition(cd);

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

        if(article.getFinalShowTime()!=null){
            LocalDateTime finalShowTime = Instant.ofEpochMilli(article.getFinalShowTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            info.put("finalShowTime", finalShowTime.format(formatter));
        }
        User us = userService.findById(String.valueOf(article.getArticleAuthorId()));
        info.put("userId",us.getIdUser());
        info.put("userName",us.getNickname());
        info.put("avatarUrl",us.getAvatarUrl());
        return GlobalResultGenerator.genSuccessResult(info);
    }

}