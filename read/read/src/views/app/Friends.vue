<template>
  <div class="qq-chat-container">
    <!-- 左侧边栏：好友列表 -->
    <div class="chat-sidebar">
      <!-- 标签切换 -->
      <div class="sidebar-tabs">
        <div class="tab-item" :class="{active: sidebarTab==='messages'}" @click="switchTab('messages')">消息</div>
        <div class="tab-item" :class="{active: sidebarTab==='friends'}" @click="switchTab('friends')">好友</div>
        <div class="tab-item" :class="{active: sidebarTab==='requests'}" @click="switchTab('requests')">申请</div>
        <div class="tab-item" :class="{active: sidebarTab==='add'}" @click="switchTab('add')">添加</div>
      </div>

      <!-- 搜索框 -->
      <div class="sidebar-search" v-if="sidebarTab==='add'">
        <el-input v-model="searchKeyword" placeholder="搜索用户" size="small" clearable @keyup.enter="searchUsers" />
        <el-button size="small" type="primary" @click="searchUsers">搜索</el-button>
      </div>

      <!-- 消息列表 -->
      <div class="sidebar-list" v-if="sidebarTab==='messages'">
        <div v-for="msg in recentMessages" :key="msg.id" class="contact-item"
             :class="{active: activeChatId === getFriendId(msg)}"
             @click="openChat(getFriendId(msg), msg.nickname, msg.avatar)">
          <el-avatar :size="40" :src="getAvatarUrl(msg.avatar)"/>
          <div class="contact-info">
            <div class="contact-name">{{ msg.nickname }}</div>
            <div class="contact-preview">{{ msg.content }}</div>
          </div>
        </div>
        <el-empty v-if="!recentMessages.length" description="暂无消息" />
      </div>

      <!-- 好友列表 -->
      <div class="sidebar-list" v-if="sidebarTab==='friends'">
        <div v-for="f in friends" :key="f.id" class="contact-item"
             :class="{active: activeChatId === f.friendId}" @click="openChat(f.friendId, f.friendNickname, f.friendAvatar)">
          <el-avatar :size="40" :src="getAvatarUrl(f.friendAvatar)"/>
          <div class="contact-info">
            <div class="contact-name">{{ f.friendNickname }}</div>
          </div>
        </div>
        <el-empty v-if="!friends.length" description="暂无好友" />
      </div>

      <!-- 好友申请 -->
      <div class="sidebar-list" v-if="sidebarTab==='requests'">
        <div v-for="req in requests" :key="req.id" class="request-row">
          <el-avatar :size="36" :src="getAvatarUrl(req.fromUserAvatar)"/>
          <div class="request-info">
            <span class="contact-name">{{ req.fromUserNickname }}</span>
          </div>
          <div class="request-actions">
            <el-button size="small" type="success" @click="handleRequest(req.id,'agree')">同意</el-button>
            <el-button size="small" type="danger" @click="handleRequest(req.id,'reject')">拒绝</el-button>
          </div>
        </div>
        <el-empty v-if="!requests.length" description="暂无申请" />
      </div>

      <!-- 添加好友 -->
      <div class="sidebar-list" v-if="sidebarTab==='add'">
        <div v-for="user in searchResults" :key="user.id" class="contact-item" @click="sendRequest(user.id)">
          <el-avatar :size="40" :src="getAvatarUrl(user.avatar)"/>
          <div class="contact-info">
            <div class="contact-name">{{ user.nickname }}</div>
            <div class="contact-preview">@{{ user.username }}</div>
          </div>
          <el-button size="small" type="primary">添加</el-button>
        </div>
        <div class="recommend-section" v-if="!searchResults.length && sidebarTab==='add'">
          <div class="section-label">推荐好友</div>
          <div v-for="user in recommendUsers" :key="user.id" class="contact-item" @click="sendRequest(user.id)">
            <el-avatar :size="40" :src="getAvatarUrl(user.avatar)"/>
            <div class="contact-info">
              <div class="contact-name">{{ user.nickname }}</div>
              <div class="contact-preview">@{{ user.username }}</div>
            </div>
            <el-button size="small" type="primary">添加</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：聊天面板 -->
    <div class="chat-main" v-if="activeChatId">
      <div class="chat-top">
        <span class="chat-friend-name">{{ activeChatName }}</span>
      </div>
      <div class="chat-messages" ref="msgListRef">
        <div v-for="msg in chatMessages" :key="msg.id" class="msg-row" :class="msg.senderId===myUserId ? 'msg-me' : 'msg-other'">
          <el-avatar v-if="msg.senderId!==myUserId" :size="32" :src="getAvatarUrl(activeChatAvatar)" class="msg-avatar"/>
          <div class="msg-bubble" :class="msg.senderId===myUserId?'bubble-me':''">
            <div class="msg-text">{{ msg.content }}</div>
            <div class="msg-time">{{ formatMsgTime(msg.createdAt) }}</div>
          </div>
        </div>
      </div>
      <div class="chat-input-area">
        <el-input v-model="inputText" type="textarea" :rows="2" placeholder="输入消息... Enter 发送"
                  @keydown.enter.exact.prevent="sendMsg" class="chat-input"/>
        <el-button type="primary" @click="sendMsg" size="small">发送</el-button>
      </div>
    </div>

    <!-- 未选择好友时的占位 -->
    <div class="chat-placeholder" v-else>
      <el-empty description="选择一个好友开始聊天" />
    </div>
  </div>
