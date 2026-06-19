<template>
  <div id="app-wrapper">
    <div class="main-router-view">
      <RouterView v-slot="{ Component }">
        <Transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </div>
  </div>
</template>

<script setup>
import {RouterView} from "vue-router";
</script>

<style>
/* === Taste-Skill Calibrated App Root === */

/* 全局覆盖 Element Plus 主题色 — 靛蓝 + 青蓝点缀 */
:root {
  --el-color-primary: #3730a3;
  --el-color-primary-rgb: 55, 48, 163;
  --el-color-primary-light-3: #4f46e5;
  --el-color-primary-light-5: #6366f1;
  --el-color-primary-light-7: #a5b4fc;
  --el-color-primary-light-8: #c7d2fe;
  --el-color-primary-light-9: #e0e7ff;
  --el-color-primary-dark-2: #312e81;
  --el-color-success: #059669;
  --el-color-warning: #d97706;
  --el-color-danger: #dc2626;
}

/* 干净冷灰背景 — 非粉色、非暖黄色 */
#app-wrapper {
  min-height: 100dvh;
  background-color: #f8f9fb;
  position: relative;
}

.main-router-view {
  position: relative;
  z-index: 10;
}

/* 页面路由切换 — 简洁淡入淡出 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.18s cubic-bezier(0.16, 1, 0.3, 1);
}
.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
}

/* 清除 Element Plus 默认背景噪声 */
.el-header,
.el-footer {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.el-dialog__header,
.el-dialog__footer,
.el-card__header {
  background: transparent !important;
  border-bottom: 1px solid #e2e8f0;
}
</style>