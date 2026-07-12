package com.josee.aiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;

/**
 * Spring AI 框架调用 AI 大模型
 */
public class SpringAiAiInvoke {
    @Resource
    private ChatModel dashscopeChatModel;
}