</template>

<script setup>
import { getAvatarUrl } from '@/utils/avatar'
import { ref, onMounted, nextTick, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getFriends, getFriendRequests, sendFriendRequest, deleteFriend,
  handleFriendRequest, searchUsers as apiSearchUsers, getRecommendUsers, getRecentMessages
} from '@/api/modules/friend'
import { getMessages } from '@/api/modules/chat'

const userStore = useUserStore()
const myUserId = computed(() => userStore.userId)
const myAvatar = computed(() => userStore.avatar || '')

// Sidebar state
const sidebarTab = ref('friends')
const friends = ref([])
const requests = ref([])
const recentMessages = ref([])
const searchKeyword = ref('')
const searchResults = ref([])
const recommendUsers = ref([])

// Chat state
const activeChatId = ref(null)
const activeChatName = ref('')
const activeChatAvatar = ref('')
const chatMessages = ref([])
const inputText = ref('')
const msgListRef = ref(null)
let chatSocket = null

// ---- Sidebar logic ----
const switchTab = (tab) => {
  sidebarTab.value = tab
  if (tab === 'friends') fetchFriends()
  else if (tab === 'requests') fetchRequests()
  else if (tab === 'messages') fetchRecentMsgs()
  else if (tab === 'add') fetchRecommend()
}

const fetchFriends = async () => {
  try { friends.value = await getFriends() || [] } catch { }
}
const fetchRequests = async () => {
  try { requests.value = await getFriendRequests() || [] } catch { }
}
const fetchRecentMsgs = async () => {
  try { recentMessages.value = await getRecentMessages() || [] } catch { }
}
const fetchRecommend = async () => {
  try { recommendUsers.value = await getRecommendUsers() || [] } catch { }
}

const searchUsers = async () => {
  if (!searchKeyword.value.trim()) return
  try { searchResults.value = await apiSearchUsers(searchKeyword.value) || [] } catch { }
}
const getFriendId = (msg) => {
  if (!msg) return null
  if (msg.friendId) return msg.friendId
  return msg.senderId === myUserId.value ? msg.receiverId : msg.senderId
}

const sendRequest = async (uid) => {
  try { await sendFriendRequest(uid); ElMessage.success('已发送'); fetchRecommend() } catch { ElMessage.error('发送失败') }
}
const handleRequest = async (reqId, action) => {
  try {
    await handleFriendRequest(reqId, action)
    ElMessage.success(action === 'agree' ? '已添加' : '已拒绝')
    fetchRequests(); if (action === 'agree') { fetchFriends(); fetchRecommend() }
  } catch { ElMessage.error('操作失败') }
}

