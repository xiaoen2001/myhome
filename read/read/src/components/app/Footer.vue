<template>
  <footer class="site-footer">
    <div class="footer-content">
      <div class="footer-section brand-section">
        <div class="footer-logo">
          <el-icon :size="24" class="logo-icon"><Reading /></el-icon>
          <span class="logo-text">Read 社区</span>
        </div>
        <p class="brand-desc">
          分享读书心得，探索知识的海洋。致力于打造最纯粹的阅读交流与知识沉淀平台。
        </p>
      </div>

      <div class="footer-section">
        <h4>快捷导航</h4>
        <ul>
          <li><router-link to="/home">返回首页</router-link></li>
          <li><router-link to="/notes">探索笔记</router-link></li>
          <li><router-link to="/notifications">消息中心</router-link></li>
        </ul>
      </div>

      <div class="footer-section">
        <h4>联系我们</h4>
        <ul>
          <li><el-icon><Message /></el-icon> support@read-community.com</li>
          <li><el-icon><Location /></el-icon> 知识宇宙，阅读星系</li>
        </ul>
      </div>
    </div>

    <div class="footer-bottom">
      <p>2024 Read 社区. 赋予阅读新的生命。</p>
    </div>
  </footer>
</template>

<script setup>
import { Reading, Message, Location } from '@element-plus/icons-vue'
</script>

<style scoped>
.site-footer {
  width: 100%;
  margin-top: 80px;
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
}
.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 48px 24px 32px;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 48px;
}
.footer-section h4 {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 16px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #3730a3;
  margin-bottom: 12px;
}
.logo-text {
  font-size: 20px;
  font-weight: 800;
  color: #3730a3;
  letter-spacing: -0.022em;
}
.brand-desc {
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
  max-width: 360px;
}
.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.footer-section ul li {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
  font-size: 14px;
}
.footer-section ul li .el-icon {
  color: #0d9488;
  font-size: 15px;
}
.footer-section a {
  text-decoration: none;
  color: #64748b;
  transition: color 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.footer-section a:hover {
  color: #3730a3;
}
.footer-bottom {
  text-align: center;
  padding: 20px 24px;
  border-top: 1px solid #f1f5f9;
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
    gap: 32px;
    text-align: center;
  }
  .brand-desc {
    max-width: 100%;
  }
  .footer-logo,
  .footer-section ul li {
    justify-content: center;
  }
}
</style>