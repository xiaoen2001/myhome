import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || null)
    const userId = ref(null)
    const username = ref('')
    const nickname = ref('')
    const avatar = ref('')
    const role = ref('')

    const setUser = (userInfo) => {
        token.value = userInfo.token
        userId.value = userInfo.id
        username.value = userInfo.username
        nickname.value = userInfo.nickname || userInfo.username
        avatar.value = userInfo.avatar || ''
        role.value = userInfo.role || 'user'
    }

    const logout = () => {
        token.value = null
        userId.value = null
        username.value = ''
        nickname.value = ''
        avatar.value = ''
        role.value = ''
    }
    //持久化，刷新还是登录状态
    return { token, userId, username, nickname, avatar, role, setUser, logout }
}, {
    persist: {
        key: 'user-store',
        storage: localStorage,
        paths: ['token', 'userId', 'username', 'nickname', 'avatar', 'role']
    }
})