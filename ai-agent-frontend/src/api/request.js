import axios from 'axios'
import { API_BASE_URL } from './config'

// 统一的 axios 实例，供后续新增的普通 REST 接口使用。
// 当前的对话接口均通过 SSE（EventSource）实时推流，见 src/components/ChatRoom.vue。
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
})

export default request
