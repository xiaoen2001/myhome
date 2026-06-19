<template>
  <div class="my-collects-page">
    <div class="page-header">
      <h2>我的收藏</h2>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
          v-model="keyword"
          placeholder="搜索书名"
          clearable
          style="width: 250px"
          @clear="fetchCollects"
          @keyup.enter="fetchCollects"
      />
      <el-button type="primary" @click="fetchCollects">搜索</el-button>
    </div>

    <!-- 收藏列表 -->
    <div v-loading="loading" class="collects-list">
      <el-card v-for="item in collects" :key="item.id" class="collect-card" shadow="hover">
        <div class="collect-cover" @click="goToDetail(item)">
          <img :src="item.cover || 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'"/>
        </div>
        <div class="collect-info">
          <h3 @click="goToDetail(item.noteId)">{{ item.title }}</h3>
          <p class="book">{{ item.bookName }} · {{ item.author }}</p>
          <div class="meta">
            <span>浏览 {{ item.viewCount }}</span>
            <span>点赞 {{ item.likeCount }}</span>
            <span>评论 {{ item.commentCount }}</span>
          </div>
          <div class="actions">
            <el-button type="danger" size="small" @click="removeCollect(item)">取消收藏</el-button>
          </div>
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
          @size-change="fetchCollects"
          @current-change="fetchCollects"
      />
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getMyCollects, removeCollect as apiRemoveCollect} from '@/api/modules/collect'

const router = useRouter()
const loading = ref(false)
const collects = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const keyword = ref('')

const fetchCollects = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    }
    const res = await getMyCollects(params)
    collects.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const removeCollect = (item) => {
  ElMessageBox.confirm(`确定取消收藏「${item.title}」吗？`, '提示', {type: 'warning'}).then(async () => {
    try {
      await apiRemoveCollect(item.id)
      ElMessage.success('已取消收藏')
      fetchCollects()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {
  })
}

const goToDetail = (note) => {
  router.push(`/notes/${note.id}`)
}

onMounted(() => {
  fetchCollects()
})
</script>

<style scoped>
.my-collects-page {
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

.collects-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.collect-card {
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.collect-cover {
  height: 160px;
  overflow: hidden;
}

.collect-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.collect-cover:hover img {
  transform: scale(1.05);
}

.collect-info {
  padding: 16px;
}

.collect-info h3 {
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
  margin-bottom: 12px;
}

.actions {
  margin-top: 8px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>