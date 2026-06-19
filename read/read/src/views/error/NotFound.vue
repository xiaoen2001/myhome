<template>
  <div class="not-found-container">
    <div class="not-found-card">
      <div class="number-wrap">
        <span class="digit">4</span>
        <div class="circle"></div>
        <span class="digit">4</span>
      </div>

      <h2 class="title">页面未找到</h2>
      <p class="desc">你访问的页面不存在或已被移动</p>

      <router-link to="/" class="back-home">
        返回首页
      </router-link>
    </div>
  </div>
</template>

<style scoped>
/* 整体背景 —— 与登录页保持同主题渐变 */
.not-found-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 20px;
}

/* 卡片 */
.not-found-card {
  background: #ffffff;
  border-radius: 28px;
  padding: 60px 50px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  max-width: 460px;
  width: 100%;
  animation: fadeIn 0.6s ease forwards;
}

/* 404 数字区域 */
.number-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 24px;
}

.digit {
  font-size: 80px;
  font-weight: 900;
  color: #667eea;
  line-height: 1;
}

.circle {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  animation: bounce 1.2s infinite ease-in-out;
}

/* 标题 */
.title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 8px 0;
}

/* 描述 */
.desc {
  font-size: 15px;
  color: #6b7280;
  margin: 0 0 36px 0;
}

/* 返回首页按钮 */
.back-home {
  display: inline-block;
  padding: 14px 36px;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
}

.back-home:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
}

/* 动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes bounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.15);
  }
}

/* 移动端适配 */
@media (max-width: 480px) {
  .not-found-card {
    padding: 40px 30px;
  }

  .digit {
    font-size: 60px;
  }

  .title {
    font-size: 24px;
  }
}
</style>