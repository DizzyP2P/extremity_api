package com.breech.extremity.model;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "extremity_team_user_info")
public class TeamMemberInfo implements Serializable, Cloneable {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer idRole;

    /**
     * 角色职位
     */
    @Column(name = "position")
    private String position;

    /**
     * 研究方向
     */
    @Column(name = "research_direction")
    private String researchDirection;

    /**
     * 个人简介
     */
    @Column(name = "personal_bio")
    private Integer personalBio;

    /**
     * 科研介绍
     */
    @Column(name = "research_overview")
    private String researchOverview;
}
