import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as apiLogin, register as apiRegister } from '@/api/auth'
import { getProfile } from '@/api/user'

const TOKEN_KEY = 'note_token'
const USER_KEY = 'note_user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  // 同步 token 和用户信息到 localStorage
  function setAuth(t, u) {
    token.value = t
    userInfo.value = u
    if (t) {
      localStorage.setItem(TOKEN_KEY, t)
      localStorage.setItem(USER_KEY, JSON.stringify(u || {}))
    } else {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }

  async function login(formData) {
    const res = await apiLogin(formData)
    setAuth(res.token, {
      userId: res.userId,
      username: res.username,
      role: res.role,
    })
    return res
  }

  async function register(formData) {
    const res = await apiRegister(formData)
    setAuth(res.token, {
      userId: res.userId,
      username: res.username,
      role: res.role,
    })
    return res
  }

  // 拉取完整的用户资料并合并到 store
  async function fetchProfile() {
    const profile = await getProfile()
    userInfo.value = { ...userInfo.value, ...profile }
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
    return userInfo.value
  }

  function logout() {
    setAuth('', null)
  }

  return { token, userInfo, isLogin, isAdmin, login, register, fetchProfile, logout, setAuth }
})
