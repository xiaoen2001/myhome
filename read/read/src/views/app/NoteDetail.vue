<template>
  <div class="note-detail-page">
    <!-- 悬浮返回按钮 -->
    <div class="glass-back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回</span>
    </div>

    <div class="note-detail" v-loading="loading" loading-text="加载中...">
    <div class="note-header">
      <div class="author-section" @click="goToAuthorProfile">
        <el-avatar
            :size="52"
            :src="getAvatarUrl(note.userAvatar)"
            class="author-avatar"
        />
        <div class="author-info">
          <div class="author-name">{{ note.nickname || note.username }}</div>
          <div class="publish-time">{{ formatTime(note.createdAt) }}</div>
        </div>
      </div>
      <div class="action-buttons">
        <el-button
            :type="isLiked ? 'primary' : 'default'"
            @click="toggleLike"
            class="action-btn"
            icon="Trophy"
        >
          {{ note.likeCount || 0 }}
        </el-button>
        <el-button
            :type="isCollected ? 'primary' : 'default'"
            @click="toggleCollect"
            class="action-btn"
            icon="Star"
        >
          {{ note.collectCount || 0 }}
        </el-button>
        <el-button
            v-if="!isSelf && !isFriend"
            type="primary"
            plain
            @click="addFriend"
            class="add-friend-btn"
        >
          加好友
        </el-button>
      </div>
    </div>
    <h3 style="margin-top: 10px;margin-bottom: 10px">{{ note.bookName }} - {{ note.author }}</h3>
    <div class="cover-container" v-if="note.cover">
      <img :src="note.cover" class="note-cover" loading="lazy" alt="笔记图片" />
    </div>


    <h2 class="note-title">{{ note.title }}</h2>
    <div class="note-content" v-html="note.content"></div>

    <div class="tags" v-if="note.tags && note.tags.length">
      <el-tag v-for="tag in note.tags" :key="tag" size="small" class="note-tag">{{ tag }}</el-tag>
    </div>

    <div class="comments">
      <div class="comments-header">
        <h3>评论</h3>
        <span class="comments-count">({{ comments.total || 0 }})</span>
      </div>

      <div class="comment-form">
        <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            placeholder="写下你的评论..."
            class="comment-input"
            resize="none"
        />
        <el-button
            type="primary"
            @click="submitComment"
            :loading="commentLoading"
            class="submit-comment-btn"
        >
          发表评论
        </el-button>
      </div>

      <div class="comment-list">
        <div v-for="item in comments.list" :key="item.id" class="comment-item">
          <el-avatar
              :size="36"
              :src="getAvatarUrl(item.userAvatar)"
              class="comment-avatar"
          >
            <img :src="defaultAvatar" alt="默认头像"/>
          </el-avatar>
          <div class="comment-content">
            <div class="comment-user">{{ item.userNickname || item.username }}</div>
            <div class="comment-text">{{ item.content }}</div>
            <div class="comment-time">{{ formatTime(item.createdAt) }}</div>
          </div>
        </div>
        <el-empty
            v-if="!comments.list || comments.list.length === 0"
            description="暂无评论，快来抢沙发～"
            class="empty-comments"
        />
      </div>
    </div>
    </div>
  </div>
</template>
<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {useUserStore} from '@/stores/user'
import {collectNote, getNoteDetail, likeNote, uncollectNote, unlikeNote} from '@/api/modules/note'
import {addComment, getComments} from '@/api/modules/comment'
import {addFriendRequest, checkFriendStatus} from '@/api/modules/friend'
import { getAvatarUrl } from '@/utils/avatar'
import { debounce } from 'lodash'
import { onBeforeUnmount } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'

const defaultAvatar = '/default-avatar.png'
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const noteId = computed(() => parseInt(route.params.id))
const currentUserId = computed(() => userStore.userId)

const loading = ref(false)
const note = ref({})
const isLiked = ref(false)
const isCollected = ref(false)
const isFriend = ref(false)
const isSelf = computed(() => note.value.userId === currentUserId.value)
const comments = ref({list: [], total: 0})
const newComment = ref('')
const commentLoading = ref(false)

