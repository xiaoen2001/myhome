import request from '@/api/request'

// 后台统计数据
export const getAdminStats = () => request.get('/admin/stats')
export const getUserTrend = () => request.get('/admin/trend/user')
export const getNoteTrend = () => request.get('/admin/trend/note')
// 用户管理
export const getAdminUsers = (params) => request.get('/admin/users', { params })
export const resetUserPassword = (id) => request.put(`/admin/users/${id}/reset-password`)

export const toggleUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })
// 更新用户信息
export const updateUserInfo = (id, data) => request.put(`/admin/users/${id}`, data)
// 获取评论量趋势（最近6个月）
export const getCommentTrend = () => request.get('/admin/trend/comment')

// 获取浏览量趋势（最近6个月）
export const getViewTrend = () => request.get('/admin/trend/view')

// 轮播图管理
export const getCarousels = () => request.get('/admin/carousels')
export const addCarousel = (data) => request.post('/admin/carousels', data)
export const updateCarousel = (id, data) => request.put(`/admin/carousels/${id}`, data)
export const deleteCarousel = (id) => request.delete(`/admin/carousels/${id}`)
// API 定义 (admin.js)
export const deleteCarouselNewImage = (url) => {
    return request.delete('/admin/carousels/new', { params: { url } })
}
export const uploadCarouselImage = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/admin/carousels/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}
// 分类管理
// 笔记管理
// 笔记管理
export const getAdminNotes = (params) => request.get('/admin/notes', { params })
export const approveNote = (id) => request.put(`/admin/notes/${id}/approve`)
export const rejectNote = (id, reason) => request.put(`/admin/notes/${id}/reject`, { reason })
export const offlineNote = (id) => request.put(`/admin/notes/${id}/offline`)
export const banNote = (id) => request.put(`/admin/notes/${id}/ban`)
export const deleteNote = (id) => request.delete(`/admin/notes/${id}`)
// 标签管理
// 通知管理
// 公告管理
// 后台通知管理
export const getAdminNotifications = (params) => request.get('/admin/notifications', { params })
export const addNotification = (data) => request.post('/admin/notifications', data)
export const deleteNotification = (id) => request.delete(`/admin/notifications/${id}`)
export const getNotificationDetail = (id) => request.get(`/admin/notifications/detail/${id}`)
// 获取评论列表（后台）
export const getAdminComments = (params) => request.get('/admin/comments', { params })

// 删除评论（附带原因）
export const deleteComment = (id, data) => request.delete(`/admin/comments/${id}`, { data })
export const getCategories = () => request.get('/admin/categories')
export const addCategory = (data) => request.post('/admin/categories', data)
export const updateCategory = (id, data) => request.put(`/admin/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/admin/categories/${id}`)

// 标签管理（分页，后台用）
export const getTags = (params) => request.get('/admin/tags', { params })
export const addTag = (data) => request.post('/admin/tags', data)
export const updateTag = (id, data) => request.put(`/admin/tags/${id}`, data)
export const deleteTag = (id) => request.delete(`/admin/tags/${id}`)