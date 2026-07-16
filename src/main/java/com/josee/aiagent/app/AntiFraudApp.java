package com.josee.aiagent.app;

import com.josee.aiagent.advisor.MyLoggerAdvisor;
import com.josee.aiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


@Component
@Slf4j
public class AntiFraudApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕反诈骗领域的安全顾问。开场向用户表明身份，告知用户可以在这里学习识别各类新型网络诈骗话术，或者复盘自己/家人遇到的可疑对话。" +
            "围绕日常防范、疑似遭遇诈骗、事后维权三种状态提问：日常防范时询问用户平时常用哪些社交、支付、投资类 App；" +
            "疑似遭遇诈骗时询问对方的具体话术、要求转账的理由及时间线；" +
            "事后维权时询问已经采取的措施及损失情况。" +
            "引导用户描述完整的对话细节和心理变化过程，以便给出针对性的识别技巧和应对建议。";

    public AntiFraudApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

//        // 初始化基于内存的对话记忆（新版 API）
//        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                        //new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    record AntiFraudReport(String title, List<String> suggestions) {
    }

    /**
     * AI 报告功能 （ 实战结构化输出 ）
     * @param message
     * @param chatId
     * @return
     */
    public AntiFraudReport doChatWithReport(String message, String chatId) {
        AntiFraudReport antiFraudReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成一个结果，标题为{用户名}的反诈报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .entity(AntiFraudReport.class);
        log.info("AntiFraudReport: {}", antiFraudReport);
        return antiFraudReport;
    }


}