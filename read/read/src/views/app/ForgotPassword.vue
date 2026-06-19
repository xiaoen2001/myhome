<template>
  <div class="forgot-password-container">
    <el-card class="forgot-card">
      <div class="card-header">
        <h2 class="title">忘记密码</h2>
        <p class="subtitle">验证您的身份并重置密码</p>
      </div>

      <!-- 步骤指示器 -->
      <el-steps
          :active="activeStep"
          finish-status="success"
          align-center
          class="steps-container"
      >
        <el-step
            v-for="(step, index) in steps"
            :key="index"
            :title="step.title"
            :icon="step.icon"
            class="step-item"
        />
      </el-steps>

      <!-- 步骤1：身份验证 -->
      <div v-if="activeStep === 0" class="step-content">
        <el-form
            :model="step1Form"
            :rules="step1Rules"
            ref="step1FormRef"
            label-width="0"
            class="form-container"
        >
          <el-form-item prop="email" class="form-item">
            <el-input
                v-model="step1Form.email"
                placeholder="注册邮箱"
                prefix-icon="Message"
                size="large"
                class="form-input"
                :class="{ 'input-focus': isEmailFocus }"
                @focus="isEmailFocus = true"
                @blur="isEmailFocus = false"
            />
          </el-form-item>
          <el-form-item prop="code" class="form-item">
            <div class="code-input">
              <el-input
                  v-model="step1Form.code"
                  placeholder="验证码"
                  prefix-icon="Key"
                  size="large"
                  class="form-input code-input__field"
                  :class="{ 'input-focus': isCodeFocus }"
                  @focus="isCodeFocus = true"
                  @blur="isCodeFocus = false"
              />
              <el-button
                  :disabled="codeSending || countdown > 0"
                  @click="sendCode"
                  class="code-btn"
                  :class="{ 'btn-disabled': codeSending || countdown > 0 }"
              >
                {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
        <el-button
            type="primary"
            size="large"
            @click="verifyCode"
            :loading="verifying"
            class="action-btn next-btn"
        >
          下一步
        </el-button>
      </div>

      <!-- 步骤2：重置密码 -->
      <div v-else-if="activeStep === 1" class="step-content">
        <el-form
            :model="step2Form"
            :rules="step2Rules"
            ref="step2FormRef"
            label-width="0"
            class="form-container"
        >
          <el-form-item prop="newPassword" class="form-item">
            <el-input
                v-model="step2Form.newPassword"
                type="password"
                placeholder="新密码（至少6位）"
                prefix-icon="Lock"
                show-password
                size="large"
                class="form-input"
                :class="{ 'input-focus': isPwdFocus }"
                @focus="isPwdFocus = true"
                @blur="isPwdFocus = false"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword" class="form-item">
            <el-input
                v-model="step2Form.confirmPassword"
                type="password"
                placeholder="确认新密码"
                prefix-icon="Lock"
                show-password
                size="large"
                class="form-input"
                :class="{ 'input-focus': isConfirmPwdFocus }"
                @focus="isConfirmPwdFocus = true"
                @blur="isConfirmPwdFocus = false"
            />
          </el-form-item>
        </el-form>
        <el-button
            type="primary"
            size="large"
            @click="resetPassword"
            :loading="resetting"
            class="action-btn reset-btn"
        >
          确认重置
        </el-button>
      </div>

      <!-- 步骤3：完成 -->
      <div v-else class="step-content success-step">
        <el-result
            icon="success"
            title="密码重置成功"
            sub-title="请使用新密码登录，祝您使用愉快～"
            class="success-result"
        >
          <template #extra>
            <el-button
                type="primary"
                @click="goToLogin"
                class="success-btn"
            >
              去登录
            </el-button>
          </template>
        </el-result>
      </div>

      <div class="login-link">
        <el-link
            type="primary"
            @click="goToLogin"
            class="back-link"
            :underline="false"
            hover="underline"
        >
          返回登录
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import {ref, reactive} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {sendResetCode, resetPassword as apiResetPassword,confirmCode} from '@/api/modules/auth'

const router = useRouter()
const activeStep = ref(0)
const codeSending = ref(false)
const countdown = ref(0)
const verifying = ref(false)
const resetting = ref(false)

// 步骤配置（新增）
const steps = ref([
  {title: '身份验证', icon: 'User'},
  {title: '重置密码', icon: 'Lock'},
  {title: '完成', icon: 'CircleCheck'}
])

// 输入框焦点状态（新增）
const isEmailFocus = ref(false)
const isCodeFocus = ref(false)
const isPwdFocus = ref(false)
const isConfirmPwdFocus = ref(false)

// 步骤1表单
const step1FormRef = ref(null)
const step1Form = reactive({
  email: '',
  code: ''
})
const step1Rules = {
  email: [
    {required: true, message: '请输入邮箱', trigger: 'blur'},
    {type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur'}
  ],
  code: [
    {required: true, message: '请输入验证码', trigger: 'blur'}
  ]
}

// 步骤2表单
const step2FormRef = ref(null)
const step2Form = reactive({
  newPassword: '',
  confirmPassword: ''
})
const step2Rules = {
  newPassword: [
    {required: true, message: '请输入新密码', trigger: 'blur'},
    {min: 6, message: '密码长度不能小于6位', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请再次输入密码', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value !== step2Form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 发送验证码
const sendCode = async () => {
  if (!step1Form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  codeSending.value = true
  try {
    await sendResetCode({email: step1Form.email})
    ElMessage.success('验证码已发送，请查收')
    // 倒计时60秒
    countdown.value = 60
    const timer = setInterval(() => {
      if (countdown.value <= 1) {
        clearInterval(timer)
        countdown.value = 0
      } else {
        countdown.value--
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送失败')
  } finally {
    codeSending.value = false
  }
}

// 验证验证码（步骤1 -> 步骤2）
const verifyCode = async () => {
  if (!step1FormRef.value) return
  await step1FormRef.value.validate(async (valid) => {
    if (!valid) return
    verifying.value = true
    try {
      //todo confirmCode
      const data = {
        'email': step1Form.email,
        'code':step1Form.code
      }
      await confirmCode(data);
      activeStep.value = 1
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '验证失败')
    } finally {
      verifying.value = false
    }
  })
}

// 重置密码
const resetPassword = async () => {
  if (!step2FormRef.value) return
  await step2FormRef.value.validate(async (valid) => {
    if (!valid) return
    resetting.value = true
    try {
      await apiResetPassword({
        email: step1Form.email,
        code: step1Form.code,
        newPassword: step2Form.newPassword
      })
      activeStep.value = 2
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '重置失败')
    } finally {
      resetting.value = false
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
/* 全局容器 */
.forgot-password-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 更柔和的渐变背景 */
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  padding: 20px;
}

/* 卡片样式 */
.forgot-card {
  width: 520px;
  padding: 40px 50px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  border: none;
  background: #ffffff;
  transition: all 0.3s ease;
}

.forgot-card:hover {
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

/* 卡片头部 */
.card-header {
  text-align: center;
  margin-bottom: 35px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.subtitle {
  color: #7f8c8d;
  font-size: 14px;
  margin: 0;
}

/* 步骤指示器 */
.steps-container {
  margin-bottom: 40px;
  padding: 0 20px;
}

.step-item {
  --el-step-title-font-size: 14px;
  --el-step-icon-size: 24px;
}

/* 步骤内容 */
.step-content {
  margin: 10px 0 30px;
}

/* 表单样式 */
.form-container {
  margin-bottom: 25px;
}

.form-item {
  margin-bottom: 20px;
}

.form-input {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  height: 50px;
  transition: all 0.3s ease;
  --el-input-text-color: #333;
  --el-input-placeholder-color: #9ca3af;
}

.form-input.input-focus {
  border-color: #84fab0;
  box-shadow: 0 0 0 3px rgba(132, 250, 176, 0.2);
}

/* 验证码输入框 */
.code-input {
  display: flex;
  gap: 12px;
  align-items: center;
}

.code-input__field {
  flex: 1;
}

.code-btn {
  white-space: nowrap;
  padding: 0 20px;
  height: 50px;
  border-radius: 12px;
  font-size: 14px;
  background: #f8fafc;
  color: #2c3e50;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  width: 150px;
}

.code-btn:not(.btn-disabled):hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.code-btn.btn-disabled {
  background: #fafafa !important;
  color: #9ca3af !important;
  border-color: #f0f0f0 !important;
  cursor: not-allowed;
}

/* 操作按钮 */
.action-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  transition: all 0.3s ease;
}

.next-btn {
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  --el-button-hover-bg-color: linear-gradient(120deg, #74e89f 0%, #7fc8e8 100%);
}

.reset-btn {
  background: linear-gradient(120deg, #667eea 0%, #764ba2 100%);
  --el-button-hover-bg-color: linear-gradient(120deg, #5a6edb 0%, #6b449a 100%);
}

/* 成功步骤 */
.success-step {
  text-align: center;
  padding: 10px 0;
}

.success-result {
  --el-result-title-font-size: 20px;
  --el-result-subtitle-font-size: 14px;
  --el-result-icon-size: 60px;
  color: #2c3e50;
}

.success-btn {
  border-radius: 12px;
  padding: 10px 30px;
  font-size: 15px;
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%);
  border: none;
}

/* 返回登录链接 */
.login-link {
  text-align: center;
  margin-top: 10px;
}

.back-link {
  font-size: 14px;
  color: #667eea !important;
  transition: all 0.3s ease;
}

.back-link:hover {
  color: #5a6edb !important;
}

/* 响应式适配 */
@media (max-width: 576px) {
  .forgot-card {
    width: 100%;
    padding: 30px 25px;
  }

  .steps-container {
    padding: 0;
  }

  .code-btn {
    padding: 0 15px;
    font-size: 13px;
  }
}
</style>