import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'   // 导入 router 实例

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器：添加 token
request.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = `Bearer ${userStore.token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// 统一的未授权处理函数
const handleUnauthorized = () => {
    const userStore = useUserStore()
    userStore.logout()
    router.push('/login')
    ElMessage.error('登录已过期，请重新登录')
}

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        // 统一判断：code === 200 或 1 表示成功（根据你的后端定义）
        if (res.code === 200 || res.code === 1) {
            return res.data
        }
        // 业务状态码 401 表示未授权
        else if (res.code === 401) {
            handleUnauthorized()
            return Promise.reject(new Error(res.msg || '未授权'))
        }
        else {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg))
        }
    },
    error => {
        // 处理 HTTP 状态码 401
        if (error.response && error.response.status === 401) {
            handleUnauthorized()
            return Promise.reject(new Error('登录已过期'))
        }
        // 其他网络错误
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request