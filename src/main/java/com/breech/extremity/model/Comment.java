package com.breech.extremity.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "extremity_comments")
public class Comment {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    Long id;
    Long userId;
    String comment;
    Long parentId;
    Integer rootCommentId;
    Date createdAt;
    Date updatedAt;
    Boolean isDeleted;
    Long articleId;
}
