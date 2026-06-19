<template>
  <div class="history-page">
    <div class="page-header">
      <h2>浏览历史</h2>
      <el-button type="danger" plain @click="clearHistory" :disabled="notes.length === 0">清空历史</el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
          v-model="keyword"
          placeholder="搜索标题或书名"
          clearable
          style="width: 250px"
          @clear="fetchHistory"
          @keyup.enter="fetchHistory"
      />
      <el-button type="primary" @click="fetchHistory">搜索</el-button>
    </div>

    <!-- 笔记列表 -->
    <div v-loading="loading" class="notes-list">
      <el-card v-for="item in notes" :key="item.id" class="note-card" shadow="hover" @click="goToDetail(item)">
        <div class="note-cover">
          <img :src="item.cover || 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'"/>
        </div>
        <div class="note-info">
          <h3>{{ item.title }}</h3>
          <p class="book">{{ item.bookName }} · {{ item.author }}</p>
          <div class="meta">
            <span><el-icon><View/></el-icon> {{ item.viewCount }}</span>
            <span><el-icon><ThumbUp/></el-icon> {{ item.likeCount }}</span>
            <span><el-icon><ChatLineSquare/></el-icon> {{ item.commentCount }}</span>
          </div>
          <div class="time">浏览时间：{{ formatTime(item.browsedAt) }}</div>
        </div>
        <div class="action">
          <el-button link type="danger" @click.stop="removeFromHistory(item.id)">
            <el-icon>
              <Delete/>
            </el-icon>
            移除
          </el-button>
        </div>
      </el-card>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchHistory"
          @current-change="fetchHistory"
      />
    </div>

    <el-empty v-if="!loading && notes.length === 0" description="暂无浏览记录"/>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {View, ChatLineSquare, Delete} from '@element-plus/icons-vue'
import {getHistory, clearHistory as apiClearHistory, removeHistoryItem} from '@/api/modules/history'

const router = useRouter()
const loading = ref(false)
const notes = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const keyword = ref('')

const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const fetchHistory = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    }
    const res = await getHistory(params)
    console.log(res)
    notes.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载历史失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (node) => {
  router.push(`/notes/${node.id}`)
}

const removeFromHistory = async (noteId) => {
  try {
    await removeHistoryItem(noteId)
    ElMessage.success('已移除')
    fetchHistory()
  } catch (error) {
    ElMessage.error('移除失败')
  }
}

const clearHistory = async () => {
  ElMessageBox.confirm('确定清空所有浏览记录吗？', '提示', {type: 'warning'}).then(async () => {
    try {
      await apiClearHistory()
      ElMessage.success('已清空')
      fetchHistory()
    } catch (error) {
      ElMessage.error('清空失败')
    }
  }).catch(() => {
  })
}

onMounted(() => {
  fetchHistory()
})
</script>

<style scoped>
.history-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
}

.notes-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.note-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s;
}

.note-card:hover {
  transform: translateY(-4px);
}

.note-cover {
  height: 160px;
  overflow: hidden;
}

.note-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.note-info {
  padding: 16px;
}

.note-info h3 {
  font-size: 18px;
  margin-bottom: 6px;
}

.book {
  font-size: 14px;
  color: #6b7a86;
  margin-bottom: 8px;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #8c9aa6;
  margin-bottom: 6px;
}

.time {
  font-size: 12px;
  color: #8c9aa6;
}

.action {
  padding: 0 16px 16px;
  text-align: right;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>