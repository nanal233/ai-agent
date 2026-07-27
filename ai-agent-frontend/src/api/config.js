// 接口地址前缀。开发环境下默认走 '/api'，由 vite.config.js 中的代理转发到
// http://localhost:8123，避免浏览器跨域限制；生产环境可通过 VITE_API_BASE_URL 覆盖。
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
