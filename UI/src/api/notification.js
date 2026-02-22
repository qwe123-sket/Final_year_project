import request from './request'

export const getNotifications = (params) => request.get('/notifications', { params })
export const getUnreadCount = () => request.get('/notifications/unread-count')
export const markAllRead = () => request.post('/notifications/read-all')
