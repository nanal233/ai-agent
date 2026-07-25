package com.josee.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChainAiInvoke {
    public static void main(String[] args) {
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(testApiKey.API_KEY)
                .modelName("qwen3.5-plus")
                .build();
        String result = qwenChatModel.chat("你好，我是Josee");
        System.out.println(result);
    }
}
