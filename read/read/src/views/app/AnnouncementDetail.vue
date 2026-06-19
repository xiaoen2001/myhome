<template>
  <div class="announcement-detail-page" v-loading="loading">
    <!-- 公告卡片 -->
    <div class="announcement-card">
      <!-- 返回按钮 -->
      <div class="back-btn-wrapper">
        <el-button @click="goBack" type="primary" link class="back-btn">
          <el-icon>
            <ArrowLeft/>
          </el-icon>
          返回
        </el-button>
      </div>
      <div class="titleCon">
        <h1 class="announcement-title">{{ announcement.title }}</h1>
      </div>
      <!-- 标题区域 -->
      <div class="title-section">

        <div class="meta-info">
          <div class="meta-item">
            <el-icon>
              <User/>
            </el-icon>
            <span>发布者：{{ announcement.author || '管理员' }}</span>
          </div>
          <div class="meta-item">
            <el-icon>
              <Clock/>
            </el-icon>
            <span>发布时间：{{ formatTime(announcement.createTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-section" v-html="announcement.content"></div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ArrowLeft, Clock, User} from '@element-plus/icons-vue'
import {getAnnouncementDetail} from '@/api/modules/notification'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const announcement = ref({})

const announcementId = ref(parseInt(route.params.id))

// 优化时间格式化
const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

const fetchDetail = async () => {
  loading.value = true
  try {
    announcement.value = await getAnnouncementDetail(announcementId.value)
  } catch (error) {
    console.log(error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.announcement-detail-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
  background-color: #f9fafb;
  min-height: 100vh;
}

/* 返回按钮 */
.back-btn-wrapper {
  margin-bottom: 20px;
}

.back-btn {
  color: #1e3a8a !important;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

/* 公告卡片 */
.announcement-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #f3f4f6;
}
.titleCon{
  text-align: center;
}
/* 标题区域 */
.title-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.announcement-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2d3d;
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.meta-info {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  justify-content: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.meta-item el-icon {
  color: #1e3a8a;
}

/* 内容区域 */
.content-section {
  font-size: 16px;
  line-height: 1.8;
  color: #2c3e50;
  margin-bottom: 32px;
}

/* 确保富文本内容样式统一 */
.content-section :deep(p) {
  margin: 0 0 16px 0;
}

.content-section :deep(h2),
.content-section :deep(h3) {
  color: #1f2d3d;
  margin: 24px 0 16px 0;
}

.content-section :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 16px 0;
}

.content-section :deep(a) {
  color: #1e3a8a;
  text-decoration: underline;
}


.el-button {
  --el-button-primary-color: #1e3a8a;
  --el-button-primary-hover-color: #1e40af;
  --el-button-primary-active-color: #1e3a8a;
}
</style>