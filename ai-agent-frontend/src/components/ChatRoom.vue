<template>
  <div class="chat-page">
    <header class="chat-header">
      <router-link to="/" class="back-link">← 返回主页</router-link>
      <span class="dot dot-red"></span>
      <span class="dot dot-amber"></span>
      <span class="dot dot-green"></span>
      <h1 class="chat-title">{{ title }}</h1>
      <span v-if="withChatId" class="chat-id">会话ID：{{ chatId }}</span>
    </header>

    <main class="chat-messages neon-scroll" ref="messagesEl">
      <div v-if="messages.length === 0" class="empty-tip">
        向「{{ title }}」发送一条消息，开始对话吧
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-row"
        :class="msg.role"
      >
        <div v-if="msg.role === 'ai'" class="avatar ai-avatar">AI</div>

        <!-- Manus 步骤时间线模式 -->
        <div v-if="msg.role === 'ai' && msg.mode === 'steps'" class="steps-panel">
          <div v-if="msg.steps.length === 0 && !msg.answer && !msg.done" class="typing-dots">
            <i></i><i></i><i></i>
          </div>

          <ol v-if="msg.steps.length" class="step-list">
            <li
              v-for="step in msg.steps"
              :key="step.id"
              class="step-item"
              :class="{ 'step-final': step.isFinal }"
            >
              <span class="step-icon">{{ step.isFinal ? '🏁' : step.tool ? '🔧' : '🧠' }}</span>
              <div class="step-main">
                <div class="step-head">
                  <span class="step-badge">STEP {{ step.stepNo }}</span>
                  <code v-if="step.tool" class="step-tool">{{ step.tool }}</code>
                  <span class="step-status">{{ step.isFinal ? '🏁 任务结束' : '✅ 已完成' }}</span>
                </div>
                <p class="step-desc">{{ step.description }}</p>
                <button
                  v-if="step.result"
                  class="step-toggle"
                  @click="step.expanded = !step.expanded"
                >
                  {{ step.expanded ? '▾ 收起原始结果' : '▸ 查看原始结果' }}
                </button>
                <pre v-if="step.expanded" class="step-result neon-scroll">{{ truncateResult(step.result) }}</pre>
              </div>
            </li>
          </ol>

          <div
            v-if="msg.answer"
            class="answer-block markdown-body"
            v-html="renderMarkdown(msg.answer)"
          ></div>

          <div v-if="!msg.done && (msg.steps.length || msg.answer)" class="step-pending">
            <span class="pending-dot"></span> 正在执行下一步…
          </div>
        </div>

        <!-- 普通流式文本模式 -->
        <div v-else-if="msg.role === 'ai'" class="bubble ai">
          <div
            v-if="msg.content"
            class="bubble-text markdown-body"
            v-html="renderMarkdown(msg.content)"
          ></div>
          <span v-else-if="msg.loading" class="typing-dots"><i></i><i></i><i></i></span>
        </div>

        <div v-else class="bubble user">{{ msg.content }}</div>

        <div v-if="msg.role === 'user'" class="avatar user-avatar">我</div>
      </div>
    </main>

    <footer class="chat-input">
      <textarea
        ref="textareaEl"
        v-model="inputText"
        placeholder="输入消息，按 Enter 发送"
        rows="1"
        @keydown="handleKeydown"
        @input="autoResize"
      ></textarea>
      <button :disabled="sending || !inputText.trim()" @click="sendMessage">
        {{ sending ? '执行中…' : '发送' }}
      </button>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { API_BASE_URL } from '../api/config'
import { renderMarkdown } from '../utils/markdown'
import { parseManusChunk } from '../utils/manusParser'

const props = defineProps({
  title: { type: String, required: true },
  // 后端接口相对路径，例如 /ai/ec_app/chat/sse2
  apiPath: { type: String, required: true },
  // 是否需要在 URL 中携带 chatId 参数（工程师顾问应用需要，超级智能体应用不需要）
  withChatId: { type: Boolean, default: false },
  // 'plain'：原样拼接流式文本；'steps'：解析成 Manus 的步骤时间线
  mode: { type: String, default: 'plain' },
})

const RESULT_PREVIEW_LIMIT = 4000

