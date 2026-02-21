import request from './request'

// 推荐列表（目前是 fallback，等算法接好了后端会返回个性化结果）
export const getRecommendList = (params) =>
  request.get('/recommend/list', { params })
