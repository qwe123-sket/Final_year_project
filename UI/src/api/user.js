import request from './request'

export const getProfile = () => request.get('/user/profile')
export const updateProfile = (data) => request.put('/user/profile', data)
export const changePassword = (data) => request.put('/user/password', data)

// 个人中心统计数据
export const getUserStats = () => request.get('/user/stats')
