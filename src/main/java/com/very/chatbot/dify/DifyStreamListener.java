package com.very.chatbot.dify;

/**
 * Dify 流式回调。
 *
 * 实现类负责把 Dify 协议转成前端约定的简化帧
 * (见 API 文档第 5.1 节简化方案),通过 SseEmitter 推给前端。
 *
 * 本接口独立于具体传输,SseEmitter 句柄由 caller 持有,
 * 便于在 Mock 与真实现之间切换。
 *
 * @author chatbot
 */
public interface DifyStreamListener {

    /**
     * 收到内容增量。
     *
     * @param delta 文本片段(可能为空字符串)
     */
    void onDelta(String delta);

    /**
     * 一轮结束。
     *
     * @param difyConversationId Dify 端会话 id(首轮可能为空,需回写)
     * @param difyMessageId      Dify 端 message id(可空)
     * @param usage              统计信息(JSON 字符串,原样透传)
     */
    void onMessageEnd(String difyConversationId, String difyMessageId, String usage);

    /**
     * 流式过程中发生错误(可恢复 / 不可恢复)。
     *
     * @param code    错误码,对应 API 文档第 1.5 节中的 2001、2002、2003
     * @param message 可读错误信息
     */
    void onError(int code, String message);

    /**
     * 流被主动停止(用户点"停止生成"或前端断流)。
     *
     * @param difyMessageId 最后一次推送的 Dify message id(可空)
     */
    void onStopped(String difyMessageId);
}
