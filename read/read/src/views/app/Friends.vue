<template>
  <div class="friends-page">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 消息标签页 -->
      <el-tab-pane label="消息" name="messages">
        <div class="message-list">
          <div v-for="msg in recentMessages" :key="msg.id" class="message-item" @click="startChatFromMsg(msg)">
            <el-avatar :size="48" :src="getAvatarUrl(msg.avatar)"/>
            <div class="message-info">
              <div class="name">{{ msg.nickname }}</div>
              <div class="last-message">{{ msg.content }}</div>
            </div>
            <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
          <el-empty v-if="recentMessages.length === 0" description="暂无消息"/>
        </div>
      </el-tab-pane>

      <el-tab-pane label="好友列表" name="friends">
        <div class="friend-list">
          <div v-for="friend in friends" :key="friend.id" class="friend-item">
            <el-avatar :size="48" :src="getAvatarUrl(friend.friendAvatar)"/>
            <div class="friend-info">
              <div class="name">{{ friend.friendNickname || '未知用户' }}</div>
              <div class="since">成为好友于 {{ formatDate(friend.createdAt) }}</div>
            </div>
            <div class="friend-actions">
              <el-button link type="primary" @click="startChat(friend)">发消息</el-button>
              <el-button link type="danger" @click="removeFriend(friend)">删除好友</el-button>
            </div>
          </div>
          <el-empty v-if="friends.length === 0" description="暂无好友，去添加吧"/>
        </div>
      </el-tab-pane>

      <el-tab-pane label="好友申请" name="requests">
        <div class="request-list">
          <div v-for="req in requests" :key="req.id" class="request-item">
            <el-avatar :size="48" :src="getAvatarUrl(req.fromUserAvatar)"/>
            <div class="request-info">
              <div class="name">{{ req.fromUserNickname }}</div>
              <div class="time">申请时间：{{ formatDate(req.createdAt) }}</div>
            </div>
            <div class="request-actions">
              <el-button type="success" size="small" @click="handleRequest(req.id, 'agree')">同意</el-button>
              <el-button type="danger" size="small" @click="handleRequest(req.id, 'reject')">拒绝</el-button>
            </div>
          </div>
          <el-empty v-if="requests.length === 0" description="暂无好友申请"/>
        </div>
      </el-tab-pane>

      <el-tab-pane label="添加好友" name="add">
        <div class="search-bar">
          <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名或昵称"
              clearable
              @keyup.enter="searchUsers"
          />
          <el-button type="primary" @click="searchUsers">搜索</el-button>
        </div>
        <div class="search-results">
          <div v-for="user in searchResults" :key="user.id" class="search-item">
            <el-avatar :size="40" :src="getAvatarUrl(user.avatar)"/>
            <div class="user-info">
              <div class="nickname">{{ user.nickname }}</div>
              <div class="username">@{{ user.username }}</div>
            </div>
            <el-button type="primary" size="small" @click="sendRequest(user.id)">添加好友</el-button>
          </div>
          <el-empty v-if="searchResults.length === 0 && searched" description="未找到用户"/>
        </div>

        <div class="recommend-section" v-if="recommendUsers.length">
          <div class="section-title">推荐好友</div>
          <div class="recommend-list">
            <div v-for="user in recommendUsers" :key="user.id" class="recommend-item">
              <el-avatar :size="40" :src="getAvatarUrl(user.avatar)">
                <img :src="defaultAvatar" alt="默认头像"/>
              </el-avatar>
              <div class="user-info">
                <div class="nickname">{{ user.nickname }}</div>
                <div class="username">@{{ user.username }}</div>
                <div class="mutual" v-if="user.mutualFriendsCount">共同好友 {{ user.mutualFriendsCount }}人</div>
              </div>
              <el-button type="primary" size="small" @click="sendRequest(user.id)">添加好友</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { getAvatarUrl } from '@/utils/avatar'
