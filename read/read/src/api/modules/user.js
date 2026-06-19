import request from '@/api/request'

// 个人资料
export const getUserProfile = (userId) => request.get(`/users/profile/${userId}`)
export const putUserProfile = (data) => request.put(`/users/profile`,data)

export const getUserNotes = (userId) => request.get(`/users/notes/${userId}`)
export const checkFriendStatus = (userId) => request.get(`/users/${userId}/friend-status`)
export const addFriendRequest = (userId) => request.post('/friend-requests', { toUserId: userId })

export const getUserById = (id) => request.get(`/users/${id}/profile`)

