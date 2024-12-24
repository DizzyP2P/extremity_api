package com.breech.extremity.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name="extremity_team")
public class TeamInfo implements Serializable, Cloneable{
    @Id
    @Column(name = "team_id")
    @GeneratedValue(generator = "JDBC")
    private Integer teamId;

    /**
     * 团队名称
     */
    @Column(name = "team_name")
    private String teamName;

    /**
     * 研究领域
     */
    @Column(name = "research_field")
    private String researchField;

    /**
     * 团队介绍
     */
    @Column(name = "description")
    private String description;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;
}
