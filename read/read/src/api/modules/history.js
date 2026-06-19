import request from '@/api/request'

// 获取浏览历史
export const getHistory = (params) => request.get('/users/me/history', { params })

// 移除单个浏览记录
export const removeHistoryItem = (noteId) => request.delete(`/users/me/history/${noteId}`)

// 清空所有浏览记录
export const clearHistory = () => request.delete('/users/me/history')

export const addHistory = (data) => request.post('/users/me/history', data)