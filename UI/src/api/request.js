import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

const TOKEN_KEY = 'note_token'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截 —— 自动带上 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (err) => Promise.reject(err)
)

// 从各种格式的错误响应中提取 message
function extractMsg(err) {
  const data = err.response?.data
  if (data?.message) return data.message
  if (typeof data === 'string') {
    try {
      const parsed = JSON.parse(data)
      if (parsed?.message) return parsed.message
    } catch (_) { /* 不是 JSON，忽略 */ }
  }
  return null
}

// 响应拦截
request.interceptors.response.use(
  (res) => {
    const { code, message, data } = res.data
    if (code === 200) return data

    ElMessage.error(message || 'Something went wrong')
    return Promise.reject(new Error(message))
  },
  (err) => {
    const status = err.response?.status
    const serverMsg = extractMsg(err)

    // 401/403 —— 登录态过期或无权限
    if (status === 401 || status === 403) {
      localStorage.removeItem(TOKEN_KEY)
      ElNotification({
        type: 'info',
        title: 'Sign in required',
        message: serverMsg && !serverMsg.includes('status code')
          ? serverMsg
          : status === 401
            ? 'Your session has expired. Please sign in again.'
            : "You don't have permission. Please sign in.",
        duration: 3000,
        position: 'top-right',
      })
      // 避免在登录页反复跳转
      const path = window.location.pathname
      if (!path.startsWith('/login') && !path.startsWith('/register')) {
        window.location.href = '/login'
      }
      return Promise.reject(err)
    }

    if (status === 429) {
      ElMessage.warning('Too many attempts, please try later')
      return Promise.reject(err)
    }

    // 其他错误
    const fallback = 'Something went wrong, please try again later.'
    const msg = serverMsg && !serverMsg.toLowerCase().includes('status code')
      ? serverMsg
      : fallback
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
