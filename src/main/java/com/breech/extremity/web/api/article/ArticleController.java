package com.breech.extremity.web.api.article;

import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.core.exception.BusinessException;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.model.Article;
import com.breech.extremity.model.ArticleContent;
import com.breech.extremity.model.Attachment;
import com.breech.extremity.model.User;
import com.breech.extremity.service.ArticleContentService;
import com.breech.extremity.service.ArticleService;
import com.breech.extremity.service.AttachmentService;
import com.breech.extremity.util.FileUtils;
import com.breech.extremity.util.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Condition;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

import static cn.hutool.core.date.DateTime.now;

@Slf4j
@RestController
@RequestMapping("/api/article")
public class ArticleController {
    @Resource
    private ArticleService articleService;
    @Autowired
    private ObjectMapper jacksonObjectMapper;
    @Resource
    private ArticleContentService articleContentService;
    @Resource
    private AttachmentService attachmentService;

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
        Condition cd = new Condition(Attachment.class);
        cd.createCriteria().andEqualTo("articleId", Long.valueOf(draftId));
        List<Attachment> res = attachmentService.findByCondition(cd);
        return GlobalResultGenerator.genSuccessResult(res);
    }

    @DeleteMapping("/deleteattachmentById/{id}")
    @Transactional
    public GlobalResult deleteAttachment(@PathVariable("id") String id) {
        User currentUser = UserUtils.getCurrentUserByToken();
        Attachment res = attachmentService.findById(id);

        if(res==null){
            throw  new BusinessException("不存在");
        }
        Article article=  articleService.findById(String.valueOf(res.getArticleId()));
        if(article==null){
            throw  new BusinessException("未知错误");
        }
        if(!article.getArticleAuthorId().equals(currentUser.getIdUser())){
            throw  new BusinessException("不是你的attachment");
        }
        attachmentService.deleteById(id);
        FileUtils.delete(fileDownloadUrl+res.getAttachmentUrl());
        return GlobalResultGenerator.genSuccessResult();
    }

    @PostMapping("/attachment/{id}")
    @Transactional
    public GlobalResult uploadAttachment(@PathVariable("id") String draftId,
                                         @RequestParam("file") MultipartFile file) {
        // 校验文件是否为空
        if (file.isEmpty()) {
            return GlobalResultGenerator.genErrorResult("上传文件不能为空！");
        }

        // 校验文件大小（限制为 5MB）
        long maxFileSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxFileSize) {
            return GlobalResultGenerator.genErrorResult("文件大小不能超过 5MB！");
        }
        Attachment attachment = new Attachment();
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String dirPath = fileDownloadUrl + File.separator + "article" +File.separator +draftId;
        String filePath = dirPath + File.separator + fileName;

        attachment.setAttachmentUrl(File.separator + "article" +File.separator +draftId+ File.separator+fileName);
        attachment.setArticleId(Long.valueOf(draftId));
        attachment.setAttachmentName(fileName);

        try {
            File dir = new File(dirPath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            file.transferTo(new File(filePath));
            attachmentService.save(attachment);
            return GlobalResultGenerator.genSuccessResult(attachment);
        } catch (IOException e) {
            e.printStackTrace();
            return GlobalResultGenerator.genErrorResult("文件上传失败，原因：" + e.getMessage());
        }
    }

    @PutMapping("/attachment/{id}")
    @Transactional
    public GlobalResult updateAttachment(@PathVariable("id") String attachmentId,@RequestBody Map<String,Object> attachmentName
                                         ) {
        Attachment attachment = attachmentService.findById(attachmentId);
        if(attachment==null){
            throw new BusinessException("没这个附件");
        }
        attachment.setAttachmentName(attachmentName.get("attachmentName").toString());
        attachmentService.update(attachment);
        return GlobalResultGenerator.genSuccessResult();
    }

    @GetMapping
    public GlobalResult<List<Article>> getAllArticles() {
        List<Article> articles = articleService.getAllArticles();
        return GlobalResultGenerator.genSuccessResult(articles);
    }

    @PostMapping("/Draft")
    @Transactional
    public GlobalResult postDrafts(@RequestBody Article article) {
        User currentuser = UserUtils.getCurrentUserByToken();
        Article articletosave = new Article();
        articletosave.setArticleStatus("0");
        articletosave.setArticleAuthorId(currentuser.getIdUser());
        articletosave.setArticleTitle(article.getArticleTitle());
        articletosave.setCreatedTime(now());
        articletosave.setUpdatedTime(now());
        articleService.save(articletosave);
        ArticleContent ct = new ArticleContent();
        ct.setCreatedTime(now());
        ct.setUpdatedTime(now());
        ct.setIdArticle(articletosave.getIdArticle());
        articleContentService.save(ct);
        return GlobalResultGenerator.genSuccessResult(articletosave.getIdArticle());
    }


    //get 一片文章的摘要信息
    @GetMapping("/DraftCompendium/{Id}")
    public GlobalResult getArticle(@PathVariable("Id") String draftId) {
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> res = articleService.findByCondition(condition);
        if(!res.stream().map(Article::getIdArticle).collect(Collectors.toList()).contains(Long.valueOf(draftId))){
            throw new BusinessException("你小子没有权限");
        }
        Article article = articleService.findById(draftId);
        return GlobalResultGenerator.genSuccessResult(article);
    }

    //get 一片文章的摘要信息
    @GetMapping("/DraftWithAllInfo/{Id}")
    public GlobalResult getDraftWithAllInfo(@PathVariable("Id") String draftId) {
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> res = articleService.findByCondition(condition);

        if(!res.stream().map(Article::getIdArticle).collect(Collectors.toList()).contains(Long.valueOf(draftId))){
            throw new BusinessException("你小子没有权限");
        }
        Article article = articleService.findById(draftId);
        ArticleContent at = articleContentService.findById(draftId);
        Map<String,Object> info = objectMapper.convertValue(article, Map.class);
        try{
            info.put("articleContent",jacksonObjectMapper.readTree(at.getArticleContent()));
        }
        catch (JsonProcessingException e){
            throw new BusinessException(e.getMessage());
        }
        info.put("userId",currentuser.getIdUser());
        info.put("userName",currentuser.getNickname());
        info.put("avatarUrl",currentuser.getAvatarUrl());
        return GlobalResultGenerator.genSuccessResult(info);
    }

    //post 一片文章的摘要信息
    @PostMapping(value = "/DraftCompendium", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GlobalResult postCompendium(
            @RequestParam("idArticle") Long idArticle,
            @RequestParam(value = "summary", required = false) String summary,
            @RequestParam(value = "articleType", required = false) Integer articleType,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile
    ) {
        // 1. 校验用户
        User currentUser = UserUtils.getCurrentUserByToken();

        // 2. 根据 idArticle 查出要修改的文章
        Article article = articleService.findById(String.valueOf(idArticle));
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        // 确认该文章是当前用户创建的
        if (!article.getArticleAuthorId().equals(currentUser.getIdUser())) {
            throw new BusinessException("没有权限");
        }
        // 3. 设置更新字段
        // 如果前端传了 summary，就更新，否则保留原值...
        if (summary != null) {
            article.setArticlePreviewContent(summary);
        }

        if (articleType != null) {
            article.setArticleType(String.valueOf(articleType));
        }

        if (tags != null) {
            // 这里简单写法：例如把前端传来的 "tag1,tag2,tag3" 用逗号分隔
            article.setArticleTags(tags);
        }
        article.setUpdatedTime(now());

        if (coverFile != null && !coverFile.isEmpty()) {
            String url = article.getArticleThumbnailUrl();

            if(url!=null){
                FileUtils.delete(imageDownloadUrl+url);
            }

            String originalFilename = coverFile.getOriginalFilename();
            String newFileName = System.currentTimeMillis() + "_" + originalFilename;
            File dest = new File(imageDownloadUrl+"/articles/" + String.valueOf(idArticle) +"/"+ newFileName);
            if(!dest.exists()){
                dest.mkdirs();
            }
            try {
                coverFile.transferTo(dest);
                article.setArticleThumbnailUrl("/image/articles/" + String.valueOf(idArticle) +"/" + newFileName);
            } catch (IOException e) {
                throw new BusinessException("文件保存失败: " + e.getMessage());
            }
        }
        articleService.update(article);
        return GlobalResultGenerator.genSuccessResult("更新文章信息成功");
    }


    @GetMapping("/doAudit/{draftId}")
    @Transactional
    public GlobalResult deliverArticle(@PathVariable("draftId") String draftId) {
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> res = articleService.findByCondition(condition);
        if(!res.stream().map(Article::getIdArticle).collect(Collectors.toList()).contains(Long.valueOf(draftId))){
            throw new BusinessException("你小子没有权限");
        }
        Article article = articleService.findById(draftId);
        if(!article.getArticleStatus().equals("0")){
            throw new BusinessException("文章已经在审核了");
        }
        article.setUpdatedTime(now());
        article.setArticleStatus("2");
        articleService.update(article);
        return GlobalResultGenerator.genSuccessResult();
    }

    @GetMapping("/undoAudit/{draftId}")
    @Transactional
    public GlobalResult undeliverArticle(@PathVariable("draftId") String draftId) {
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> res = articleService.findByCondition(condition);

        if(!res.stream().map(Article::getIdArticle).collect(Collectors.toList()).contains(Long.valueOf(draftId))){
            throw new BusinessException("你小子没有权限");
        }

        Article article = articleService.findById(draftId);

        if(!article.getArticleStatus().equals("2")){
            throw new BusinessException("文章状态不对");
        }

        article.setUpdatedTime(now());
        article.setArticleStatus("0");
        articleService.update(article);
        return GlobalResultGenerator.genSuccessResult();
    }


    @PutMapping("/Draft")
    @Transactional
    public GlobalResult updateArticle(@RequestBody Article article) {
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> res = articleService.findByCondition(condition);

        if(!res.stream().map(Article::getIdArticle).collect(Collectors.toList()).contains(article.getIdArticle())){
                throw new BusinessException("你小子没有权限");
        }

        article.setArticleStatus("0");
        article.setUpdatedTime(now());
        articleService.update(article);
        return GlobalResultGenerator.genSuccessResult();
    }


    @DeleteMapping("/Draft/{draftId}")
    @Transactional
    public GlobalResult deleteArticle(@PathVariable("draftId") String draftId) {
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> res = articleService.findByCondition(condition);
        log.warn(res.toString());
        if(!res.stream().map(Article::getIdArticle).collect(Collectors.toList()).contains(Long.valueOf(draftId))){
            throw new BusinessException("你小子没有权限");
        }
        Article currentArticle = articleService.findById(draftId);
        if(currentArticle.getArticleStatus().equals("1")){
            throw new BusinessException("你小子没有权限");
        }
        articleService.deleteById(draftId);
        articleContentService.deleteById(draftId);
        FileUtils.deleteDirectory(imageDownloadUrl+"/articles/" + draftId);
        return GlobalResultGenerator.genSuccessResult();
    }

    @GetMapping("/Draft/{draftId}")
    public GlobalResult getDrafts(@PathVariable("draftId") String draftId){
        try{
            ArticleContent ct = articleContentService.findById(draftId);
            if(ct.getArticleContent()==null){
                return GlobalResultGenerator.genSuccessResult(null);
            }
            JsonNode root = jacksonObjectMapper.readTree(ct.getArticleContent());
            return GlobalResultGenerator.genSuccessResult(root);
        }
        catch(Exception e){
            throw new BusinessException(e.getMessage());
        }
    }

    @GetMapping("/{userid}")
    public GlobalResult getAllArticlesByUserId(){
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> articles = articleService.findByCondition(condition);
        return GlobalResultGenerator.genSuccessResult(articles);
    }

    @PostMapping("/UpdateDraft/{draftId}")
    @Transactional
    public GlobalResult postDrafts(
            @PathVariable("draftId") String draftId,
            @RequestParam("contentJson") String contentJson,
            @RequestParam MultiValueMap<String, MultipartFile> fileMap
    )
    {
        User currentuser = UserUtils.getCurrentUserByToken();
        Condition condition = new Condition(Article.class);
        condition.createCriteria().andEqualTo("articleAuthorId", currentuser.getIdUser());
        List<Article> res = articleService.findByCondition(condition);
        if(!res.stream().map(Article::getIdArticle).collect(Collectors.toList()).contains(Long.valueOf(draftId))){
            throw new BusinessException("你小子没有权限");
        }
        articleContentService.findById(draftId).setUpdatedTime(now());
        articleService.updateArticleContent(Long.valueOf(draftId),contentJson);
        try{
            JsonNode root = jacksonObjectMapper.readTree(contentJson);
            log.warn(root.toString());
            // 2) 遍历表单中的所有文件字段，构建 "uploadKey -> 最终URL" 的映射表
            //    字段名形如 "file_img_xyzk", 需提取出 "img_xyzk" 作为 uploadKey
            Map<String, String> replacedUrlMap = new HashMap<>();
            // fileMap 可能包含多个 entry，每个 entry 对应同一个 fieldName
            // 通常本例中 "file_img_xxx" 不会重复 fieldName，但写法稍灵活
            for (String key : fileMap.keySet()) {
                String uploadKey = key.replace("file_", "");
                List<MultipartFile> files = fileMap.get(key);
                if (files == null) continue;
                for (MultipartFile multipartFile : files) {
                    // Ensure the directory exists
                    File directory = new File(imageDownloadUrl+"/articles/"+draftId);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    // Generate a unique key for the file
                    String originalFilename = multipartFile.getOriginalFilename();
                    if (originalFilename != null) {
                        String uniquename = "img_" + UUID.randomUUID().toString().replace("-", "") + originalFilename;
                        File savedFile = new File(directory, uniquename);
                        multipartFile.transferTo(savedFile);
                        String newUrl = urlPrefix + "/articles/"+draftId +"/"+ uniquename;
                        replacedUrlMap.put(uploadKey, newUrl);
                    }
                }
            }
            // 4) 在 JSON 中把所有 src=uploadKey 的图片替换为最终的 URL
            ArticleContent content = articleContentService.findById(draftId);
            log.warn(replacedUrlMap.toString());
            replaceUploadKeyInJson(root, replacedUrlMap);

            if(content==null){
                throw new BusinessException("文章不存在你干鸡毛呢");
            }
            else{
                content.setArticleContent(root.toString());
                articleContentService.update(content);
            }

            return GlobalResultGenerator.genSuccessResult(root);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            throw new BusinessException(e);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(e);
        }
    }

    private void replaceUploadKeyInJson(JsonNode node, Map<String, String> replacedUrlMap) {
        if (node == null || node.isNull()) return;

        // 如果是 "type":"image", 并且 attrs.src 在 replacedUrlMap 中，进行替换
        if (node.has("type") && "image".equals(node.get("type").asText())) {
            JsonNode attrs = node.get("attrs");
            if (attrs != null && attrs.has("src")) {
                String key = attrs.get("src").asText();
                if (replacedUrlMap.containsKey(key)) {
                    String newUrl = replacedUrlMap.get(key);
                    ((ObjectNode) attrs).put("src", newUrl);
                }
            }
        }

        // 继续遍历子节点
        JsonNode contentNode = node.get("content");
        if (contentNode != null && contentNode.isArray()) {
            for (JsonNode child : contentNode) {
                replaceUploadKeyInJson(child, replacedUrlMap);
            }
        }
    }
}
