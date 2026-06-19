<template>
  <div class="chat-page">
    <div class="chat-header">
      <el-button link @click="goBack">
        <el-icon>
          <ArrowLeft/>
        </el-icon>
      </el-button>
      <div class="friend-info">
        <el-avatar :size="40" :src="getAvatarUrl(friend.avatar)"/>
        <span class="friend-name">{{ friend.nickname || friend.username }}</span>
      </div>
    </div>

    <div class="message-list" ref="messageListRef">
      <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'message-self': msg.senderId === currentUserId, 'message-other': msg.senderId !== currentUserId }"
      >
        <el-avatar v-if="msg.senderId !== currentUserId" :size="32" :src="getAvatarUrl(friend.avatar)" class="avatar"/>
        <div class="message-bubble">
          <div class="message-content" v-if="msg.type === 'text'">{{ msg.content }}</div>
          <div class="message-image" v-else-if="msg.type === 'image'">
            <el-image :src="msg.content" style="max-width: 200px; border-radius: 8px;" preview-teleported/>
          </div>
          <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
        </div>
        <el-avatar v-if="msg.senderId === currentUserId" :size="32" :src="getAvatarUrl(currentUserAvatar)" class="avatar"/>
      </div>
    </div>

    <div class="input-area">
      <div class="input-row">
        <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入消息...（Enter 发送，Shift+Enter 换行）"
            @keydown.enter.exact.prevent="sendText"
            @keydown.shift.enter="inputText += '\n'"
            class="message-input"
        />
        <el-button type="primary" @click="sendText" circle class="send-btn">
          <el-icon color="#656363"><Position /></el-icon>
        </el-button>
        <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageUpload"
            accept="image/*"
            class="upload-btn"
        >
          <el-button icon="Picture" circle class="image-btn"/>
        </el-upload>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getAvatarUrl } from '@/utils/avatar'
import {ref, onMounted, nextTick, onBeforeUnmount, computed} from 'vue'
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
const currentUserAvatar = userStore.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const friendId = ref(parseInt(route.params.id))
const friend = ref({})
const messages = ref([])
const inputText = ref('')
const messageListRef = ref(null)
// 在 Chat.vue 中添加
let chatSocket = null;

const connectWebSocket = () => {
  const token = userStore.token;
  if (!token) return;
  const wsUrl = `ws://localhost:8080/ws/chat?token=${token}`;
  chatSocket = new WebSocket(wsUrl);
  chatSocket.onopen = () => console.log('聊天WebSocket已连接');
  chatSocket.onmessage = (event) => {
    const newMsg = JSON.parse(event.data);
    // 只处理与当前好友相关的消息
    if (newMsg.senderId === friendId.value || newMsg.receiverId === friendId.value) {
      messages.value.push(newMsg);
      nextTick(() => scrollToBottom());
    }
  };
  chatSocket.onerror = (error) => console.error('聊天WebSocket错误', error);
  chatSocket.onclose = () => console.log('聊天WebSocket已关闭');
};

const disconnectWebSocket = () => {
  if (chatSocket) chatSocket.close();
};

const goBack = () => {
  router.back()
}

const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`
}

const fetchFriendInfo = async () => {
  try {
    const res = await getUserById(friendId.value)
    friend.value = res
  } catch (error) {
    ElMessage.error('加载好友信息失败')
  }
}
const fetchMessages = async () => {
  try {
    const res = await getMessages({friendId: friendId.value})
    messages.value = res
    await nextTick()
    scrollToBottom()
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}
const sendMessageViaWebSocket = (type, content) => {
  if (!chatSocket || chatSocket.readyState !== WebSocket.OPEN) {
    ElMessage.error('连接已断开，请刷新页面重试');
    return;
  }
  const msgData = {
    receiverId: friendId.value,
    type: type,
    content: content
  };
  chatSocket.send(JSON.stringify(msgData));
};

const sendText = () => {
  if (!inputText.value.trim()) return;
  sendMessageViaWebSocket('text', inputText.value);
  inputText.value = '';
};

const handleImageUpload = (file) => {
  const reader = new FileReader();
  reader.onload = (e) => {
    sendMessageViaWebSocket('image', e.target.result);
  };
  reader.readAsDataURL(file.raw);
};

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

onMounted(() => {
  fetchFriendInfo();
  fetchMessages();
  connectWebSocket();
})

onBeforeUnmount(() => {
  disconnectWebSocket();
});
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 60px);
  background: #f5f7fa;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.friend-name {
  font-weight: 600;
  font-size: 18px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-self {
  flex-direction: row-reverse;
}

.message-bubble {
  max-width: 70%;
  background: white;
  border-radius: 18px;
  padding: 10px 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-self .message-bubble {
  background: #1e3a8a;
  color: white;
}

.message-content {
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: #8c9aa6;
  margin-top: 4px;
  text-align: right;
}

.message-self .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.avatar {
  flex-shrink: 0;
}

/* 核心调整：输入区域横向布局 */
.input-area {
  background: white;
  border-top: 1px solid #e5e7eb;
  padding: 12px 20px;
}

/* 横向行容器：flex 布局，保证输入框占满剩余空间 */
.input-row {
  display: flex;
  align-items: center;
  gap: 12px; /* 元素之间的间距 */
  width: 100%;
}

/* 输入框样式：flex:1 占满剩余空间 */
.message-input {
  flex: 1;
  min-height: 40px; /* 保证输入框最小高度 */
  resize: none; /* 禁止手动调整大小（可选） */
}

/* 发送按钮样式微调 */
.send-btn {
  white-space: nowrap; /* 防止按钮文字换行 */
  padding: 8px 16px;
  background-color: #ffffff; /* 保持主题色 */
  border: solid 1px #bdc1d1;
}

.send-btn:hover{
  background-color: #98f4d6;
  border: solid 1px #308f6f;
}

/* 图片按钮容器：消除默认样式，保证按钮对齐 */
.upload-btn {
  flex-shrink: 0; /* 防止按钮被压缩 */
}

/* 图片按钮样式 */
.image-btn {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}
</style>