import request from './request'

export const getRecommendList = (params) =>
  request.get('/recommend/list', { params })
