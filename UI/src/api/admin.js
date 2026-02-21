import request from './request'

export const getStats = () => request.get('/admin/stats')

export const listPendingNotes = (params) =>
  request.get('/admin/notes/pending', { params })

export const auditNote = (noteId, data) =>
  request.put(`/admin/notes/${noteId}/audit`, data)

export const listUsers = (params) => request.get('/admin/users', { params })

export const updateUserStatus = (userId, data) =>
  request.put(`/admin/users/${userId}/status`, data)

// role 放在 query 参数里
export const updateUserRole = (userId, role) =>
  request.put(`/admin/users/${userId}/role`, null, { params: { role } })
