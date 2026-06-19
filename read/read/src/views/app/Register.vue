<template>
  <div class="register-container">

    <el-card class="register-card">
      <!-- 顶部logo/标题区域 -->
      <div class="register-header">
        <div class="register-icon">
          <el-icon size="32">
            <UserFilled/>
          </el-icon>
        </div>
        <h2 class="register-title">创建账号</h2>
        <p class="register-subtitle">填写以下信息开始您的旅程</p>
      </div>

      <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="0"
          class="register-form"
      >
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              class="form-input"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
              v-model="form.email"
              placeholder="请输入邮箱地址"
              prefix-icon="Message"
              class="form-input"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
              v-model="form.nickname"
              placeholder="请输入昵称（可选）"
              prefix-icon="UserFilled"
              class="form-input"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请设置密码（至少6位）"
              prefix-icon="Lock"
              show-password
              class="form-input"
              size="large"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认密码"
              prefix-icon="Lock"
              show-password
              class="form-input"
              size="large"
          />
        </el-form-item>

        <el-form-item class="form-submit">
          <el-button
              type="primary"
              @click="handleRegister"
              :loading="loading"
              class="register-btn"
              size="large"
              round
          >
            完成注册
          </el-button>
        </el-form-item>

        <el-form-item class="form-footer">
          <el-link
              type="primary"
              @click="goToLogin"
              class="login-link"
          >
            已有账号？立即登录
          </el-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElIcon} from 'element-plus'
import {UserFilled} from '@element-plus/icons-vue'
import {register} from '@/api/modules/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
// 自定义验证器：不能包含中文
const validateNoChinese = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const chineseRegex = /[\u4e00-\u9fa5]/
  if (chineseRegex.test(value)) {
    callback(new Error('用户名不能包含中文'))
  } else {
    callback()
  }
}
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { validator: validateNoChinese, trigger: 'blur' }
  ],
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 6, message: '密码长度不能小于6位', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请再次输入密码', trigger: 'blur'},
    {validator: validateConfirm, trigger: 'blur'}
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register({
        username: form.username,
        password: form.password,
        email: form.email,
        nickname: form.nickname
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* 全局容器 */
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 卡片样式 */
.register-card {
  width: 480px;
  padding: 40px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 20px rgba(255, 255, 255, 0.2);
  animation: fadeInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  transition: all 0.3s ease;
}

.register-card:hover {
  box-shadow: 0 15px 45px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 头部区域 */
.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-icon {
  width: 70px;
  height: 70px;
  line-height: 70px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
  text-align: center;
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* 表单样式 */
.register-form {
  margin-top: 16px;
}

.form-input {
  border-radius: 12px;
  margin-bottom: 2px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

.form-input:focus-within {
  border-color: #4facfe;
  box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.1);
}

/* 提交按钮 */
.form-submit {
  margin-top: 24px;
}

.register-btn {
  width: 100%;
  height: 50px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(79, 172, 254, 0.3);
}

/* 底部链接 */
.form-footer {
  text-align: center;
  margin-top: 16px;
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #4facfe;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
}

.login-link:hover {
  color: #00f2fe;
  text-decoration: underline;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .register-card {
    width: 90%;
    padding: 30px 20px;
  }

  .decor-bg {
    display: none;
  }
}

/* 覆盖element默认样式 */
:deep(.el-card) {
  border: none;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 0 16px;
  height: 50px;
  box-shadow: none;
}

:deep(.el-input__prefix) {
  color: #9ca3af;
}

:deep(.el-button--primary) {
  --el-button-hover-bg: linear-gradient(135deg, #3fa1f5 0%, #00d2df 100%);
  --el-button-active-bg: linear-gradient(135deg, #308cd6 0%, #00bac6 100%);
}
</style>