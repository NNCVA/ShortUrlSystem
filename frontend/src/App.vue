<template>
  <router-view />
</template>

<script setup lang="ts">
// App 根组件
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Poppins:wght@400;500;600;700;800&display=swap');

:root {
  /* 主色调 - 马卡龙薄荷风 */
  --color-primary: #FF7E9F;
  --color-secondary: #5AE4A8;
  --color-accent: #B794F6;
  --color-coral: #FF6B9D;

  /* 渐变色 */
  --gradient-bg: linear-gradient(135deg, #5AE4A8 0%, #FFB5C5 50%, #E9D5FF 100%);
  --gradient-btn: linear-gradient(135deg, #FF7E9F 0%, #FF6B9D 100%);
  --gradient-btn-hover: linear-gradient(135deg, #FF8EAB 0%, #FF7E9F 100%);

  /* 文字颜色 */
  --color-text-primary: #2D3748;
  --color-text-secondary: #718096;
  --color-text-light: #A0AEC0;

  /* 卡片 */
  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(255, 255, 255, 0.5);
  --glass-shadow: 0 8px 32px rgba(31, 38, 135, 0.15);

  /* 圆角 */
  --radius-sm: 8px;
  --radius-md: 16px;
  --radius-lg: 24px;
  --radius-xl: 32px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: var(--gradient-bg);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  min-height: 100vh;
  color: var(--color-text-primary);
}

@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

#app {
  width: 100%;
  min-height: 100vh;
}

/* 全局滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, var(--color-coral), var(--color-primary));
}

/* Element Plus 覆盖样式 */
.el-button--primary {
  background: var(--gradient-btn) !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(255, 126, 159, 0.4) !important;
  transition: all 0.3s ease !important;
}

.el-button--primary:hover {
  background: var(--gradient-btn-hover) !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 126, 159, 0.5) !important;
}

.el-input__wrapper {
  border-radius: var(--radius-md) !important;
  box-shadow: none !important;
  border: 2px solid transparent !important;
  background: rgba(255, 255, 255, 0.8) !important;
  transition: all 0.3s ease !important;
}

.el-input__wrapper:hover {
  border-color: rgba(255, 126, 159, 0.3) !important;
}

.el-input__wrapper.is-focus {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 3px rgba(255, 126, 159, 0.2) !important;
}

.el-card {
  border: none !important;
  border-radius: var(--radius-lg) !important;
}

.el-table {
  --el-table-border-color: rgba(255, 255, 255, 0.3);
  --el-table-header-bg-color: rgba(255, 126, 159, 0.1);
}

.el-table th.el-table__cell {
  background: linear-gradient(135deg, rgba(255, 126, 159, 0.1), rgba(183, 148, 246, 0.1)) !important;
}

.el-dialog {
  border-radius: var(--radius-lg) !important;
  overflow: hidden;
}

.el-dialog__header {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  margin: 0 !important;
  padding: 20px !important;
}

.el-dialog__title {
  color: white !important;
  font-family: 'Poppins', sans-serif !important;
  font-weight: 600 !important;
}

.el-dialog__headerbtn .el-dialog__close {
  color: white !important;
}

.el-pagination .el-pager li.is-active {
  background: var(--gradient-btn) !important;
}
</style>
