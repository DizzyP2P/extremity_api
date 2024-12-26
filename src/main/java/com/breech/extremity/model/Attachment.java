package com.breech.extremity.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Data
@Table(name = "extremity_attachment")
public class Attachment {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id")
    private Long id;
    private String attachmentUrl;
    private Long articleId;
    private String attachmentName;
}
