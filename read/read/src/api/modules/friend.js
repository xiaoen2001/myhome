import request from '@/api/request'

// 获取好友列表
export const getFriends = () => request.get('/friends')

// 获取好友申请列表（收到的）
export const getFriendRequests = () => request.get('/friend-requests')

// 发送好友申请
export const sendFriendRequest = (userId) => request.post('/friend-requests', { toUserId: userId })

// 处理好友申请（同意/拒绝）
export const handleFriendRequest = (requestId, action) => request.put(`/friend-requests/${requestId}`, { action })

// 搜索用户（用于添加好友）
export const searchUsers = (keyword) => request.get('/users/search', { params: { keyword } })

export const checkFriendStatus = (userId) => request.get(`/users/${userId}/friend-status`)

export const addFriendRequest = (userId) => request.post('/friend-requests', { toUserId: userId })

// 获取推荐好友列表
export const getRecommendUsers = () => request.get('/users/recommend')

// 获取最近消息列表
export const getRecentMessages = () => request.get(`/messages/recent`)
// 删除好友
export const deleteFriend = (friendId) => request.delete(`/friends/${friendId}`)