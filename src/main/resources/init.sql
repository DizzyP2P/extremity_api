create database extremity default character set utf8mb4 collate utf8mb4_unicode_ci;
use extremity;

create table extremity_article
(
    id                      bigint auto_increment comment '主键'
        primary key,
    article_title           varchar(128)     null comment '文章标题',
    article_thumbnail_url   varchar(128)     null comment '文章缩略图',
    article_author_id       bigint           null comment '文章作者id',
    article_type            char default '0' null comment '文章类型',
    article_tags            varchar(128)     null comment '文章标签',
    article_view_count      int  default 1   null comment '浏览总数',
    article_preview_content varchar(256)     null comment '预览内容',
    article_comment_count   int  default 0   null comment '评论总数',
    article_permalink       varchar(128)     null comment '文章永久链接',
    article_link            varchar(32)      null comment '站内链接',
    created_time            datetime         null comment '创建时间',
    updated_time            datetime         null comment '更新时间',
    article_perfect         char default '0' null comment '0:非优选1：优选',
    article_status          char default '0' null comment '文章状态',
    article_thumbs_up_count int  default 0   null comment '点赞总数'
) comment '文章表 ' collate = utf8mb4_unicode_ci;

create table extremity_article_content
(
    id_article           bigint   not null comment '主键',
    article_content      text     null comment '文章内容原文',
    article_content_html text     null comment '文章内容Html',
    created_time         datetime null comment '创建时间',
    updated_time         datetime null comment '更新时间'
) comment ' ' collate = utf8mb4_unicode_ci;

create table extremity_user
(
    id               bigint auto_increment comment '用户ID'
        primary key,
    account          varchar(32)      null comment '账号',
    password         varchar(64)      not null comment '密码',
    nickname         varchar(128)     null comment '昵称',
    real_name        varchar(32)      null comment '真实姓名',
    sex              char default '0' null comment '性别',
    avatar_type      char default '0' null comment '头像类型',
    avatar_url       varchar(512)     null comment '头像路径',
    email            varchar(64)      null comment '邮箱',
    phone            varchar(11)      null comment '电话',
    status           char default '0' null comment '状态',
    created_time     datetime         null comment '创建时间',
    updated_time     datetime         null comment '更新时间',
    last_login_time  datetime         null comment '最后登录时间',
    signature        varchar(128)     null comment '签名',
    last_online_time datetime         null comment '最后在线时间',
    bg_img_url       varchar(512)     null comment '背景图片'
) comment '用户表 ' collate = utf8mb4_unicode_ci;

create table extremity_user_role
(
    id_user      bigint   not null comment '用户表主键',
    id_role      bigint   not null comment '角色表主键',
    created_time datetime null comment '创建时间'
) comment '用户权限表 ' collate = utf8mb4_unicode_ci;

create table extremity_role
(
    id           bigint auto_increment comment '主键'
        primary key,
    name         varchar(32)         null comment '名称',
    input_code   varchar(32)         null comment 'English',
    status       char    default '0' null comment '状态',
    created_time datetime            null comment '创建时间',
    updated_time datetime            null comment '更新时间',
    weights      tinyint default 0   null comment '权重,数值越小权限越大;0:无权限'
) comment ' ' collate = utf8mb4_unicode_ci;


INSERT INTO extremity_article (id, article_title, article_thumbnail_url, article_author_id, article_type,
                               article_tags, article_view_count, article_preview_content, article_comment_count,
                               article_permalink, article_link, created_time, updated_time, article_perfect,
                               article_status, article_thumbs_up_count)
VALUES (1, '给新人的一封信', null, 1, '0', '公告,新手信', 3275,
        '您好，欢迎来到 RYMCU 社区，RYMCU 是一个嵌入式知识学习交流平台。RY 取自”容易”的首字母，寓意为让电子设计变得 so easy。新手的疑问初学者都有很多疑问，在这里对这些疑问进行一一解答。我英语不好，可以学习编程吗？对于初学者来说，英语不是主要的障碍，国内有着充足的中文教程。但在接下来的学习过程中，需要阅读大量的英文文档，所以还是需要有一些英语基础和理解学习能力，配合翻译工具（如百度',
        0, 'http://localhost:3000/article/1', '/article/1', '2020-01-03 01:27:25', '2022-09-26 15:33:03', '0', '0', 7);



INSERT INTO `extremity_role` (`id`, `name`, `input_code`, `status`, `created_time`, `updated_time`, `weights`) VALUES (1, '高级管理员', 'topop', '0', NULL, NULL, 1);
INSERT INTO `extremity_role` (`id`, `name`, `input_code`, `status`, `created_time`, `updated_time`, `weights`) VALUES (4, '普通用户', 'user', '0', NULL, NULL, 4);
INSERT INTO `extremity_role` (`id`, `name`, `input_code`, `status`, `created_time`, `updated_time`, `weights`) VALUES (5, '未认证用户', 'unauthorized_user', '0', NULL, NULL, 5);
INSERT INTO `extremity_user` (`id`, `account`, `password`, `nickname`, `real_name`, `sex`, `avatar_type`, `avatar_url`, `email`, `phone`, `status`, `created_time`, `updated_time`, `last_login_time`, `signature`, `last_online_time`, `bg_img_url`) VALUES (3, 'pigremaaaa', '7b98d552b94583de9cefcaf7528a46f84de473607e067e2b7eddc6d0', 'pigremaaaa', NULL, '0', '0', 'https://tse4-mm.cn.bing.net/th/id/OIP-C._r7lZRECo5odDY5N9ufpQwHaNG?w=724&h=1280&rs=1&pid=ImgDetMain', 'pigremaaaa@gmail.com', NULL, '0', '2024-12-20 14:16:46', '2024-12-20 14:16:46', NULL, NULL, NULL, NULL);
INSERT INTO `extremity_user` (`id`, `account`, `password`, `nickname`, `real_name`, `sex`, `avatar_type`, `avatar_url`, `email`, `phone`, `status`, `created_time`, `updated_time`, `last_login_time`, `signature`, `last_online_time`, `bg_img_url`) VALUES (4, 'pigremaaaa', '20d3e435304362e0ee8a9e94c3b0557566637d845bc0e538cc6d273e', 'pigremaaaa', NULL, '0', '0', '', 'pigremaaaa@163.com', NULL, '0', '2024-12-20 14:41:58', '2024-12-20 14:41:58', NULL, NULL, NULL, NULL);