// 时间格式化优化
const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60 * 1000) return '刚刚'
  // 小于1小时
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`
  // 小于1天
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  // 小于1年
  if (diff < 365 * 24 * 60 * 60 * 1000) {
    return date.toLocaleDateString('zh-CN', {month: 'short', day: 'numeric'})
  }
  // 超过1年
  return date.toLocaleDateString('zh-CN', {year: 'numeric', month: 'short', day: 'numeric'})
}

const goBack = () => {
  router.back()
}

const goToAuthorProfile = () => {
  if (note.value.userId) {
    router.push(`/profile/${note.value.userId}`)
  }
}

const fetchNoteDetail = async () => {
  loading.value = true
  try {
    const res = await getNoteDetail(noteId.value)
    note.value = res
    if (note.value.visibility === 'private' && note.value.userId !== currentUserId.value) {
      ElMessage.error('该笔记为私密，您无权查看')
      router.back()
      return
    }
    isLiked.value = res.liked || false
    isCollected.value = res.collected || false
    if (note.value.userId && note.value.userId !== currentUserId.value) {
      isFriend.value = await checkFriendStatus(note.value.userId)
    }
  } catch (error) {
    ElMessage.error('加载笔记失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getComments({noteId: noteId.value})
    comments.value = {list: res, total: res.length}
  } catch (error) {
    console.error('加载评论失败', error)
  }
}

const toggleLike = debounce(async () => {
  const previous = isLiked.value;
  isLiked.value = !previous;
  note.value.likeCount += isLiked.value ? 1 : -1;
  try {
    if (!previous) {
      await likeNote(noteId.value);
    } else {
      await unlikeNote(noteId.value);
    }
  } catch {
    isLiked.value = previous;
    note.value.likeCount += previous ? 1 : -1;
    ElMessage.error('操作失败');
  }
}, 500)

const toggleCollect = debounce(async () => {
  const previous = isCollected.value;
  isCollected.value = !previous;
  note.value.collectCount += isCollected.value ? 1 : -1;
  try {
    if (!previous) {
      await collectNote(noteId.value);
    } else {
      await uncollectNote(noteId.value);
    }
  } catch {
    isCollected.value = previous;
    note.value.collectCount += previous ? 1 : -1;
    ElMessage.error('操作失败');
  }
}, 500)

const addFriend = async () => {
  try {
    await addFriendRequest(note.value.userId)
    ElMessage.success('好友申请已发送')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '申请失败')
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commentLoading.value = true
  try {
    await addComment({noteId: noteId.value, content: newComment.value})
    ElMessage.success('评论成功')
    newComment.value = ''
    fetchComments()
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commentLoading.value = false
  }
}

onMounted(() => {
  fetchNoteDetail()
  fetchComments()
})
onBeforeUnmount(() => {
  toggleLike.cancel()
  toggleCollect.cancel()
})
</script>

<style scoped>
.note-detail-page {
  position: relative;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* 悬浮返回按钮 */
.glass-back-btn {
  position: fixed;
  top: 100px;
  left: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  color: var(--el-color-primary);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 100;
}

.glass-back-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-4px);
  box-shadow: 0 15px 30px rgba(79, 172, 254, 0.2);
}

/* 基础布局 */
.note-detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  animation: fadeInUp 0.6s ease forwards;
}

/* 头部区域 */
.note-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f5f7fa;
}

.author-section {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.author-section:hover {
  opacity: 0.9;
}

.author-avatar {
  border: 2px solid #f5f7fa;
}

.author-info {
  line-height: 1.4;
}

.author-name {
  font-weight: 600;
  font-size: 18px;
  color: #1d2129;
}

.publish-time {
  font-size: 14px;
  color: #86909c;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.add-friend-btn {
  border-radius: 8px;
  padding: 8px 16px;
}

/* 封面图 */
.cover-container {
  text-align: center;
  margin-bottom: 32px;
  border-radius: 12px;
  overflow: hidden;
}

.note-cover {
  width: 100%;
  max-height: 450px;
  object-fit: cover;
  border-radius: 12px;
  transition: transform 0.3s ease;
}

.note-cover:hover {
  transform: scale(1.01);
}

/* 标题 */
.note-title {
  font-size: 28px;
  font-weight: 700;
  color: #1d2129;
  margin: 0 0 24px;
  line-height: 1.3;
  letter-spacing: 0.5px;
  text-align: center;
}

/* 内容区域 */
.note-content {
  font-size: 16px;
  line-height: 1.8;
  color: #4e5969;
  margin-bottom: 28px;
  padding: 0 4px;
}

/* 标签区域 */
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 36px;
}

/* 评论区域 */
.comments {
  margin-top: 40px;
  border-top: 1px solid rgba(255, 255, 255, 0.4);
  padding-top: 32px;
}

.comments-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.comments-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin: 0;
}

.comments-count {
  font-size: 14px;
  color: #86909c;
}

.comment-form {
  margin: 0 0 32px;
}

.comment-input {
  margin-bottom: 12px;
}

:deep(.comment-input .el-textarea__inner) {
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;
  padding: 12px;
}

:deep(.comment-input .el-textarea__inner:focus) {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.2);
  background: rgba(255, 255, 255, 0.9);
}

.submit-comment-btn {
  width: 100%;
  padding: 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  font-weight: bold;
  transition: all 0.3s ease;
  font-size: 16px;
}

.submit-comment-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(79, 172, 254, 0.3);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 16px;
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
}

.comment-item:hover {
  background: rgba(255, 255, 255, 0.85);
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(79, 172, 254, 0.15);
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  line-height: 1.5;
}

.comment-user {
  font-weight: 600;
  font-size: 15px;
  color: #1d2129;
  margin-bottom: 6px;
  transition: color 0.3s ease;
}

.comment-item:hover .comment-user {
  color: var(--el-color-primary);
}

.comment-text {
  font-size: 15px;
  color: #4e5969;
  margin-bottom: 8px;
  line-height: 1.6;
}

.comment-time {
  font-size: 12px;
  color: #86909c;
}

.empty-comments {
  margin: 40px 0;
}

/* 响应式适配 */
@media (max-width: 1300px) {
  .glass-back-btn {
    position: static;
    display: inline-flex;
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .note-detail {
    padding: 16px;
    border-radius: 12px;
  }

  .note-title {
    font-size: 24px;
  }

  .note-cover {
    max-height: 300px;
  }

  .note-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .action-buttons {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>