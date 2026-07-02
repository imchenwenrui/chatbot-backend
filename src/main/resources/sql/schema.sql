-- =====================================================
-- Chatbot 数据库 Schema
-- 适用数据库：MySQL 8.0+
-- 字符集：utf8mb4 / utf8mb4_unicode_ci
-- =====================================================

-- DROP DATABASE IF EXISTS chatbot;
-- CREATE DATABASE chatbot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE chatbot;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 1. 会话表
-- 对应前端左侧栏一项
-- -----------------------------------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `conversation_id`    VARCHAR(64)  NOT NULL                COMMENT '对外暴露的业务 UUID',
    `user_id`            VARCHAR(64)  NOT NULL DEFAULT 'anonymous' COMMENT '所属用户 X-User-Id',
    `title`              VARCHAR(128) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    `dify_conversation_id` VARCHAR(64) DEFAULT NULL          COMMENT 'Dify chatflow 会话 id，首次返回后写入',
    `message_count`      INT          NOT NULL DEFAULT 0     COMMENT '冗余字段：消息数（避免连表计数）',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`            TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除：0 正常 / 1 已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_id` (`conversation_id`),
    KEY `idx_user_updated` (`user_id`, `updated_at`),
    KEY `idx_dify_cid` (`dify_conversation_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话表';


-- -----------------------------------------------------
-- 2. 消息表
-- 包含用户提问和助手回复；通过 conversation_id 关联会话
-- -----------------------------------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `message_id`      VARCHAR(64)   NOT NULL                COMMENT '对外暴露的业务 UUID',
    `conversation_id` VARCHAR(64)   NOT NULL                COMMENT '关联 conversation.conversation_id',
    `role`            VARCHAR(16)   NOT NULL                COMMENT 'user / assistant / system',
    `content`         MEDIUMTEXT    NOT NULL                COMMENT '消息最终内容（流式拼接后的完整文本）',
    `status`          VARCHAR(16)   NOT NULL DEFAULT 'done' COMMENT 'pending / streaming / done / stopped / failed',
    `feedback`        VARCHAR(16)   DEFAULT NULL            COMMENT 'like / dislike / NULL',
    `dify_message_id` VARCHAR(64)   DEFAULT NULL            COMMENT 'Dify 侧 message_id，便于排查',
    `usage_tokens`    INT           DEFAULT NULL            COMMENT 'Token 消耗（首版来自 Dify metadata.usage）',
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_conv_created` (`conversation_id`, `created_at`),
    KEY `idx_role` (`role`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表';


SET FOREIGN_KEY_CHECKS = 1;
