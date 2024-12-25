package com.breech.extremity.service;

import com.breech.extremity.core.service.Service;
import com.breech.extremity.model.Article;

import java.util.List;

public interface ArticleService extends Service<Article> {
    List<Article> getAllArticles();
    //处理过的json进行再加工
    void updateArticleContent(Long articleId, String newJsonString);
}
