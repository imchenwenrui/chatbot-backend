-- ============================================
--  数据库: chatbot
--  字符集: utf8mb4 / utf8mb4_0900_ai_ci(MySQL 8.0+)
--  引擎:   InnoDB
--  规范:   t_xxx 命名、snake_case 字段、
--          create_time / update_time / deleted 三件套
--  维护:   项目仓库 chatbot-backend/_doc/sql/chatbot_ddl.sql
--  说明:   本期单用户(匿名),不建 t_user;
--          主键用雪花 Long,
--          Dify 端业务 id 单独冗余存 dify_xxx_id 字段
-- ============================================

CREATE DATABASE IF NOT EXISTS `chatbot`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `chatbot`;

-- --------------------------------------------
--  项目(Project 域)
-- --------------------------------------------
DROP TABLE IF EXISTS `t_project`;
CREATE TABLE `t_project` (
    `id`          BIGINT        NOT NULL                COMMENT '雪花主键',
    `name`        VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '项目名称 1~50 字符',
    `description` VARCHAR(255)  NOT NULL DEFAULT ''     COMMENT '项目描述 0~200 字符',
    `user_id`     VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '所属用户(本期匿名占位)',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                          ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除 0=否 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_project_user_update` (`user_id`, `deleted`, `update_time`),
    KEY `idx_project_update_time` (`update_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='项目表';

-- --------------------------------------------
--  对话(Conversation 域)
-- --------------------------------------------
DROP TABLE IF EXISTS `t_conversation`;
CREATE TABLE `t_conversation` (
    `id`             BIGINT         NOT NULL               COMMENT '雪花主键',
    `project_id`     BIGINT         NOT NULL DEFAULT 0    COMMENT '所属项目 id;0=无项目分组',
    `title`          VARCHAR(255)   NOT NULL DEFAULT ''    COMMENT '会话标题,首版由后端异步生成',
    `user_id`        VARCHAR(64)    NOT NULL DEFAULT ''    COMMENT '所属用户(本期匿名占位)',
    `dify_conv_id`   VARCHAR(64)    NOT NULL DEFAULT ''    COMMENT 'Dify 端 conversation_id,仅后端使用',
    `archived`       TINYINT        NOT NULL DEFAULT 0     COMMENT '是否归档 0=否 1=是(对应 UI 隐藏/恢复)',
    `create_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT        NOT NULL DEFAULT 0     COMMENT '逻辑删除 0=否 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_conv_project_update` (`project_id`, `deleted`, `update_time`),
    KEY `idx_conv_user_update`    (`user_id`,    `deleted`, `update_time`),
    KEY `idx_conv_archived`       (`user_id`, `archived`, `update_time`),
    KEY `idx_conv_dify`           (`dify_conv_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='会话表';

-- --------------------------------------------
--  消息(Message 域)
-- --------------------------------------------
DROP TABLE IF EXISTS `t_message`;
CREATE TABLE `t_message` (
    `id`               BIGINT       NOT NULL                COMMENT '雪花主键',
    `conversation_id`  BIGINT       NOT NULL DEFAULT 0     COMMENT '所属会话 id',
    `role`             VARCHAR(16)  NOT NULL DEFAULT ''     COMMENT 'user / assistant / system',
    `content`          MEDIUMTEXT   NULL                    COMMENT '消息最终内容(流式落地后可空)',
    `feedback`         VARCHAR(16)  NULL                    COMMENT 'like / dislike / NULL',
    `incomplete`       TINYINT      NOT NULL DEFAULT 0      COMMENT '是否被中断 0=否 1=是',
    `dify_message_id`  VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT 'Dify 端 message_id,仅后端使用',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除 0=否 1=是',
    PRIMARY KEY (`id`),
    KEY `idx_msg_conv_create` (`conversation_id`, `deleted`, `create_time`),
    KEY `idx_msg_create`      (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='消息表';
