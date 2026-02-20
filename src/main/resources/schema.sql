-- =============================================
-- 笔记推荐系统 - 数据库初始化脚本 (MySQL 8+)
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（若在连接 URL 中已指定库名可跳过）
-- CREATE DATABASE IF NOT EXISTS note_recommend_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE note_recommend_db;

-- ---------------------------------------------
-- 1. 用户表
-- ---------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`     VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`     VARCHAR(128) NOT NULL COMMENT '密码（加密）',
    `email`        VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `nickname`     VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `avatar`       VARCHAR(256) DEFAULT NULL COMMENT '头像 URL',
    `role`         VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER, ADMIN',
    `status`       VARCHAR(16)  NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL, DISABLED, BANNED',
    `created_at`   DATETIME(6)  NOT NULL COMMENT '创建时间',
    `updated_at`   DATETIME(6)  NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ---------------------------------------------
-- 2. 笔记表
-- ---------------------------------------------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT       NOT NULL COMMENT '作者用户 ID',
    `title`          VARCHAR(256) NOT NULL COMMENT '标题',
    `content`        TEXT         DEFAULT NULL COMMENT '正文',
    `status`         VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING, APPROVED, REJECTED',
    `reject_reason`  VARCHAR(512) DEFAULT NULL COMMENT '拒绝原因（敏感词/违规说明）',
    `view_count`     BIGINT       NOT NULL DEFAULT 0 COMMENT '浏览量',
    `created_at`     DATETIME(6)  NOT NULL COMMENT '创建时间',
    `updated_at`     DATETIME(6)  NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_note_user_id` (`user_id`),
    KEY `idx_note_status` (`status`),
    KEY `idx_note_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记表';

-- ---------------------------------------------
-- 3. 回复表
-- ---------------------------------------------
DROP TABLE IF EXISTS `reply`;
CREATE TABLE `reply` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `note_id`    BIGINT      NOT NULL COMMENT '笔记 ID',
    `user_id`    BIGINT      NOT NULL COMMENT '回复用户 ID',
    `content`    TEXT        NOT NULL COMMENT '回复内容',
    `created_at` DATETIME(6) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_reply_note_id` (`note_id`),
    KEY `idx_reply_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='笔记回复表';

-- ---------------------------------------------
-- 4. 收藏表
-- ---------------------------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '用户 ID',
    `note_id`    BIGINT      NOT NULL COMMENT '笔记 ID',
    `created_at` DATETIME(6) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_user_note` (`user_id`, `note_id`),
    KEY `idx_favorite_user_id` (`user_id`),
    KEY `idx_favorite_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- ---------------------------------------------
-- 5. 浏览记录表（供推荐算法使用）
-- ---------------------------------------------
DROP TABLE IF EXISTS `browse_record`;
CREATE TABLE `browse_record` (
    `id`                      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`                 BIGINT      NOT NULL COMMENT '用户 ID',
    `note_id`                 BIGINT      NOT NULL COMMENT '笔记 ID',
    `browse_duration_seconds` BIGINT      NOT NULL DEFAULT 0 COMMENT '浏览时长（秒）',
    `last_browse_at`          DATETIME(6) NOT NULL COMMENT '最后浏览时间',
    `created_at`              DATETIME(6) NOT NULL COMMENT '创建时间',
    `updated_at`              DATETIME(6) NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_browse_user_id` (`user_id`),
    KEY `idx_browse_note_id` (`note_id`),
    KEY `idx_browse_last_at` (`last_browse_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览记录表';

SET FOREIGN_KEY_CHECKS = 1;
