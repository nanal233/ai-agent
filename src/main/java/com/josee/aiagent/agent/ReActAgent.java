package com.josee.aiagent.agent;

import com.josee.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                // 没有调用任何工具，说明模型是在直接回答（闲聊/简单问题）。
                // 把真实回复内容返回给用户，并结束本轮循环，不再继续追问工具选择。
                List<Message> messageList = getMessageList();
                Message lastMessage = messageList.isEmpty() ? null : messageList.get(messageList.size() - 1);
                String answer = (lastMessage instanceof AssistantMessage assistantMessage)
                        ? assistantMessage.getText()
                        : "思考完成 - 无需行动";
                setState(AgentState.FINISHED);
                return answer;
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
