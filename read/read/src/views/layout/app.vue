<template>
  <el-container class="layout-container">
    <AppHeader @logout="disconnectWebSocket" />
    <el-main class="main">
      <router-view v-slot="{ Component, route }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </el-main>
    <el-footer class="footer">
      <div class="footer-content">
        <span>读书笔记分享平台 ©2025 版权所有</span>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import {onMounted, onUnmounted} from 'vue'
import {ElNotification} from 'element-plus'
import {useUserStore} from '@/stores/user'
import AppHeader from '@/components/app/Header.vue'

const userStore = useUserStore()

let socket = null;

function connectWebSocket() {
  console.log("键入了")
  const token = userStore.token
  if (!token) return;
  const wsUrl = `ws://localhost:8081/ws/notifications?token=${token}`;
  socket = new WebSocket(wsUrl);
  socket.onopen = () => console.log('WebSocket 连接成功');

  socket.onmessage = (event) => {
    // 直接使用文本，或如果后端返回 JSON 则解析
    const data = event.data;
    let title = '新通知';
    let content = data;
    try {
      const json = JSON.parse(data);
      title = json.title || title;
      content = json.content || data;
    } catch (e) {}
    ElNotification({ title, message: content, type: 'info' });
    window.dispatchEvent(new CustomEvent('new-notification'));
  };

  socket.onerror = (error) => {
    console.error('WebSocket 错误', error);
  };

  socket.onclose = () => {
    console.log('WebSocket 连接关闭');
  };
}

// 退出登录时关闭连接
function disconnectWebSocket() {
  if (socket) {
    socket.close();
    socket = null;
  }
}

onMounted(() => {
  if (!socket){
    connectWebSocket()
  }
  console.log(socket)
});

onUnmounted(() => {
  disconnectWebSocket()
});
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: var(--background);
  display: flex;
  flex-direction: column;
}

/* 主内容区 */
.main {
  padding: 32px 24px;
  max-width: 100%;
  margin: 0 auto;
  width: 100%;
  flex: 1;
  min-height: 0;
  background-color: #f8f9fa;
}

/* 底部样式优化 */
.footer {
  background-color: var(--header-bg) !important;
  color: rgba(255, 255, 255, 0.7);
  margin-top: auto;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
}

.footer-content {
  font-size: 14px;
}
</style>
