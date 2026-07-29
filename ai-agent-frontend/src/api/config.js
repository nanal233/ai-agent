// 接口地址前缀。前后端分开部署在不同域名下，生产环境需要写后端的完整地址；
// 也支持用 VITE_API_BASE_URL 环境变量覆盖（比如后端换了新域名，不用改这里的代码）。
const PROD_API_BASE_URL = 'https://ai-agent-backend-289019-8-1460461116.sh.run.tcloudbase.com/api'
const DEV_API_BASE_URL = 'http://localhost:8123/api'

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  (import.meta.env.PROD ? PROD_API_BASE_URL : DEV_API_BASE_URL)
