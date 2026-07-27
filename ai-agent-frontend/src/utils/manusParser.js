// Manus 智能体的 SSE 每条 event.data 都是一整段已经执行完的步骤报告，形如：
//   "Step 1: 思考完成 - 无需行动"
//   "Step 2: 调用 scrapeWebPage 完成，正在处理任务！结果: {...很长的 JSON/HTML...}"
//   "Step 3: 工具 executeTerminalCommand 完成了它的任务！结果: \"...\""
// 这里把工具名 / 步骤说明 / 原始结果拆开，方便前端只展示"做了什么"，隐藏冗长的原始结果。
const STEP_RE = /^Step\s*(\d+)\s*[:：]\s*([\s\S]*)$/
const RESULT_RE = /^([\s\S]*?)结果\s*[:：]\s*([\s\S]*)$/
const TOOL_RE_LIST = [/调用\s*([A-Za-z0-9_]+)/, /工具\s*([A-Za-z0-9_]+)/]

export function parseManusChunk(raw, seq) {
  const stepMatch = raw.match(STEP_RE)
  if (!stepMatch) {
    return { id: seq, type: 'answer', raw }
  }

  const stepNo = Number(stepMatch[1])
  let description = stepMatch[2]
  let result = null

  const resultMatch = description.match(RESULT_RE)
  if (resultMatch) {
    description = resultMatch[1].trim()
    result = resultMatch[2]
  }

  let tool = null
  for (const re of TOOL_RE_LIST) {
    const m = description.match(re)
    if (m) {
      tool = m[1]
      break
    }
  }

  return {
    id: seq,
    type: 'step',
    stepNo,
    tool,
    isFinal: tool === 'doTerminate',
    description,
    result,
  }
}
