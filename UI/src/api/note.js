import request from './request'

// 笔记相关接口
export const listNotes = (params) => request.get('/notes/list', { params })
export const searchNotes = (params) => request.get('/notes/search', { params })
export const getNote = (id) => request.get(`/notes/${id}`)
export const myNotes = (params) => request.get('/notes/my', { params })
export const createNote = (data) => request.post('/notes', data)
export const updateNote = (id, data) => request.put(`/notes/${id}`, data)
export const deleteNote = (id) => request.delete(`/notes/${id}`)
export const recordView = (id) => request.post(`/notes/${id}/view`)

// 点赞
export const likeNote = (id) => request.post(`/notes/${id}/like`)
export const unlikeNote = (id) => request.delete(`/notes/${id}/like`)

// 热门
export const getTrending = (params) => request.get('/notes/trending', { params })
export const getHotTags = (params) => request.get('/notes/tags/hot', { params })
