import request from './request'

export const listReplies = (noteId, params) =>
  request.get(`/notes/${noteId}/replies`, { params })
export const createReply = (noteId, data) =>
  request.post(`/notes/${noteId}/replies`, data)