import {ref, onMounted, computed} from 'vue'
import {ElMessage} from 'element-plus'
import { ElMessageBox } from 'element-plus'
import {useRouter} from 'vue-router'
import {useUserStore} from "@/stores/user.js";
import {
  getFriends,
  getFriendRequests,
  sendFriendRequest,
  deleteFriend,
  handleFriendRequest,
  searchUsers as apiSearchUsers,
  getRecommendUsers,
  getRecentMessages
} from '@/api/modules/friend'
const defaultAvatar = '/default-avatar.png'
const userId = computed(() => useUserStore().userId) // 直接取，假设 store 中已是数字
const router = useRouter()
const activeTab = ref('friends')
const friends = ref([])
const requests = ref([])
const searchKeyword = ref('')
const searchResults = ref([])
const searched = ref(false)
const recommendUsers = ref([])
const recentMessages = ref([])
//删除好友
const removeFriend = (friend) => {
  ElMessageBox.confirm(
      `确定要删除好友「${friend.friendNickname || '未知用户'}」吗？删除后双方将不再是好友。`,
      '提示',
      { type: 'warning' }
  ).then(async () => {
    try {
      await deleteFriend(friend.friendId) // 注意这里是 friendId，而不是 id
      ElMessage.success('好友已删除')
      fetchFriends()
      fetchRecentMessages() // 可选，刷新消息列表
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}
const formatDate = (isoString) => {
  if (!isoString) return ''
  const date = new Date(isoString)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}
const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}
const fetchFriends = async () => {
  try {
    const res = await getFriends()
    friends.value = res || []
  } catch (error) {
    ElMessage.error('加载好友列表失败')
  }
}

const fetchRecentMessages = async () => {
  try {
    const res = await getRecentMessages()
    recentMessages.value = res
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

const startChat = (friend) => {
  router.push(`/chat/${friend.friendId}`)
}

const fetchRequests = async () => {
  try {
    const res = await getFriendRequests()
    requests.value = res || []
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  }
}

const fetchRecommendUsers = async () => {
  try {
    const res = await getRecommendUsers()
    recommendUsers.value = res || []
  } catch (error) {
    console.error('加载推荐好友失败', error)
  }
}
const handleRequest = async (requestId, action) => {
  try {
    await handleFriendRequest(requestId, action)
    ElMessage.success(action === 'agree' ? '已添加好友' : '已拒绝申请')
    fetchRequests()
    if (action === 'agree') {
      fetchFriends()
      fetchRecommendUsers() // 刷新推荐列表
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const searchUsers = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  try {
    const res = await apiSearchUsers(searchKeyword.value)
    searchResults.value = res || []
    searched.value = true
  } catch (error) {
    ElMessage.error('搜索失败')
  }
}

const sendRequest = async (userId) => {
  try {
    await sendFriendRequest(userId)
    ElMessage.success('好友申请已发送')
    // 刷新推荐列表（移除已发送申请的用户）
    fetchRecommendUsers()
    // 清空搜索结果
    searchKeyword.value = ''
    searchResults.value = []
    searched.value = false
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  }
}

// 处理标签页切换
const handleTabClick = () => {
  if (activeTab.value === 'friends') fetchFriends()
  else if (activeTab.value === 'requests') fetchRequests()
  else if (activeTab.value === 'messages') fetchRecentMessages()
}

// 从消息列表跳转到聊天
const startChatFromMsg = (msg) => {
  console.log('msg对象:', msg)
  const currentUserId = userId.value
  if (!currentUserId) {
    console.error('当前用户ID未获取到')
    return
  }
  let friendId = null

  if (msg.friendId) {
    friendId = msg.friendId
  } else if (msg.senderId && msg.receiverId) {
    // 根据发送者和接收者找出好友ID
    friendId = msg.senderId === currentUserId ? msg.receiverId : msg.senderId
  } else {
    console.error('无法确定好友ID', msg)
    return
  }
  router.push(`/chat/${friendId}`)
}

onMounted(() => {
  fetchFriends()
  fetchRequests()
  fetchRecommendUsers()
  fetchRecentMessages()
})
</script>

<style scoped>
.friends-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.friend-item, .request-item, .search-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.friend-info, .request-info, .user-info {
  flex: 1;
}

.name {
  font-weight: 600;
  margin-bottom: 4px;
}

.since, .time {
  font-size: 12px;
  color: #8c9aa6;
}

.request-actions {
  display: flex;
  gap: 8px;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.search-results {
  margin-top: 20px;
}

.recommend-section {
  margin-top: 30px;
  border-top: 1px solid #e5e7eb;
  padding-top: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1f2d3d;
}

.recommend-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.mutual {
  font-size: 12px;
  color: #8c9aa6;
  margin-top: 2px;
}

/* 新增消息列表样式 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: background 0.2s;
}

.message-item:hover {
  background: #f5f7fa;
}

.message-info {
  flex: 1;
}

.message-info .name {
  font-weight: 600;
  margin-bottom: 4px;
}

.message-info .last-message {
  font-size: 14px;
  color: #6b7a86;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  font-size: 12px;
  color: #8c9aa6;
  white-space: nowrap;
}
</style>