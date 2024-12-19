package com.breech.extremity.web.api.auth;

import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.mapper.ArticleMapper;
import com.breech.extremity.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    ArticleMapper articleMapper;

    @GetMapping
    public GlobalResult<Map<String,Object>> Get(){
        List<Article> res =  articleMapper.selectAll();
        return GlobalResultGenerator.genResult(true, Collections.singletonMap("tmp",res),"nothing");
    }


}
