package com.breech.extremity.model;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @author ronger
 */
@Data
@Table(name = "extremity_article")
public class Article implements Serializable, Cloneable {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    private Long idArticle;
    private String articleTitle;
    private String articleThumbnailUrl;
    private Long articleAuthorId;
    private String articleType;
    private String articleTags;
    private Integer articleViewCount;
    private String articlePreviewContent;
    private Integer articleCommentCount;
    /**
     * 0:一般1：精选;
     */
    private String articlePerfect;
    private String articlePermalink;
    private String articleLink;
    private Date createdTime;
    private Date updatedTime;
    private String articleStatus;
    private Integer articleThumbsUpCount;
}