const chatId = ref('')
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const messagesEl = ref(null)
const textareaEl = ref(null)

let eventSource = null
let msgSeq = 0

function genId() {
  if (window.crypto && typeof window.crypto.randomUUID === 'function') {
    return window.crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

onMounted(() => {
  if (props.withChatId) {
    chatId.value = genId()
  }
})

onBeforeUnmount(() => {
  closeEventSource()
})

function closeEventSource() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = messagesEl.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function autoResize() {
  const el = textareaEl.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 120)}px`
}

function resetTextareaHeight() {
  nextTick(() => {
    const el = textareaEl.value
    if (el) el.style.height = ''
  })
}

function truncateResult(text) {
  if (text.length <= RESULT_PREVIEW_LIMIT) return text
  return `${text.slice(0, RESULT_PREVIEW_LIMIT)}\n\n… (已截断，原始长度 ${text.length} 字符)`
}

function createAiMessage() {
  if (props.mode === 'steps') {
    return reactive({ id: ++msgSeq, role: 'ai', mode: 'steps', steps: [], answer: '', done: false })
  }
  return reactive({ id: ++msgSeq, role: 'ai', mode: 'plain', content: '', loading: true })
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || sending.value) return

  messages.value.push({ id: ++msgSeq, role: 'user', content: text })
  const aiMsg = createAiMessage()
  messages.value.push(aiMsg)

  inputText.value = ''
  resetTextareaHeight()
  sending.value = true
  scrollToBottom()

  const params = new URLSearchParams({ message: text })
  if (props.withChatId) {
    params.set('chatId', chatId.value)
  }

  closeEventSource()
  eventSource = new EventSource(`${API_BASE_URL}${props.apiPath}?${params.toString()}`)

  let stepSeq = 0
  eventSource.onmessage = (event) => {
    if (aiMsg.mode === 'steps') {
      const parsed = parseManusChunk(event.data, ++stepSeq)
      if (parsed.type === 'step') {
        aiMsg.steps.push(reactive({ ...parsed, expanded: false }))
      } else {
        aiMsg.answer += event.data
      }
    } else {
      aiMsg.loading = false
      aiMsg.content += event.data
    }
    scrollToBottom()
  }

  eventSource.onerror = () => {
    if (aiMsg.mode === 'steps') {
      if (!aiMsg.steps.length && !aiMsg.answer) {
        aiMsg.answer = '⚠️ 连接中断或请求失败，请确认后端服务已启动。'
      }
      aiMsg.done = true
    } else {
      if (!aiMsg.content) {
        aiMsg.content = '⚠️ 连接中断或请求失败，请确认后端服务已启动。'
      }
      aiMsg.loading = false
    }
    sending.value = false
    closeEventSource()
  }
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: transparent;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  background: var(--bg-panel);
  border-bottom: 1px solid var(--border-color);
}

.back-link {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  margin-right: 4px;
}

.back-link:hover {
  color: var(--accent-cyan);
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  display: inline-block;
}

.dot-red {
  background: #ff5f56;
}

.dot-amber {
  background: #ffbd2e;
}

.dot-green {
  background: #27c93f;
}

.chat-title {
  font-size: 16px;
  margin: 0;
  flex: 1;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}

.chat-id {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-dim);
  white-space: nowrap;
  background: rgba(255, 255, 255, 0.04);
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.empty-tip {
  margin: auto;
  color: var(--text-dim);
  font-family: var(--font-mono);
  font-size: 13px;
}

.message-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 18px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.ai {
  justify-content: flex-start;
}

.avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-mono);
  font-size: 11px;
  color: #05070d;
  font-weight: 600;
}

.ai-avatar {
  background: linear-gradient(135deg, var(--accent-cyan), var(--accent-purple));
  color: #05070d;
}

.user-avatar {
  background: var(--accent-green);
}

.bubble {
  max-width: 68%;
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.6;
  font-size: 14px;
}

.bubble.user {
  background: linear-gradient(135deg, var(--accent-purple), var(--accent-cyan));
  color: #05070d;
  font-weight: 500;
  border-bottom-right-radius: 2px;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble.ai {
  background: var(--bg-panel);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-bottom-left-radius: 2px;
}

.typing-dots {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 14px;
}

.typing-dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent-cyan);
  animation: typing-blink 1.2s infinite ease-in-out;
}

.typing-dots i:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots i:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-blink {
  0%,
  80%,
  100% {
    opacity: 0.2;
  }
  40% {
    opacity: 1;
  }
}

/* Manus 步骤时间线 */
.steps-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 72%;
  width: 100%;
}

.step-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-item {
  display: flex;
  gap: 10px;
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-left: 3px solid var(--accent-cyan);
  border-radius: 10px;
  padding: 12px 14px;
}

.step-item.step-final {
  border-left-color: var(--accent-green);
}

.step-icon {
  font-size: 16px;
  line-height: 1.4;
}

.step-main {
  flex: 1;
  min-width: 0;
}

.step-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.step-badge {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-dim);
  background: rgba(255, 255, 255, 0.04);
  padding: 2px 6px;
  border-radius: 6px;
}

.step-tool {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--accent-cyan);
  background: rgba(0, 240, 255, 0.08);
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid rgba(0, 240, 255, 0.25);
}

.step-status {
  font-size: 12px;
  color: var(--accent-green);
  margin-left: auto;
  white-space: nowrap;
}

.step-desc {
  margin: 0;
  font-size: 13.5px;
  color: var(--text-secondary);
  line-height: 1.55;
}

.step-toggle {
  margin-top: 8px;
  background: none;
  border: none;
  color: var(--accent-purple);
  font-family: var(--font-mono);
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.step-result {
  margin: 8px 0 0;
  max-height: 220px;
  overflow: auto;
  background: #05070d;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px;
  font-size: 12px;
  font-family: var(--font-mono);
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-break: break-all;
}

.step-pending {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-dim);
  padding-left: 4px;
}

.pending-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent-cyan);
  animation: typing-blink 1s infinite;
}

.answer-block {
  background: var(--bg-panel);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
}

/* Markdown 渲染内容通用样式 */
.markdown-body :deep(p) {
  margin: 0 0 10px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(strong) {
  color: var(--text-primary);
}

.markdown-body :deep(code) {
  font-family: var(--font-mono);
  background: rgba(255, 255, 255, 0.06);
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background: #05070d;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 12px;
  overflow-x: auto;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 1.3em;
  margin: 0 0 10px;
}

.markdown-body :deep(a) {
  color: var(--accent-cyan);
  text-decoration: underline;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 14px 0 8px;
  color: var(--text-primary);
}

.markdown-body :deep(h1:first-child),
.markdown-body :deep(h2:first-child),
.markdown-body :deep(h3:first-child) {
  margin-top: 0;
}

.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 24px;
  background: var(--bg-panel);
  border-top: 1px solid var(--border-color);
}

.chat-input textarea {
  flex: 1;
  resize: none;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.5;
  min-height: 42px;
  max-height: 120px;
  font-family: inherit;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.chat-input textarea::placeholder {
  color: var(--text-dim);
}

.chat-input textarea:focus {
  outline: none;
  border-color: var(--accent-cyan);
  box-shadow: 0 0 0 3px rgba(0, 240, 255, 0.12);
}

.chat-input button {
  height: 42px;
  padding: 0 22px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent-cyan), var(--accent-purple));
  color: #05070d;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.chat-input button:hover:not(:disabled) {
  box-shadow: var(--shadow-glow-cyan);
  transform: translateY(-1px);
}

.chat-input button:disabled {
  background: var(--bg-panel-raised);
  color: var(--text-dim);
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .chat-header {
    padding: 12px 16px;
  }

  .chat-title {
    font-size: 14px;
  }

  .chat-messages {
    padding: 16px;
  }

  .bubble,
  .steps-panel {
    max-width: 88%;
  }

  .chat-input {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .chat-id,
  .dot {
    display: none;
  }

  .avatar {
    width: 26px;
    height: 26px;
    font-size: 10px;
  }

  .bubble,
  .steps-panel {
    max-width: 92%;
    font-size: 13.5px;
  }

  .chat-input button {
    padding: 0 16px;
  }
}
</style>
