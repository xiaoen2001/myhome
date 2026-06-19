<template>
  <header class="glass-header">
    <!-- 补回遗失的安全容器，防止内容被推到屏幕外面去 -->
    <div class="header-content">
      <div class="logo-box" @click="$router.push('/home')">
        <el-icon :size="28" class="logo-icon"><Reading /></el-icon>
        <span class="logo-text">Read 社区</span>
      </div>
      <nav class="nav-links">
        <router-link to="/home" class="nav-item"><el-icon><House /></el-icon> 首页</router-link>
        <router-link to="/notes" class="nav-item"><el-icon><Collection /></el-icon> 发现笔记</router-link>
        <router-link to="/notifications" class="nav-item"><el-icon><Bell /></el-icon> 系统通知</router-link>
        <router-link to="/friends" class="nav-item"><el-icon><User /></el-icon> 我的好友</router-link>
        <router-link v-if="userStore.role === 'admin'" to="/admin" class="nav-item"><el-icon><Setting /></el-icon> 后台管理</router-link>
      </nav>
      <div class="user-actions">
        <el-dropdown @command="handleCommand">
          <div class="user-dropdown">
            <el-avatar
                :size="36"
                :src="(userStore.avatar === 'null' || !userStore.avatar) ? '' : userStore.avatar"
                class="user-avatar"
            >
              <img :src="defaultAvatar" alt="默认头像"/>
            </el-avatar>
            <span class="username">{{ userStore.nickname || userStore.username }}</span>
            <el-icon class="arrow-icon"><ArrowDown/></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown-menu">
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="myNotes">我的笔记</el-dropdown-item>
              <el-dropdown-item command="myCollects">我的收藏</el-dropdown-item>
              <el-dropdown-item command="myLikes">我的喜欢</el-dropdown-item>
              <el-dropdown-item command="history">浏览历史</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<script setup>
import { Reading, House, Collection, Bell, User, Setting, ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = '/default-avatar.png'

const emit = defineEmits(['logout'])

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push(`/profile/${userStore.userId}`)
      break
    case 'myNotes':
      router.push('/my-notes')
      break
    case 'myCollects':
      router.push('/my-collects')
      break
    case 'history':
      router.push('/history')
      break
    case 'myLikes':
      router.push('/my-likes')
      break
    case 'password':
      router.push('/change-password')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {type: 'warning'}).then(() => {
        userStore.logout()
        emit('logout')
        router.push('/login')
        ElMessage.success('已退出登录')
      }).catch(() => {})
      break
  }
}
</script>

<style scoped>
/* === Taste-Skill Header: sticky, 64px cap, one-line nav === */
.glass-header {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  height: 64px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 64px;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* Logo */
.logo-box {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #3730a3;
  cursor: pointer;
  transition: opacity 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.logo-box:hover {
  opacity: 0.82;
}
.logo-text {
  font-size: 22px;
  font-weight: 800;
  color: #3730a3;
  letter-spacing: -0.022em;
}

/* Nav */
.nav-links {
  display: flex;
  gap: 4px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: #475569;
  font-weight: 600;
  font-size: 14px;
  padding: 8px 14px;
  border-radius: 8px;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.nav-item:hover {
  color: #3730a3;
  background: #eef2ff;
}
.nav-item.router-link-active {
  color: #3730a3;
  background: #eef2ff;
}

/* User */
.user-actions {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 999px;
  transition: background 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.user-dropdown:hover {
  background: #f1f5f9;
}
.user-dropdown:active {
  transform: scale(0.98);
}

.user-avatar {
  border: 2px solid #e2e8f0;
}

.username {
  font-size: 15px;
  color: #334155;
  font-weight: 600;
}

.arrow-icon {
  color: #94a3b8;
  font-size: 13px;
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}
.user-dropdown:hover .arrow-icon {
  transform: rotate(180deg);
}

.user-dropdown-menu {
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.10);
  border: 1px solid #e2e8f0;
  padding: 6px 0;
}
</style>
