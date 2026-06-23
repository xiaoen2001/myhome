<template>
  <div class="chat-page">
    <div class="chat-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft/></el-icon>
      </el-button>
      <div class="friend-info">
        <el-avatar :size="36" :src="getAvatarUrl(friend.avatar)"/>
        <span class="friend-name">{{ friend.nickname || friend.username }}</span>
      </div>
    </div>

    <div class="message-list" ref="messageListRef">
      <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="msg.senderId === currentUserId ? 'message-self' : 'message-other'"
      >
        <!-- 对方消息：头像在左 -->
        <template v-if="msg.senderId !== currentUserId">
          <el-avatar :size="30" :src="getAvatarUrl(friend.avatar)" class="msg-avatar"/>
          <div class="message-bubble">
            <div class="message-content" v-if="msg.type === 'text'">{{ msg.content }}</div>
            <el-image v-else-if="msg.type === 'image'" :src="msg.content" style="max-width:160px;border-radius:8px" preview-teleported/>
            <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </template>
        <!-- 自己的消息：头像在右 -->
        <template v-else>
          <div class="message-bubble self-bubble">
            <div class="message-content" v-if="msg.type === 'text'">{{ msg.content }}</div>
            <el-image v-else-if="msg.type === 'image'" :src="msg.content" style="max-width:160px;border-radius:8px" preview-teleported/>
            <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
          <el-avatar :size="30" :src="getAvatarUrl(currentUserAvatar)" class="msg-avatar"/>
        </template>
      </div>
    </div>

    <div class="input-area">
      <div class="input-row">
        <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入消息... Enter 发送"
            @keydown.enter.exact.prevent="sendText"
            class="message-input"
        />
        <el-button type="primary" @click="sendText" :icon="Position" circle size="small" class="send-btn"/>
        <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageUpload"
            accept="image/*"
            class="upload-btn"
        >
          <el-button icon="Picture" circle size="small" class="image-btn"/>
        </el-upload>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getAvatarUrl } from '@/utils/avatar'
import {ref, onMounted, nextTick, onBeforeUnmount} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {ArrowLeft, Position} from '@element-plus/icons-vue'
import {useUserStore} from '@/stores/user'
import {getMessages} from '@/api/modules/chat'
import {getUserById} from '@/api/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentUserId = userStore.userId
const currentUserAvatar = userStore.avatar || ''

const friendId = ref(parseInt(route.params.id))
const friend = ref({})
const messages = ref([])
const inputText = ref('')
const messageListRef = ref(null)
let chatSocket = null

const connectWebSocket = () => {
  const token = userStore.token
  if (!token) return
  const wsUrl = `ws://localhost:8080/ws/chat?token=${token}`
  chatSocket = new WebSocket(wsUrl)
  chatSocket.onopen = () => console.log('聊天WebSocket已连接')
  chatSocket.onmessage = (event) => {
    const newMsg = JSON.parse(event.data)
    if (newMsg.senderId === friendId.value || newMsg.receiverId === friendId.value) {
      messages.value.push(newMsg)
      nextTick(() => scrollToBottom())
    }
  }
  chatSocket.onerror = (error) => console.error('聊天WebSocket错误', error)
  chatSocket.onclose = () => console.log('聊天WebSocket已关闭')
}

const disconnectWebSocket = () => {
  if (chatSocket) chatSocket.close()
}

const goBack = () => router.back()

const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2,'0')}:${String(date.getMinutes()).padStart(2,'0')}`
}

const fetchFriendInfo = async () => {
  try {
    friend.value = await getUserById(friendId.value)
  } catch (error) {
    ElMessage.error('加载好友信息失败')
  }
}

const fetchMessages = async () => {
  try {
    messages.value = await getMessages({friendId: friendId.value})
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}

const sendMessageViaWebSocket = (type, content) => {
  if (!chatSocket || chatSocket.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接已断开，请刷新页面重试')
    return
  }
  chatSocket.send(JSON.stringify({ receiverId: friendId.value, type, content }))
}

const sendText = () => {
  if (!inputText.value.trim()) return
  sendMessageViaWebSocket('text', inputText.value)
  inputText.value = ''
}

const handleImageUpload = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => sendMessageViaWebSocket('image', e.target.result)
  reader.readAsDataURL(file.raw)
}

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

onMounted(() => {
  fetchFriendInfo()
  fetchMessages()
  connectWebSocket()
})

onBeforeUnmount(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  max-width: 720px;
  margin: 0 auto;
  background: #f8f9fb;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.friend-name {
  font-weight: 600;
  font-size: 16px;
  color: #0f172a;
}

/* ---- 消息列表 ---- */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.message-other {
  justify-content: flex-start;
}

.message-self {
  justify-content: flex-end;
}

.msg-avatar {
  flex-shrink: 0;
}

/* ---- 消息气泡 ---- */
.message-bubble {
  max-width: 60%;
  background: #fff;
  border-radius: 14px;
  padding: 8px 14px;
  box-shadow: 0 1px 2px rgba(15,23,42,0.06);
}

.self-bubble {
  background: #3730a3;
  color: #fff;
}

.message-content {
  word-break: break-word;
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 3px;
  text-align: right;
}

.self-bubble .message-time {
  color: rgba(255,255,255,0.6);
}

/* ---- 输入区域 ---- */
.input-area {
  background: #fff;
  border-top: 1px solid #e2e8f0;
  padding: 10px 16px;
  flex-shrink: 0;
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.message-input {
  flex: 1;
}

.send-btn {
  flex-shrink: 0;
  background-color: #3730a3;
  border-color: #3730a3;
}

.image-btn {
  flex-shrink: 0;
}

.upload-btn {
  flex-shrink: 0;
}
</style>
