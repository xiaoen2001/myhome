<template>
  <div class="profile-page" v-loading="loading">
    <div class="profile-header">
      <el-avatar :size="100" :src="getAvatarUrl(user.avatar)" />
      <div class="info">
        <h2>{{ user.nickname || user.username }}</h2>
        <p class="username">@{{ user.username }}</p>
        <p class="bio">{{ user.bio || '这个人很懒，什么都没写' }}</p>
        <div class="stats">
          <div><span>笔记</span><strong>{{ user.noteCount }}</strong></div>
          <div><span>收藏</span><strong>{{ user.collectionCount }}</strong></div>
          <div><span>点赞</span><strong>{{ user.likeCount }}</strong></div>
        </div>
        <el-button v-if="isSelf" type="primary" size="large" @click="onedit">更新信息</el-button>
        <el-button v-if="!isSelf && !isFriend" type="primary" size="small" @click="addFriend">加好友</el-button>
      </div>
    </div>
    <div class="profile-notes">
      <h3 v-if="isSelf">我的笔记</h3>
      <h3 v-else>他的笔记</h3>
      <div class="notes-list">
        <el-card v-for="note in userNotes" :key="note.id" class="note-card" @click="goToNote(note.id)">
          <div class="note-cover"><img :src="note.cover || 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'"/></div>
          <div class="note-info">
            <h4>{{ note.title }}</h4>
            <p>{{ note.bookName }}</p>
          </div>
        </el-card>
        <el-empty v-if="!userNotes.length" description="暂无笔记"/>
      </div>
    </div>

    <el-dialog
        v-model="edit"
        title="编辑用户信息"
        width="600px"
        :close-on-click-modal="false"
        @close="handleClose"
    >
      <el-form
          ref="formRef"
          :model="editForm"
          :rules="formRules"
          label-width="80px"
          label-position="right"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="editForm.email"
              placeholder="请输入邮箱"
              clearable
          />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
              v-model="editForm.nickname"
              placeholder="请输入昵称"
              clearable
              maxlength="20"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="头像" prop="photo">
          <div class="avatar-upload-container">
            <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="handleBeforeUpload"
                :http-request="handleCustomUpload"
            >
              <div v-if="editForm.photo" class="avatar-preview">
                <img :src="editForm.photo" alt="头像预览"/>
                <div class="avatar-mask">
                  <el-icon>
                    <Edit/>
                  </el-icon>
                </div>
              </div>
              <el-icon v-else class="avatar-uploader-icon">
                <Plus/>
              </el-icon>
            </el-upload>
            <el-button
                v-if="editForm.photo"
                type="danger"
                size="small"
                link
                class="remove-avatar-btn"
                @click="removeAvatar"
            >
              删除头像
            </el-button>
          </div>
          <div class="avatar-tip">支持 jpg、png 格式，大小不超过 2MB</div>
        </el-form-item>

        <el-form-item label="简介" prop="brief">
          <el-input
              v-model="editForm.brief"
              type="textarea"
              :rows="4"
              placeholder="请输入个人简介"
              maxlength="200"
              show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getAvatarUrl } from '@/utils/avatar'
