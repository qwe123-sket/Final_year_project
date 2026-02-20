import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

const TOKEN_KEY = 'note_token'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (err) => Promise.reject(err)
)

function extractMessage(err) {
  const data = err.response?.data
  if (data?.message) return data.message
  if (typeof data === 'string') {
    try {
      const o = JSON.parse(data)
      if (o?.message) return o.message
    } catch (_) {}
  }
  return null
}

request.interceptors.response.use(
  (res) => {
    const { code, message, data } = res.data
    if (code === 200) return data
    const displayMsg = message || 'Something went wrong. Please try again.'
    ElMessage.error(displayMsg)
    return Promise.reject(new Error(displayMsg))
  },
  (err) => {
    const status = err.response?.status
    const serverMsg = extractMessage(err)

    // 401/403: show friendly message and redirect to login, do not expose status code
    if (status === 401 || status === 403) {
      localStorage.removeItem(TOKEN_KEY)
      ElNotification({
        type: 'info',
        title: 'Sign in required',
        message: serverMsg && !serverMsg.includes('status code')
          ? serverMsg
          : status === 401
            ? 'Your session has expired. Please sign in again.'
            : "You don't have permission to view this. Please sign in.",
        duration: 3000,
        position: 'top-right',
      })
      if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register')) {
        window.location.href = '/login'
      }
      return Promise.reject(err)
    }

    // 429
    if (status === 429) {
      ElMessage.warning('Too many attempts. Please try again later.')
      return Promise.reject(err)
    }

    // Other errors: prefer server message, avoid exposing "Request failed with status code xxx"
    const fallback = 'Something went wrong. Please try again later.'
    const msg = serverMsg && !serverMsg.toLowerCase().includes('status code') ? serverMsg : fallback
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
