import request from '@/api/request'

// 获取轮播图
export const getCarousels = () => request.get('/carousels')

// 获取推荐笔记
export const getRecommendNotes = (type) => request.get('/notes/recommend', { params: { type } })

export const getNotes = (params) => request.get('/notes', { params })
export const getCategories = () => request.get('/categories')
export const getTags = () => request.get('/tags')
// 获取笔记详情
export const getNoteDetail = (id) => request.get(`/notes/${id}`)
// 获取笔记评论
// 发表评论
// 点赞/取消点赞
export const likeNote = (id) => request.post(`/notes/${id}/like`)
// 收藏/取消收藏
export const collectNote = (id) => request.post(`/notes/${id}/collect`)
export const uncollectNote = (id) => request.delete(`/notes/${id}/collect`)

// 获取我的笔记列表（支持分页、状态筛选）
export const getMyNotes = (params) => request.get('/users/me/notes', { params })

// 创建笔记
export const createNote = (data) => request.post('/notes', data)

// 更新笔记（公开接口，编辑后需重新审核）
export const updateNote = (id, data) => request.put(`/notes/${id}`, data)

// 更新我的笔记（不改变审核状态）
export const updateMyNote = (id, data) => request.put(`/users/me/notes/${id}`, data)

// 切换笔记可见性
export const updateNoteVisibility = (id, visibility) => request.put(`/users/me/notes/${id}/visibility`, { visibility })

// 删除笔记
export const deleteNote = (id) => request.delete(`/notes/${id}`)

// 提交审核
export const submitForReview = (id) => request.post(`/notes/${id}/submit`)

// 下架笔记
export const offlineNote = (id) => request.post(`/notes/${id}/offline`)

// 获取用户点赞过的笔记列表
export const getLikedNotes = (params) => request.get('/users/me/likes', { params })

// 取消点赞（已在互动模块中可能有，如果没有则添加）
export const unlikeNote = (noteId) => request.delete(`/notes/${noteId}/like`)