<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="admin-aside">
      <div class="logo-wrapper">
        <el-icon size="24" class="logo-icon"><Management/></el-icon>
        <span class="logo-text" v-show="!isCollapse">Read 社区管理</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          text-color="#94a3b8"
          active-text-color="#ffffff"
          background-color="#0f172a"
          class="admin-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine/></el-icon>
          <template #title>运营数据</template>
        </el-menu-item>
        <el-menu-item index="/admin/carousels">
          <el-icon><Picture/></el-icon>
          <template #title>轮播图管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User/></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/notes">
          <el-icon><Document /></el-icon>
          <template #title>笔记管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon><ChatLineSquare /></el-icon>
          <template #title>评论管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Menu /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon>
          <template #title>标签管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon size="20" class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">后台管理</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-actions">
          <!-- 用户信息下拉 -->
          <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
            <div class="user-info">
              <el-avatar
                  :size="32"
                  :src="(userStore.avatar === 'null' || !userStore.avatar) ? '' : userStore.avatar"
                  class="user-avatar"
              >
                <img :src="defaultAvatar" alt="默认头像"/>
              </el-avatar>
              <span class="user-name" v-show="!isMobile">{{ userStore.nickname || userStore.username }}</span>
              <el-icon size="14" class="arrow-icon"><ArrowDown/></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">
                  <el-icon><HomeFilled/></el-icon>
                  <span>返回前台首页</span>
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton/></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="admin-main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {computed, ref, onMounted, onUnmounted} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  Management, ArrowDown, DataLine, Picture, User,
  Fold, Expand, Bell, Setting, SwitchButton, Document, ChatLineSquare, PriceTag, HomeFilled, Menu
} from '@element-plus/icons-vue'
import {useUserStore} from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title || '运营数据')
const defaultAvatar = '/default-avatar.png'
const isCollapse = ref(false)
const isMobile = ref(false)

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      handleLogout()
      break
    case 'home':
      router.push("/home")
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm(
      '确定要退出登录吗？退出后将失去当前登录状态',
      '确认退出',
      {
        type: 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }
  ).then(() => {
    userStore.logout()
    router.push('/login')
    ElMessage.success('退出登录成功')
  }).catch(() => {})
}

const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (isMobile.value) {
    isCollapse.value = true
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
/* 全局布局 */
.admin-layout {
  height: 100vh;
  width: 100vw;
  background-color: #f1f5f9;
}

/* 侧边栏 */
.admin-aside {
  background-color: #0f172a;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 20;
}

.logo-wrapper {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #ffffff;
  background-color: #0f172a;
  border-bottom: 1px solid #1e293b;
  overflow: hidden;
  white-space: nowrap;
}

.logo-icon {
  color: #38bdf8;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.admin-menu {
  border-right: none;
  flex: 1;
}

/* 菜单项美化 */
:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  color: #94a3b8;
}

:deep(.el-menu-item:hover) {
  background-color: #1e293b !important;
  color: #f8fafc !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(118deg, #3b82f6, #2563eb) !important;
  color: #ffffff !important;
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.3);
}

:deep(.el-menu--collapse .el-menu-item) {
  margin: 4px 8px;
  display: flex;
  justify-content: center;
}

/* 顶部导航 */
.admin-header {
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  cursor: pointer;
  color: #64748b;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #3b82f6;
}

.header-actions {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.user-info:hover {
  background-color: #f1f5f9;
}

.user-avatar {
  border: 2px solid #e2e8f0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.arrow-icon {
  color: #94a3b8;
}

/* 主内容区 */
.admin-main {
  background-color: #f1f5f9;
  padding: 24px;
  overflow-y: auto;
  position: relative;
}

/* 路由切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .admin-header {
    padding: 0 16px;
  }
  .admin-main {
    padding: 16px;
  }
}
</style>
