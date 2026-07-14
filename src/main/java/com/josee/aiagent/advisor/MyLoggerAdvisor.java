package com.josee.aiagent.advisor;

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志， 只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	// 新增：保存 Builder 传进来的自定义转换函数和顺序
	private final Function<ChatClientRequest, String> requestToString;

	private final Function<ChatResponse, String> responseToString;

	private final int order;

	// 新增：无参构造方法，保留默认用法（比如 new MyLoggerAdvisor()）
	public MyLoggerAdvisor() {
		this(null, null, 0);
	}

	// 新增：给 Builder 调用的全参构造方法
	public MyLoggerAdvisor(Function<ChatClientRequest, String> requestToString,
	                       Function<ChatResponse, String> responseToString,
	                       int order) {
		this.requestToString = requestToString;
		this.responseToString = responseToString;
		this.order = order;
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
	                                             StreamAdvisorChain streamAdvisorChain) {
		logRequest(chatClientRequest);

		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	protected void logRequest(ChatClientRequest request) {
		// 如果通过 Builder 传入了自定义转换函数，优先用它；否则用默认逻辑
		String text = requestToString != null
				? requestToString.apply(request)
				: request.prompt().getUserMessage().getText();
		log.info("AI Request : {}", text);
	}

	protected void logResponse(ChatClientResponse chatClientResponse) {
		String text = responseToString != null
				? responseToString.apply(chatClientResponse.chatResponse())
				: chatClientResponse.chatResponse().getResult().getOutput().getText();
		log.info("AI Response : {}", text);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return this.order;  // 改成用实例变量，而不是写死的 0
	}

	@Override
	public String toString() {
		return MyLoggerAdvisor.class.getSimpleName();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private Function<ChatClientRequest, String> requestToString;

		private Function<ChatResponse, String> responseToString;

		private int order = 0;

		private Builder() {
		}

		public Builder requestToString(Function<ChatClientRequest, String> requestToString) {
			this.requestToString = requestToString;
			return this;
		}

		public Builder responseToString(Function<ChatResponse, String> responseToString) {
			this.responseToString = responseToString;
			return this;
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public MyLoggerAdvisor build() {
			return new MyLoggerAdvisor(this.requestToString, this.responseToString, this.order);
		}
	}
}