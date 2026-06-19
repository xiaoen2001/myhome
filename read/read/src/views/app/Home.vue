<template>
  <div class="home-container">
    <!-- 轮播图 Hero -->
    <section class="carousel-section">
      <el-carousel :interval="5000" height="380px" class="carousel">
        <el-carousel-item v-for="item in carousels" :key="item.id">
          <div class="carousel-slide" :style="{ backgroundImage: `url(${item.imageUrl})` }">
            <div class="carousel-caption">
              <p class="carousel-eyebrow">精选推荐</p>
              <h2>{{ item.title }}</h2>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 热门推荐 — 横向滚动卡片 -->
    <section class="section-block">
      <div class="section-header">
        <h3>热门推荐</h3>
        <p class="section-sub">社区点赞最多的笔记</p>
      </div>
      <div class="hot-scroll">
        <div
          v-for="note in hotNotes"
          :key="note.id"
          class="hot-card"
          @click="goToDetail(note.id)"
        >
          <div class="hot-card-img">
            <img
              :src="note.cover || defaultCover"
              :alt="note.title"
              loading="lazy"
            />
          </div>
          <div class="hot-card-body">
            <h4>{{ note.title }}</h4>
            <p class="hot-card-book">{{ note.bookName }}</p>
            <div class="hot-card-meta">
              <span><el-icon><ThumbUp /></el-icon> {{ note.likeCount }}</span>
              <span><el-icon><ChatLineSquare /></el-icon> {{ note.commentCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热议 + 最新 — 非对称双栏 -->
    <section class="section-block">
      <div class="split-grid">
        <!-- 左栏：热议（2/3 宽，大卡片） -->
        <div class="split-left">
          <div class="section-header">
            <h3>热议讨论</h3>
            <p class="section-sub">评论最活跃的笔记</p>
          </div>
          <div class="discuss-list">
            <div
              v-for="(note, idx) in mostCommentedNotes"
              :key="note.id"
              class="discuss-item"
              :class="{ 'discuss-featured': idx === 0 }"
              @click="goToDetail(note.id)"
            >
              <img
                :src="note.cover || defaultCover"
                :alt="note.title"
                class="discuss-cover"
                loading="lazy"
              />
              <div class="discuss-info">
                <h4>{{ note.title }}</h4>
                <p>{{ note.bookName }}</p>
                <div class="discuss-meta">
                  <span class="meta-badge">
                    <el-icon><ChatLineSquare /></el-icon> {{ note.commentCount }} 条讨论
                  </span>
                  <span class="meta-badge">
                    <el-icon><ThumbUp /></el-icon> {{ note.likeCount }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右栏：最新（1/3 宽，紧凑列表） -->
        <div class="split-right">
          <div class="section-header">
            <h3>最新发布</h3>
            <p class="section-sub">刚刚出炉</p>
          </div>
          <div class="latest-list">
            <div
              v-for="note in latestNotes"
              :key="note.id"
              class="latest-item"
              @click="goToDetail(note.id)"
            >
              <span class="latest-dot"></span>
              <div class="latest-text">
                <h5>{{ note.title }}</h5>
                <p>{{ note.bookName }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
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
const defaultCover = 'https://picsum.photos/seed/note-cover/400/300'

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
/* === Taste-Skill Home: asymmetric layout, no 3-equal-columns === */

.home-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px 48px;
}

/* ---- Carousel ---- */
.carousel-section {
  margin-bottom: 48px;
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
  background: linear-gradient(to top, rgba(15, 23, 42, 0.55) 0%, transparent 60%);
  pointer-events: none;
}
.carousel-caption {
  position: absolute;
  bottom: 40px;
  left: 48px;
  z-index: 2;
  color: #ffffff;
}
.carousel-eyebrow {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 8px;
  opacity: 0.85;
}
.carousel-caption h2 {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.022em;
  color: #ffffff;
}

/* ---- Section Shared ---- */
.section-block {
  margin-bottom: 56px;
}
.section-header {
  margin-bottom: 24px;
}
.section-header h3 {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.022em;
}
.section-sub {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

/* ---- Hot: horizontal scroll cards ---- */
.hot-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  padding-bottom: 8px;
  -webkit-overflow-scrolling: touch;
}
.hot-scroll::-webkit-scrollbar {
  height: 4px;
}
.hot-card {
  min-width: 240px;
  max-width: 260px;
  flex-shrink: 0;
  scroll-snap-align: start;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}
.hot-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}
.hot-card:active {
  transform: scale(0.985);
}
.hot-card-img {
  aspect-ratio: 16 / 10;
  overflow: hidden;
}
.hot-card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hot-card-body {
  padding: 14px 16px;
}
.hot-card-body h4 {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.hot-card-book {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}
.hot-card-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: #94a3b8;
}
.hot-card-meta .el-icon {
  vertical-align: -2px;
  margin-right: 2px;
}

/* ---- Split Grid: 2/3 + 1/3 ---- */
.split-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
  align-items: start;
}

/* ---- Discuss (left) ---- */
.discuss-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.discuss-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}
.discuss-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.07);
}
.discuss-item:active {
  transform: scale(0.99);
}
.discuss-featured {
  border-left: 4px solid #3730a3;
  background: #fafaff;
}
.discuss-cover {
  width: 100px;
  height: 80px;
  object-fit: cover;
  border-radius: 10px;
  flex-shrink: 0;
}
.discuss-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 4px;
}
.discuss-info p {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}
.discuss-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
}
.meta-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 10px;
  border-radius: 999px;
  font-weight: 500;
}
.meta-badge .el-icon {
  font-size: 13px;
}

/* ---- Latest (right) — compact dot-list ---- */
.latest-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}
.latest-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  cursor: pointer;
  transition: background 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.latest-item + .latest-item {
  border-top: 1px solid #f1f5f9;
}
.latest-item:hover {
  background: #f8fafc;
}
.latest-item:active {
  background: #f1f5f9;
}
.latest-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
  flex-shrink: 0;
  transition: background 0.2s;
}
.latest-item:hover .latest-dot {
  background: #3730a3;
}
.latest-text h5 {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 2px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.latest-text p {
  font-size: 12px;
  color: #94a3b8;
}

/* ---- Responsive ---- */
@media (max-width: 768px) {
  .split-grid {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .carousel {
    height: 240px !important;
  }
  .carousel-caption h2 {
    font-size: 22px;
  }
  .carousel-caption {
    left: 24px;
    bottom: 24px;
  }
  .hot-card {
    min-width: 180px;
    max-width: 200px;
  }
}
</style>
