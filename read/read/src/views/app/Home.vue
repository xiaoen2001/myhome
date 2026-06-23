<template>
  <div class="home-container">
    <!-- 轮播图 -->
    <section class="carousel-section">
      <el-carousel :interval="5000" height="380px" class="carousel">
        <el-carousel-item v-for="item in carousels" :key="item.id">
          <div class="carousel-slide" :style="{ backgroundImage: `url(${item.imageUrl})` }">
            <div class="carousel-caption">
              <h2>{{ item.title }}</h2>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 推荐笔记 — 三列等分布局 -->
    <section class="recommend-section">
      <el-row :gutter="24">
        <!-- 热门推荐 -->
        <el-col :xs="24" :sm="8">
          <div class="recommend-card">
            <div class="card-header">
              <span class="title">热门推荐</span>
              <span class="subtitle">点赞最多</span>
            </div>
            <div class="note-list">
              <div v-for="note in hotNotes" :key="note.id" class="note-item" @click="goToDetail(note.id)">
                <img :src="note.cover || defaultCover" class="note-cover" :alt="note.title" loading="lazy" />
                <div class="note-info">
                  <h4>{{ note.title }}</h4>
                  <p>{{ note.bookName }}</p>
                  <div class="meta">
                    <span><el-icon><ThumbUp /></el-icon> {{ note.likeCount }}</span>
                    <span><el-icon><ChatLineSquare /></el-icon> {{ note.commentCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 热议推荐 -->
        <el-col :xs="24" :sm="8">
          <div class="recommend-card">
            <div class="card-header">
              <span class="title">热议讨论</span>
              <span class="subtitle">评论最多</span>
            </div>
            <div class="note-list">
              <div v-for="note in mostCommentedNotes" :key="note.id" class="note-item" @click="goToDetail(note.id)">
                <img :src="note.cover || defaultCover" class="note-cover" :alt="note.title" loading="lazy" />
                <div class="note-info">
                  <h4>{{ note.title }}</h4>
                  <p>{{ note.bookName }}</p>
                  <div class="meta">
                    <span><el-icon><ThumbUp /></el-icon> {{ note.likeCount }}</span>
                    <span><el-icon><ChatLineSquare /></el-icon> {{ note.commentCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 最新发布 -->
        <el-col :xs="24" :sm="8">
          <div class="recommend-card">
            <div class="card-header">
              <span class="title">最新发布</span>
              <span class="subtitle">刚刚出炉</span>
            </div>
            <div class="note-list">
              <div v-for="note in latestNotes" :key="note.id" class="note-item" @click="goToDetail(note.id)">
                <img :src="note.cover || defaultCover" class="note-cover" :alt="note.title" loading="lazy" />
                <div class="note-info">
                  <h4>{{ note.title }}</h4>
                  <p>{{ note.bookName }}</p>
                  <div class="meta">
                    <span><el-icon><ThumbUp /></el-icon> {{ note.likeCount }}</span>
                    <span><el-icon><ChatLineSquare /></el-icon> {{ note.commentCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatLineSquare } from '@element-plus/icons-vue'
import { getCarousels, getRecommendNotes } from '@/api/modules/note'

const router = useRouter()
const carousels = ref([])
const hotNotes = ref([])
const mostCommentedNotes = ref([])
const latestNotes = ref([])
const defaultCover = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0MDAiIGhlaWdodD0iMzAwIj48cmVjdCBmaWxsPSIjZjFmNWY5IiB3aWR0aD0iNDAwIiBoZWlnaHQ9IjMwMCIvPjx0ZXh0IHg9IjIwMCIgeT0iMTU1IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTRhM2I4IiBmb250LXNpemU9IjQ4IiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiI+8J+TmTwvdGV4dD48L3N2Zz4='

const fetchCarousels = async () => {
  try {
    carousels.value = await getCarousels()
  } catch (error) {
    console.error('获取轮播图失败', error)
  }
}

const fetchRecommendNotes = async () => {
  try {
    const [hot, commented, latest] = await Promise.all([
      getRecommendNotes('hot'),
      getRecommendNotes('mostCommented'),
      getRecommendNotes('latest'),
    ])
    hotNotes.value = hot
    mostCommentedNotes.value = commented
    latestNotes.value = latest
  } catch (error) {
    ElMessage.error('加载推荐笔记失败')
  }
}

const goToDetail = (id) => {
  router.push(`/notes/${id}`)
}

onMounted(() => {
  fetchCarousels()
  fetchRecommendNotes()
})
</script>

<style scoped>
.home-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

/* ---- Carousel ---- */
.carousel-section {
  margin-bottom: 40px;
}
.carousel {
  border-radius: 16px;
  overflow: hidden;
}
.carousel-slide {
  height: 100%;
  background-size: cover;
  background-position: center;
  position: relative;
}
.carousel-slide::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(15, 23, 42, 0.5) 0%, transparent 60%);
  pointer-events: none;
}
.carousel-caption {
  position: absolute;
  bottom: 40px;
  left: 48px;
  z-index: 2;
}
.carousel-caption h2 {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: -0.022em;
  color: #ffffff;
  text-shadow: 0 1px 4px rgba(0,0,0,0.4);
}

/* ---- Recommend Section ---- */
.recommend-section {
  margin-top: 8px;
}

.recommend-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  overflow: hidden;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  margin-bottom: 20px;
}
.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.card-header .title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.022em;
}
.card-header .subtitle {
  font-size: 12px;
  color: #94a3b8;
}

.note-list {
  padding: 8px 12px;
}
.note-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 10px;
  transition: background 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.note-item:hover {
  background: #f8fafc;
}
.note-item:active {
  transform: scale(0.985);
}

.note-cover {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}
.note-info {
  flex: 1;
  min-width: 0;
}
.note-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 3px;
  letter-spacing: -0.011em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.note-info p {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}
.meta {
  display: flex;
  gap: 14px;
  font-size: 11px;
  color: #94a3b8;
}
.meta .el-icon {
  vertical-align: -2px;
  margin-right: 2px;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .carousel {
    height: 240px !important;
  }
  .carousel-caption {
    left: 24px;
    bottom: 24px;
  }
  .carousel-caption h2 {
    font-size: 22px;
  }
}
</style>
