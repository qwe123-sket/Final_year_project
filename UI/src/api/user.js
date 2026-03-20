import request from './request'

export const getProfile = () => request.get('/user/profile')
export const updateProfile = (data) => request.put('/user/profile', data)
export const changePassword = (data) => request.put('/user/password', data)

// 个人中心统计数据
export const getUserStats = () => request.get('/user/stats')
export const getUserDashboard = () => request.get('/user/dashboard')

// ========== Public user APIs (search dropdown + public profile) ==========
export const searchUsers = (params) => request.get('/user/search', { params })

export const getPublicProfile = (userId) => request.get(`/user/public/${userId}`)

export const getPublicUserNotes = (userId, params) =>
  request.get(`/user/public/${userId}/notes`, { params })