import {computed, nextTick, onMounted, reactive, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {useUserStore} from '@/stores/user'
import {addFriendRequest, checkFriendStatus, getUserNotes, getUserProfile, putUserProfile} from '@/api/modules/user'
import {Plus} from "@element-plus/icons-vue";


const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const userId = computed(() => parseInt(route.params.id))
const isSelf = computed(() => userId.value === userStore.userId)

const loading = ref(false)
const user = ref({})
const userNotes = ref([])
const isFriend = ref(false)

// 编辑相关
const edit = ref(false)
const editForm = reactive({
  id: null,
  email: null,
  nickname: null,
  photo: null,
  brief: null
})

// 表单引用与提交状态
const formRef = ref(null)
const submitLoading = ref(false)

// 默认空表单（用于重置）
const getDefaultForm = () => ({
  id: null,
  email: null,
  nickname: null,
  photo: null,
  brief: null
})

// 表单验证规则
const formRules = {
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change']}
  ],
  nickname: [
    {required: true, message: '请输入昵称', trigger: 'blur'},
    {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
  ],
  brief: [
    {max: 200, message: '简介不能超过 200 个字符', trigger: 'blur'}
  ]
}

// 重置表单
const resetForm = () => {
  Object.assign(editForm, getDefaultForm())
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

// 头像上传前校验
const handleBeforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义上传（转为 Base64 预览）
const handleCustomUpload = (options) => {
  const {file, onSuccess, onError} = options

  const reader = new FileReader()

  reader.onload = (e) => {
    editForm.photo = e.target.result
    onSuccess(null,file)
  }
  reader.onerror = () => {
    onError(new Error('读取文件失败'))
    ElMessage.error('头像读取失败，请重试')
  }
  reader.readAsDataURL(file)
}

// 删除头像
const removeAvatar = () => {
  editForm.photo = null
  ElMessage.info('已删除头像')
}

// 打开编辑对话框，填充当前用户信息
const onedit = () => {
  // 将当前用户数据复制到编辑表单
  editForm.id = user.value.id
  editForm.email = user.value.email
  editForm.nickname = user.value.nickname || ''
  editForm.photo = user.value.avatar || null
  editForm.brief = user.value.bio || ''
  edit.value = true
}

// 关闭对话框
const handleClose = () => {
  edit.value = false
  resetForm()
}

// 提交表单（保存）
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const data = {
      id: editForm.id,
      email: editForm.email,
      nickname: editForm.nickname,
      photo: editForm.photo,
      brief: editForm.brief
    }
    await putUserProfile(data)   // 不依赖返回值

    // 重新获取当前用户的最新信息
    const freshUser = await getUserProfile(userStore.userId)
    // 同步到全局 store
    userStore.avatar = freshUser.avatar
    userStore.nickname = freshUser.nickname

    ElMessage.success('保存成功')
    edit.value = false
    resetForm()
    await fetchUserProfile()   // 刷新当前页面数据
  } catch (error) {
    console.error('更新失败:', error)
    const msg = error.response?.data?.msg || error.message || '保存失败，请重试'
    ElMessage.error(msg)
  } finally {
    submitLoading.value = false
  }
}

// 获取用户资料
const fetchUserProfile = async () => {
  loading.value = true
  try {
    const res = await getUserProfile(userId.value)
    console.log(res)
    user.value = res
    if (!isSelf.value) {
      isFriend.value = await checkFriendStatus(userId.value)
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  } finally {
    loading.value = false
  }
}

// 获取用户笔记列表
const fetchUserNotes = async () => {
  try {
    const res = await getUserNotes(userId.value)
    userNotes.value = res
  } catch (error) {
    console.error('加载笔记列表失败', error)
  }
}

// 添加好友
const addFriend = async () => {
  try {
    await addFriendRequest(userId.value)
    ElMessage.success('好友申请已发送')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '申请失败')
  }
}

// 跳转笔记详情
const goToNote = (id) => {
  router.push(`/notes/${id}`)
}

// 监听路由参数变化，重新加载数据
watch(() => route.params.id, (newId, oldId) => {
  if (newId !== oldId) {
    fetchUserProfile()
    fetchUserNotes()
  }
})

onMounted(() => {
  fetchUserProfile()
  fetchUserNotes()
})
</script>

<style scoped>
/* 原有样式保持不变 */
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  display: flex;
  gap: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  margin-bottom: 30px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.05);
  animation: fadeInUp 0.5s ease forwards;
}

.info {
  flex: 1;
}

.username {
  color: #8c9aa6;
  margin-bottom: 8px;
}

.bio {
  margin: 8px 0;
}

.stats {
  display: flex;
  gap: 30px;
  margin: 16px 0;
}

.stats div {
  text-align: center;
}

.stats strong {
  display: block;
  font-size: 20px;
}

.notes-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.note-card {
  cursor: pointer;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease forwards;
}
.note-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 15px 30px rgba(79, 172, 254, 0.2);
}

.note-cover {
  height: 140px;
  overflow: hidden;
}

.note-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.note-info {
  padding: 12px;
}

.note-info h4 {
  margin-bottom: 4px;
  font-size: 16px;
}

.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 100px;
  height: 100px;
  background-color: #fafafa;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  position: relative;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-preview:hover .avatar-mask {
  opacity: 1;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.remove-avatar-btn {
  margin-top: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>