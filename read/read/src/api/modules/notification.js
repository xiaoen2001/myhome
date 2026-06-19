import request from '@/api/request'

// 获取通知列表
export const getNotifications = (params) => request.get('/notifications', { params })

// 标记单个通知为已读
export const markNotificationAsRead = (id) => request.put(`/notifications/${id}/read`)

// 删除通知
export const deleteNotification = (id) => request.delete(`/notifications/${id}`)

// 标记全部已读
export const markAllAsRead = () => request.put('/notifications/read-all')

// 获取公告详情
export const getAnnouncementDetail = (id) => request.get(`/announcements/${id}`)