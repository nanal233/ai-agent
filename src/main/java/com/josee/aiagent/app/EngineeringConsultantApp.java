package com.josee.aiagent.app;

import com.josee.aiagent.advisor.MyLoggerAdvisor;
import com.josee.aiagent.chatmemory.FileBasedChatMemory;
import com.josee.aiagent.rag.ECAppRagCustomAdvisorFactory;
import com.josee.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


@Component
@Slf4j
public class EngineeringConsultantApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕后端架构与技术选型领域的资深工程师顾问。你可以告知用户可以在这里咨询技术选型、架构设计或重构决策方面的困惑。" +
            "若用户直接提问或让你完成某项任务，则在你的能力范围内直接回答用户问题或完成任务即可。" +
            "你也可以围绕项目初期选型、现有系统重构、性能与扩展性瓶颈三种场景提问：项目初期选型时询问团队规模、技术栈熟悉度及预期业务量级；" +
            "现有系统重构时询问当前架构的痛点、历史包袱及可承受的迁移成本；" +
            "性能与扩展性瓶颈时询问具体的瓶颈指标、监控数据及已尝试过的优化手段。" +
            "引导用户描述具体的业务场景、团队现状及技术债务，以便给出可落地的技术选型建议和风险提示。";

    public EngineeringConsultantApp(ChatModel dashscopeChatModel) {
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


    record EngineeringConsultantReport(String title, List<String> suggestions) {
    }

    /**
     * AI 报告功能 （ 实战结构化输出 ）
     * @param message
     * @param chatId
     * @return
     */
    public EngineeringConsultantReport doChatWithReport(String message, String chatId) {
        EngineeringConsultantReport engineeringConsultantReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成一个结果，标题为{用户名}的咨询报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .entity(EngineeringConsultantReport.class);
        log.info("EngineeringConsultantReport: {}", engineeringConsultantReport);
        return engineeringConsultantReport;
    }


    // AI 工程师顾问知识库问答功能
    @Resource
    private VectorStore ecAppVectorStore;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 和 RAG 知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答 （基于内存存储）
                .advisors(QuestionAnswerAdvisor.builder(ecAppVectorStore).build())
                // 应用 RAG 检索增强服务 （基于 PGVector 向量存储）
                //.advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                /**
                 *  应用自定义的 RAG 检索增强服务 （文档查询器 + 上下文增强）
                 */
//                .advisors(
//                        ECAppRagCustomAdvisorFactory.createECAppRagCustomAdvisor(
//                                ecAppVectorStore, "项目初期选型"
//                        )
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // AI 工程师顾问调用工具能力
    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // AI 工程师顾问调用 MCP 服务
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


}