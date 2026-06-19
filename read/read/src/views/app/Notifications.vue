<template>
  <div class="notifications-page">
    <div v-loading="loading" class="notification-list">
      <div
          v-for="item in notifications"
          :key="item.id"
          class="notification-item"
          @click="viewDetail(item)"
      >
        <div class="notification-icon">
          <el-icon :size="24">
            <Bell v-if="item.type === 'system'"/>
            <ChatLineSquare v-else-if="item.type === 'comment'"/>
            <ThumbUp v-else-if="item.type === 'like'"/>
            <InfoFilled v-else/>
          </el-icon>
        </div>
        <div class="notification-content">
          <div class="title">{{ item.title }}</div>
          <div class="content">{{ item.content }}</div>
          <div class="time">{{ formatTime(item.createTime) }}</div>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="pagination">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchNotifications"
          @current-change="fetchNotifications"
      />
    </div>

    <el-empty v-if="!loading && notifications.length === 0" description="暂无通知"/>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Bell, ChatLineSquare, InfoFilled} from '@element-plus/icons-vue'
import {getNotifications} from '@/api/modules/notification'

const router = useRouter()
const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const formatTime = (isoString) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  return date.toLocaleString()
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    notifications.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (item) => {
  console.log(item)
  if (item.detailId) {
    router.push(`/announcements/${item.detailId}`)
  } else {
    ElMessage.info('暂无详情内容')
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped>
.notifications-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px;
}

.subtitle {
  font-size: 14px;
  color: #8c9aa6;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.2s;
  animation: fadeInUp 0.5s ease forwards;
}

.notification-item:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-4px);
  box-shadow: 0 15px 30px rgba(79, 172, 254, 0.15);
}

.notification-item.unread {
  background: #f0f7ff;
  border-left: 3px solid #0082c8;
}

.notification-icon {
  color: #0082c8;
}

.notification-content {
  flex: 1;
}

.title {
  font-weight: 600;
  margin-bottom: 4px;
}

.content {
  font-size: 14px;
  color: #4b5563;
  margin-bottom: 6px;
}

.time {
  font-size: 12px;
  color: #9ca3af;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>