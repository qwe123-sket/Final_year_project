import request from './request'

export const recordBrowse = (data) => request.post('/browse/record', data)
