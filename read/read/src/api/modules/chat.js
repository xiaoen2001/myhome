import request from '@/api/request'

// 获取与某好友的聊天记录
export const getMessages = (params) => request.get('/messages', { params })

// 发送消息
export const sendMessage = (data) => request.post('/messages', data)