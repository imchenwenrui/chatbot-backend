-- =====================================================
-- 示例数据（开发联调用，可选）
-- 用户名 anonymous
-- =====================================================

INSERT INTO `conversation`
    (`conversation_id`, `user_id`, `title`, `message_count`, `created_at`, `updated_at`)
VALUES
    ('c1', 'anonymous', 'Vue3 学习问题汇总', 2, '2026-07-02 09:30:00', '2026-07-02 10:25:00'),
    ('c2', 'anonymous', 'Dify 工作流调试', 0, '2026-07-02 11:00:00', '2026-07-02 11:00:00');

INSERT INTO `message`
    (`message_id`, `conversation_id`, `role`, `content`, `status`, `created_at`, `updated_at`)
VALUES
    ('m1', 'c1', 'user',      '解释下 Vue3 的 ref 与 reactive 的区别',                            'done', '2026-07-02 09:31:00', '2026-07-02 09:31:00'),
    ('m2', 'c1', 'assistant', '`ref` 用于包裹基本类型，内部通过 `.value` 访问；`reactive` 用于对象，访问时不需要 `.value`。模板里两者都会被自动解包。', 'done', '2026-07-02 09:31:05', '2026-07-02 09:31:05');
