<template>
  <div class="notes-page">
    <!-- 筛选卡片 -->
    <div class="filter-card">
      <div class="filter-row">
        <div class="search-wrapper">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索标题、书名或作者"
              clearable
              prefix-icon="Search"
              size="large"
              class="search-input"
              @keyup.enter="handleSearch"
          />
          <el-button type="primary" size="large" class="search-btn" @click="handleSearch">
            <el-icon class="search-icon"><Search /></el-icon>
            搜索
          </el-button>
        </div>

        <div class="filter-select-group">
          <el-select
              v-model="selectedCategory"
              placeholder="全部分类"
              clearable
              @change="handleSearch"
              class="filter-select"
              size="large"
          >
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </div>
      </div>

      <!-- 排序行 -->
      <div class="sort-row">
        <span class="sort-label">排序方式：</span>
        <el-radio-group v-model="sortType" @change="handleSearch" class="sort-radio-group">
          <el-radio-button label="latest">最新</el-radio-button>
          <el-radio-button label="hot">最热</el-radio-button>
          <el-radio-button label="mostCommented">最多评论</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 笔记列表 -->
    <div v-loading="loading" loading-text="正在加载笔记..." class="notes-grid">
      <el-card
          v-for="note in notes"
          :key="note.id"
          class="note-card"
          shadow="none"
          @click="goToDetail(note.id)"
      >
        <div class="note-card-inner">
          <div class="note-cover">
            <img
                :src="note.cover || 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'"
                :alt="note.title"
                class="cover-img"
            />
            <div class="cover-overlay">
              <el-icon class="overlay-icon"><View /></el-icon>
              <span>查看详情</span>
            </div>
          </div>
          <div class="note-info">
            <h3 class="note-title">{{ note.title }}</h3>
            <div class="book-info">
              <span class="book-name">《{{ note.bookName }}》</span>
              <span class="author">— {{ note.author }}</span>
            </div>
            <div class="meta">
              <div class="meta-item">
                <el-icon><View /></el-icon>
                <span>{{ note.viewCount }}</span>
              </div>
              <div class="meta-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ note.commentCount }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Star /></el-icon>
                <span>{{ note.likeCount }}</span>
              </div>
            </div>
            <div class="tags">
              <el-tag v-for="tag in note.tags" :key="tag" size="small" class="note-tag">{{ tag }}</el-tag>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 空状态 -->
      <div v-if="!loading && notes.length === 0" class="empty-state">
        <el-empty description="暂无相关笔记，换个搜索条件试试吧～" image-size="120">
          <el-button type="primary" @click="resetFilter">重置筛选</el-button>
        </el-empty>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePage"
          @current-change="handlePage"
          background
          class="pagination-component"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElEmpty } from 'element-plus'
import { View, ChatLineSquare, Star, Search } from '@element-plus/icons-vue'
import { getNotes, getCategories, getTags } from '@/api/modules/note'

const router = useRouter()
const loading = ref(false)
const notes = ref([])
const categories = ref([])
const tags = ref([])

// 筛选条件
const searchKeyword = ref('')
const selectedCategory = ref('')
const selectedTags = ref([])
const sortType = ref('latest')

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const handleSearch = () => {
  currentPage.value = 1
  fetchNotes()
}

const handlePage = ()=>{
  fetchNotes()
}

const fetchNotes = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      categoryId: selectedCategory.value || undefined,
      tags: selectedTags.value.length ? selectedTags.value.join(',') : undefined,
      sort: sortType.value,
    }
    const res = await getNotes(params)
    notes.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载笔记失败')
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const fetchTags = async () => {
  try {
    const res = await getTags()
    tags.value = res
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

const resetFilter = () => {
  searchKeyword.value = ''
  selectedCategory.value = ''
  selectedTags.value = []
  sortType.value = 'latest'
  handleSearch()
}

const goToDetail = (id) => {
  router.push(`/notes/${id}`)
}

onMounted(() => {
  fetchCategories()
  fetchTags()
  fetchNotes()
})
</script>

<style scoped>
.notes-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* 筛选卡片样式 */
.filter-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.05);
  padding: 24px;
  margin-bottom: 32px;
  animation: fadeInUp 0.5s ease forwards;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
  margin-bottom: 20px;
}

.search-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  max-width: 450px;
  border-radius: 10px;
  transition: all 0.3s ease;
  border: 1px solid #e5e7eb;
}

.search-input-focus {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 4px rgba(79, 172, 254, 0.1);
}

.search-btn {
  border-radius: 10px;
  padding: 12px 24px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.2);
}

.search-icon {
  font-size: 16px;
}

.filter-select-group {
  display: flex;
  gap: 16px;
  align-items: center;
}

.filter-select {
  min-width: 180px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
}

/* 排序行样式 */
.sort-row {
  display: flex;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.sort-label {
  font-size: 14px;
  color: #6b7280;
  margin-right: 16px;
  font-weight: 500;
}

.sort-radio-group {
  display: flex;
}

.sort-radio {
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.sort-radio:deep(.el-radio-button__inner) {
  border-radius: 8px;
  padding: 8px 20px;
}

.sort-radio:deep(.is-active) .el-radio-button__inner {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

/* 笔记网格 */
.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  margin-top: 8px;
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  padding: 60px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 笔记卡片样式 */
.note-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  transition: all 0.4s ease;
  animation: fadeInUp 0.6s ease forwards;
}

.note-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(79, 172, 254, 0.2);
}

.note-card-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 封面区域 */
.note-cover {
  position: relative;
  height: 220px;
  overflow: hidden;
  background-color: #f9fafb;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.note-card:hover .cover-img {
  transform: scale(1.08);
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
      to bottom,
      rgba(79, 172, 254, 0) 0%,
      rgba(79, 172, 254, 0.7) 100%
  );
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 20px;
  opacity: 0;
  transition: opacity 0.4s ease;
}

.note-card:hover .cover-overlay {
  opacity: 1;
}

.cover-overlay span {
  color: white;
  font-size: 16px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  padding: 8px 20px;
  border-radius: 30px;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.overlay-icon {
  margin-right: 8px;
  font-size: 14px;
}

/* 笔记信息区域 */
.note-info {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.note-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-info {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 16px;
}

.book-name {
  font-weight: 500;
  color: #4b5563;
}

.author {
  color: #9ca3af;
}

/* 元数据样式 */
.meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item .el-icon {
  font-size: 12px;
}

/* 标签样式 */
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
}

.note-tag {
  background-color: #f0f7ff;
  border: none;
  color: var(--el-color-primary);
  font-size: 12px;
  padding: 4px 12px;
  height: 28px;
  line-height: 20px;
  border-radius: 14px;
  transition: all 0.3s ease;
}

.note-tag:hover {
  background-color: var(--el-color-primary-light-8);
  transform: scale(1.05);
}

/* 分页样式 */
.pagination {
  margin-top: 48px;
  display: flex;
  justify-content: center;
}

.pagination-component {
  --el-pagination-button-bg-color: #fff;
  --el-pagination-button-hover-bg-color: #f0f7ff;
  --el-pagination-button-active-bg-color: var(--el-color-primary);
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .notes-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .notes-page {
    padding: 0 16px 32px;
  }

  .filter-card {
    padding: 16px;
  }

  .filter-row {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .search-wrapper {
    min-width: 100%;
  }

  .filter-select-group {
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .filter-select {
    width: 100%;
    min-width: unset;
  }

  .sort-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .notes-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .note-cover {
    height: 200px;
  }
}
</style>