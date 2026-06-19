<template>
  <div class="change-password-page">
    <div class="form-card-wrapper">
      <div class="form-header">
        <h2>修改密码</h2>
        <p>为了您的账号安全，请定期更新密码</p>
      </div>
      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="password-form"
          label-position="right"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
              v-model="form.oldPassword"
              type="password"
              placeholder="请输入旧密码"
              show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
          />
        </el-form-item>
        <div class="action-buttons">
          <el-button type="primary" class="submit-btn" @click="submitForm" :loading="submitting">
            确认修改
          </el-button>
          <el-button class="reset-btn" @click="resetForm">重置</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {updatePassword} from "@/api/modules/auth.js";

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await updatePassword({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      // 修改成功后退出登录
      userStore.logout()
      router.push('/login')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '修改失败，请检查旧密码')
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
}
</script>

<style scoped>
.change-password-page {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.form-card-wrapper {
  position: relative;
  z-index: 1;
  width: 480px;
  border-radius: 20px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 20px rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-header {
  text-align: center;
  margin-bottom: 35px;
}

.form-header h2 {
  margin: 0;
  font-size: 26px;
  background: linear-gradient(45deg, #4facfe, #00f2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: bold;
  letter-spacing: 2px;
}

.form-header p {
  margin-top: 10px;
  color: #909399;
  font-size: 14px;
}

.password-form {
  margin-top: 10px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #4facfe inset, 0 0 12px rgba(79, 172, 254, 0.4);
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 40px;
  opacity: 0;
  animation: fadeInUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards 0.4s;
}

.submit-btn {
  width: 140px;
  border-radius: 8px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  font-weight: 600;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 172, 254, 0.4);
}

.reset-btn {
  width: 100px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.reset-btn:hover {
  background-color: #f0f2f5;
  color: #606266;
}
</style>