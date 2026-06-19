import request from '@/api/request'

// 登录
export const login = (data) => {
    return request.post('/auth/login', data)
}

// 注册
export const register = (data) => {
    return request.post('/auth/register', data)
}

// 修改密码
export const updatePassword = (data) => {
    return request.put('/auth/password', data)
}

// 发送重置密码验证码
export const sendResetCode = (data) => request.post('/auth/reset-code', data)

export const confirmCode = (data) => request.post("/auth/confirmCode",data)

// 重置密码
export const resetPassword = (data) => request.post('/auth/reset-password', data)