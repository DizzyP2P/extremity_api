package com.breech.extremity.web.api.article;

import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.model.Article;
import com.breech.extremity.service.ArticleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/article")
public class ArticleController {
    @Resource
    private ArticleService articleService;

    @GetMapping
    public GlobalResult<List<Article>> getAllArticles() {
        List<Article> articles = articleService.getAllArticles();
        return GlobalResultGenerator.genSuccessResult(articles);
    }
}
