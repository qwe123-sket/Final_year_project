import request from './request'

export function listReplies(noteId, params) {
  return request.get(`/notes/${noteId}/replies`, { params })
}

export function createReply(noteId, data) {
  return request.post(`/notes/${noteId}/replies`, data)
}
