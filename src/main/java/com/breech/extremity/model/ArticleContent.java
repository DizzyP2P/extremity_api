package com.breech.extremity.model;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author ronger
 */
@Data
@Table(name = "extremity_article_content")
public class ArticleContent {
    @Id
    @Column(name = "id_article")
    @GeneratedValue(generator = "JDBC")
    private Long idArticle;
    private String articleContent;
    private Date createdTime;
    private Date updatedTime;
}
