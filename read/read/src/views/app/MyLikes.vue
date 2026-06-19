<template>
  <div class="my-likes-page">
    <div class="page-header">
      <h2>我的喜欢</h2>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
          v-model="keyword"
          placeholder="搜索标题或书名"
          clearable
          style="width: 250px"
          @clear="fetchLikes"
          @keyup.enter="fetchLikes"
      />
      <el-button type="primary" @click="fetchLikes">搜索</el-button>
    </div>

    <!-- 笔记列表 -->
    <div v-loading="loading" class="notes-list">
      <el-card v-for="note in notes" :key="note.id" class="note-card" shadow="hover" @click="goToDetail(note.id)">
        <div class="note-cover">
          <img :src="note.cover || 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'"/>
        </div>
        <div class="note-info">
          <h3>{{ note.title }}</h3>
          <p class="book">{{ note.bookName }} · {{ note.author }}</p>
          <div class="meta">
            <span><el-icon><View/></el-icon> {{ note.viewCount }}</span>
            <span><el-icon><ThumbUp/></el-icon> {{ note.likeCount }}</span>
            <span><el-icon><ChatLineSquare/></el-icon> {{ note.commentCount }}</span>
          </div>
        </div>
        <div class="action">
          <el-button link type="danger" @click.stop="unlikeNote(note.id)">
            <el-icon>
              <Delete/>
            </el-icon>
            取消喜欢
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
          @size-change="fetchLikes"
          @current-change="fetchLikes"
      />
    </div>

    <el-empty v-if="!loading && notes.length === 0" description="暂无喜欢的笔记"/>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {View, ChatLineSquare, Delete} from '@element-plus/icons-vue'
import {getLikedNotes, unlikeNote as apiUnlikeNote} from '@/api/modules/note'

const router = useRouter()
const loading = ref(false)
const notes = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const keyword = ref('')

const fetchLikes = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    }
    const res = await getLikedNotes(params)
    notes.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载喜欢列表失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/notes/${id}`)
}

const unlikeNote = async (noteId) => {
  try {
    await apiUnlikeNote(noteId)
    ElMessage.success('已取消喜欢')
    fetchLikes()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchLikes()
})
</script>

<style scoped>
.my-likes-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
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