// ---- Chat logic ----
const connectWS = () => {
  const token = userStore.token
  if (!token) return
  chatSocket = new WebSocket(`ws://localhost:8081/ws/chat?token=${token}`)
  chatSocket.onmessage = (e) => {
    const msg = JSON.parse(e.data)
    if (msg.senderId === activeChatId.value || msg.receiverId === activeChatId.value) {
      chatMessages.value.push(msg)
      nextTick(() => scrollBottom())
    }
  }
  chatSocket.onerror = () => console.error('WS error')
}

const openChat = async (friendId, name, avatar) => {
  activeChatId.value = friendId
  activeChatName.value = name || '好友'
  activeChatAvatar.value = avatar || ''
  try {
    chatMessages.value = await getMessages({ friendId }) || []
    nextTick(() => scrollBottom())
  } catch { ElMessage.error('加载消息失败') }
}

const sendMsg = () => {
  if (!inputText.value.trim() || !chatSocket || chatSocket.readyState !== WebSocket.OPEN) return
  chatSocket.send(JSON.stringify({ receiverId: activeChatId.value, type: 'text', content: inputText.value }))
  inputText.value = ''
}

const scrollBottom = () => {
  if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight
}

const formatMsgTime = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

onMounted(() => {
  fetchFriends(); fetchRequests(); fetchRecentMsgs(); fetchRecommend(); connectWS()
})
onBeforeUnmount(() => {
  if (chatSocket) chatSocket.close()
})
</script>

<style scoped>
/* ---- 整体布局 ---- */
.qq-chat-container {
  display: flex;
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f8f9fb;
  overflow: hidden;
  z-index: 1;
}

/* ---- 左侧栏 ---- */
.chat-sidebar {
  width: 280px;
  min-width: 280px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.sidebar-tabs {
  display: flex;
  border-bottom: 1px solid #e2e8f0;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}
.tab-item:hover { color: #3730a3; }
.tab-item.active { color: #3730a3; border-bottom-color: #3730a3; font-weight: 600; }

.sidebar-search {
  display: flex;
  gap: 6px;
  padding: 10px 12px;
  border-bottom: 1px solid #f1f5f9;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
}

/* ---- 联系人条目 ---- */
.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s;
}
.contact-item:hover { background: #f1f5f9; }
.contact-item.active { background: #eef2ff; }

.contact-info { flex: 1; min-width: 0; }
.contact-name { font-size: 14px; font-weight: 500; color: #0f172a; }
.contact-preview { font-size: 12px; color: #94a3b8; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.request-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid #f1f5f9;
}
.request-info { flex: 1; }
.request-actions { display: flex; gap: 4px; flex-shrink: 0; }

.recommend-section { padding: 0; }
.section-label { padding: 10px 14px 4px; font-size: 12px; color: #94a3b8; font-weight: 600; }

/* ---- 右侧聊天 ---- */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-top {
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.chat-friend-name { font-size: 15px; font-weight: 600; color: #0f172a; }

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 20px 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 70%;
}
.msg-other { align-self: flex-start; }
.msg-me { align-self: flex-end; flex-direction: row-reverse; }

.msg-avatar { flex-shrink: 0; }

.msg-bubble {
  background: #fff;
  border-radius: 12px;
  padding: 8px 14px;
  box-shadow: 0 1px 2px rgba(15,23,42,0.05);
}
.bubble-me { background: #3730a3; color: #fff; }
.msg-text { font-size: 14px; line-height: 1.5; word-break: break-word; }
.msg-time { font-size: 10px; color: #94a3b8; text-align: right; margin-top: 2px; }
.bubble-me .msg-time { color: rgba(255,255,255,0.6); }

.chat-input-area {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding: 16px 20px 20px;
  background: #fff;
  border-top: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.chat-input { flex: 1; }

.chat-input :deep(.el-textarea__inner) {
  border-radius: 8px;
}

/* ---- 占位 ---- */
.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
