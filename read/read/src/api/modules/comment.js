import request from '@/api/request'

export const getComments = (params) => request.get('/comments', { params })
export const addComment = (data) => request.post('/comments', data)