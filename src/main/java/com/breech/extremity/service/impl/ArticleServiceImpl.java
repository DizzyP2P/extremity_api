package com.breech.extremity.service.impl;

import com.breech.extremity.core.exception.BusinessException;
import com.breech.extremity.core.service.AbstractService;
import com.breech.extremity.mapper.ArticleContentMapper;
import com.breech.extremity.mapper.ArticleMapper;
import com.breech.extremity.model.Article;
import com.breech.extremity.model.ArticleContent;
import com.breech.extremity.service.ArticleService;
import com.breech.extremity.util.FileUtils;
import com.breech.extremity.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl extends AbstractService<Article> implements ArticleService {
    @Resource
    ArticleMapper articleMapper;
    @Resource
    ArticleContentMapper articleContentMapper;
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Value("${resource.url-prefix}")
    private String images_prefix;
    @Override
    public List<Article> getAllArticles(){
        return articleMapper.selectAll();
    }

    @Override
    public void updateArticleContent(Long articleId, String newJsonString) {
        // 1. 查询旧文章记录
        ArticleContent articleContent = new ArticleContent();
        articleContent.setIdArticle(articleId);
        articleContent = articleContentMapper.selectOne(articleContent);

        if(articleContent == null) {
            log.warn("找不到哦");
            return;
        }

        log.warn(articleContent.toString());
        String oldJsonString = articleContent.getArticleContent();
        JsonNode oldJsonNode = null;

        if (oldJsonString != null && !oldJsonString.isEmpty()) {
            try{
                oldJsonNode = jacksonObjectMapper.readTree(oldJsonString);
            }catch (Exception e){
                throw  new BusinessException(e.getMessage());
            }
        }

        // 3. 提取旧图片URL
        List<String> oldImages = (oldJsonNode == null)
                ? Collections.emptyList()
                : JsonUtils.extractImageUrls(oldJsonNode);

        // 4. 解析新的 JSON
        try{
            JsonNode newJsonNode = jacksonObjectMapper.readTree(newJsonString);
            List<String> newImages = JsonUtils.extractImageUrls(newJsonNode);
            // 5. 找到“被删除的图片” = 旧列表 - 新列表

            List<String> removedImages = oldImages.stream()
                    .filter(url -> !newImages.contains(url))
                    .collect(Collectors.toList());

            for (String removedUrl : removedImages) {
                if(removedUrl.startsWith(images_prefix)){
                    String relativePath = removedUrl.substring(images_prefix.length());
                    FileUtils.delete("/Users/mac/projects/Extremity/image"+relativePath);  // Pass the relative path to the delete function
                }
            }
        }
        catch (Exception e){
            throw  new BusinessException(e.getMessage());
        }
    }
}
