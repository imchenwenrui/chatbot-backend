# 数据库脚本说明

> 适用：MySQL 8.0+ / utf8mb4 / InnoDB
> 路径：`src/main/resources/sql/`

## 文件清单

| 文件 | 作用 |
| --- | --- |
| `schema.sql` | 建表 DDL，包含 `DROP & CREATE`，幂等可重跑 |
| `data.sql` | 联调示例数据（可选） |

## 表清单

| 表名 | 用途 | 关键字段 |
| --- | --- | --- |
| `conversation` | 左侧栏一项会话 | `conversation_id`、`user_id`、`title`、`dify_conversation_id`、`message_count` |
| `message` | 会话内的消息（用户提问 / 助手回复 / system） | `message_id`、`conversation_id`、`role`、`content`、`status`、`feedback` |

> 反馈（like / dislike）作为字段内联到 `message` 表，未单拆表，避免多次关联。

## 字段一览

### `conversation`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 自增主键，DB 内部使用 |
| `conversation_id` | VARCHAR(64) UNIQUE | 对外暴露的 UUID，前端流转用 |
| `user_id` | VARCHAR(64) | 来自 `X-User-Id` 请求头，便于将来多用户隔离 |
| `title` | VARCHAR(128) | 会话标题，默认 `新对话`，首轮对话后由后端更新为消息摘要 |
| `dify_conversation_id` | VARCHAR(64) | Dify chatflow 的会话 id，首次拿到后写入，后续透传以维持 Dify 上下文 |
| `message_count` | INT | 冗余计数，避免连表 `SELECT COUNT(*)` |
| `created_at` / `updated_at` | DATETIME | 默认 `CURRENT_TIMESTAMP`，`updated_at` 自动更新 |
| `deleted` | TINYINT(1) | 软删标记，0 正常 / 1 已删除 |

### `message`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 自增 |
| `message_id` | VARCHAR(64) UNIQUE | 业务 UUID |
| `conversation_id` | VARCHAR(64) | 关联 `conversation.conversation_id` |
| `role` | VARCHAR(16) | `user` / `assistant` / `system` |
| `content` | MEDIUMTEXT | 消息最终内容（流式拼接完整后落库；Dify 原始 SSE 不落库） |
| `status` | VARCHAR(16) | `pending` / `streaming` / `done` / `stopped` / `failed` |
| `feedback` | VARCHAR(16) | `like` / `dislike` / NULL |
| `dify_message_id` | VARCHAR(64) | Dify 侧 message_id，便于日志回溯 |
| `usage_tokens` | INT | Token 消耗，来自 Dify `metadata.usage` |

## 索引设计

| 表 | 索引 | 说明 |
| --- | --- | --- |
| `conversation` | `uk_conversation_id` | 业务主键唯一 |
| `conversation` | `idx_user_updated (user_id, updated_at)` | 左侧栏列表：按用户查最新会话 |
| `conversation` | `idx_dify_cid (dify_conversation_id)` | 按 Dify 会话反查我们的会话 |
| `message` | `uk_message_id` | 业务主键唯一 |
| `message` | `idx_conv_created (conversation_id, created_at)` | 历史消息按时间排序翻页 |
| `message` | `idx_role (role)` | 按角色过滤（如只统计 user 提问） |

## 执行方式

```bash
mysql -uroot -p < src/main/resources/sql/schema.sql
mysql -uroot -p < src/main/resources/sql/data.sql   # 可选，仅联调需要
```

或在 Spring Boot 中通过 `spring.sql.init.mode=always` 在启动时自动执行（仅 dev 环境推荐）。

## 后续扩展（建议）

- **Dify 工作流日志表 `dify_workflow_log`**：记录每次 chatflow 调用的入参、耗时、tokens，便于排查偶尔出现的卡顿
- **消息反馈详情表 `message_feedback`**：当反馈需要存理由（"为什么点踩"）时再拆，本期不需要
- **用户表 `user`**：引入正式登录时再建，本期用 `X-User-Id` 占位
