import request from '@/api/request'

// 获取我的收藏列表
export const getMyCollects = (params) => request.get('/users/me/collects', { params })

// 取消收藏
export const removeCollect = (noteId) => request.delete(`/users/me/collects/${noteId}`)