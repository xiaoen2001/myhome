<template>
  <div class="login-container">

    <div class="login-card">
      <div class="login-header">
        <h2 class="login-title">账号登录</h2>
        <p class="login-subtitle">欢迎使用读书笔记分享平台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
              class="login-input"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              size="large"
              class="login-input"
          />
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <el-input
                v-model="form.captcha"
                placeholder="验证码"
                prefix-icon="CircleCheck"
                size="large"
                class="login-input captcha-input"
            />
            <div class="captcha-img-box" @click="refreshCaptcha" title="点击刷新验证码">
              <canvas ref="captchaCanvas" width="120" height="50"></canvas>
            </div>
          </div>
        </el-form-item>

        <div class="link-row">
          <el-link type="primary" @click="goToForgot" class="link-item">忘记密码</el-link>
          <el-link type="primary" @click="goToRegister" class="link-item">注册账号</el-link>
        </div>

        <el-form-item class="btn-wrapper">
          <el-button
              type="primary"
              @click="handleLogin"
              :loading="loading"
              class="login-btn"
              size="large"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/modules/auth'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const captchaCanvas = ref(null) // Canvas 引用

// 响应式表单数据
const form = reactive({
  username: '',
  password: '',
  captcha: '' // [新增]
})

// 验证码逻辑变量
let identifyCode = ""
const identifyCodes = "ABCD1234567890efghijkmnpqrstuvwxyz"

// 表单验证规则
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// [核心功能] 生成随机数
const randomNum = (min, max) => Math.floor(Math.random() * (max - min) + min)

// [核心功能] 绘制验证码
const refreshCaptcha = () => {
  identifyCode = ""
  const canvas = captchaCanvas.value
  const ctx = canvas.getContext('2d')

  // 清空画布
  ctx.fillStyle = '#f3f4f6'
  ctx.fillRect(0, 0, canvas.width, canvas.height)

  // 生成4位随机验证码
  for (let i = 0; i < 4; i++) {
    const text = identifyCodes[randomNum(0, identifyCodes.length)]
    identifyCode += text
    const fontSize = randomNum(25, 35)
    const deg = randomNum(-30, 30)
    ctx.font = `${fontSize}px Arial`
    ctx.textBaseline = 'top'
    ctx.fillStyle = `rgb(${randomNum(50, 150)},${randomNum(50, 150)},${randomNum(50, 150)})`

    ctx.save()
    ctx.translate(20 * i + 20, 15)
    ctx.rotate(deg * Math.PI / 180)
    ctx.fillText(text, -10, -15)
    ctx.restore()
  }

  // 绘制5条干扰线
  for (let i = 0; i < 5; i++) {
    ctx.strokeStyle = `rgb(${randomNum(100, 255)},${randomNum(100, 255)},${randomNum(100, 255)})`
    ctx.beginPath()
    ctx.moveTo(randomNum(0, canvas.width), randomNum(0, canvas.height))
    ctx.lineTo(randomNum(0, canvas.width), randomNum(0, canvas.height))
    ctx.stroke()
  }

  // 绘制20个干扰点
  for (let i = 0; i < 20; i++) {
    ctx.fillStyle = `rgb(${randomNum(0, 255)},${randomNum(0, 255)},${randomNum(0, 255)})`
    ctx.beginPath()
    ctx.arc(randomNum(0, canvas.width), randomNum(0, canvas.height), 1, 0, 2 * Math.PI)
    ctx.fill()
  }
}

onMounted(() => {
  refreshCaptcha()
})

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // [新增] 前端验证码校验（不区分大小写）
    if (form.captcha.toLowerCase() !== identifyCode.toLowerCase()) {
      ElMessage.error('验证码错误，请重新输入')
      refreshCaptcha()
      form.captcha = ''
      return
    }

    loading.value = true
    try {
      const res = await login({
        username: form.username,
        password: form.password
      })
      userStore.setUser(res)
      ElMessage.success('登录成功')
      router.push('/home')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '登录失败')
      refreshCaptcha() // 登录失败也刷新验证码
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => router.push('/register')
const goToForgot = () => router.push('/forgot-password')
</script>

<style scoped>
/* 保持原有容器和卡片样式 ... */
.login-container {
  position: relative;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 20px;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25), 0 0 20px rgba(255, 255, 255, 0.2);
  padding: 50px 45px;
  box-sizing: border-box;
  animation: fadeInUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(30px, 30px) scale(1.05); }
  100% { transform: translate(-20px, 50px) scale(0.95); }
}

/* [新增样式] 验证码布局 */
.captcha-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.captcha-input {
  flex: 1;
}

.captcha-img-box {
  cursor: pointer;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  height: 50px;
  display: flex;
  align-items: center;
  background: #f3f4f6;
}

.captcha-img-box:hover {
  border-color: #667eea;
}

/* 之前已有的其他样式保持不变 ... */
.login-header { text-align: center; margin-bottom: 40px; }
.login-title { 
  font-size: 28px; 
  font-weight: 700; 
  margin-bottom: 8px; 
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.login-subtitle { font-size: 15px; color: #6b7280; }
.login-form { display: flex; flex-direction: column; gap: 20px; }

/* 表单项依次入场动效 */
.login-form .el-form-item, .link-row {
  opacity: 0;
  animation: fadeInUp 0.5s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
.login-form .el-form-item:nth-child(1) { animation-delay: 0.1s; }
.login-form .el-form-item:nth-child(2) { animation-delay: 0.2s; }
.login-form .el-form-item:nth-child(3) { animation-delay: 0.3s; }
.link-row { animation-delay: 0.4s; }
.btn-wrapper { animation-delay: 0.5s; }

.login-input {
  --el-input-border-radius: 14px;
  --el-input-height: 50px;
  --el-input-hover-border-color: #4facfe;
  --el-input-focus-border-color: #4facfe;
}
.link-row { display: flex; justify-content: space-between; font-size: 14px; }
.link-item { color: #4facfe !important; font-weight: 500; }

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #4facfe inset, 0 0 12px rgba(79, 172, 254, 0.4) !important;
}

.login-btn {
  width: 100%; height: 52px; border-radius: 14px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  border: none; font-size: 16px; font-weight: 600; cursor: pointer;
  transition: all 0.3s ease;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(79, 172, 254, 0.4);
}
</style>