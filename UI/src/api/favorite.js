import request from './request'

export const addFavorite = (noteId) => request.post(`/favorites/${noteId}`)
export const removeFavorite = (noteId) => request.delete(`/favorites/${noteId}`)
export const myFavorites = (params) => request.get('/favorites/my', { params })
