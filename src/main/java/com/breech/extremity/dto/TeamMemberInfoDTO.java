package com.breech.extremity.dto;
import lombok.Data;

@Data
public class TeamMemberInfoDTO {
    private Integer teamMemberId;

    private String account;

    private String nickname;

    private String realName;

    private String sex;

    private String avatarUrl;

    private String email;

    private String phone;

    private String status;

    private String signature;

    private String bgImgUrl;  // 背景？

    private String position;

    private String researchDirection;

    private String personalBio;

    private String researchOverview;
}
