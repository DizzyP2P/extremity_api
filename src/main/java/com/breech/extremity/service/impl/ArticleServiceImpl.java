package com.breech.extremity.service.impl;

import com.breech.extremity.mapper.ArticleMapper;
import com.breech.extremity.model.Article;
import com.breech.extremity.service.ArticleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Resource
    ArticleMapper articleMapper;

    @Override
    public List<Article> getAllArticles(){
        return articleMapper.selectAll();
    }
}
