import request from './request'

// 上报浏览记录，给推荐算法用
export const recordBrowse = (data) => request.post('/browse/record', data)
