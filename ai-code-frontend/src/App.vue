<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* ===== 全局 CSS 变量 ===== */
:root {
  /* 间距与尺寸 */
  --header-height: 56px;
  --max-content-width: 1200px;
  --card-radius: 12px;
  --btn-primary-radius: 8px;

  /* 浅色主题 */
  --bg-primary: #f0f2f5;
  --bg-secondary: #ffffff;
  --card-bg: rgba(255, 255, 255, 0.85);
  --card-border: rgba(0, 0, 0, 0.06);
  --btn-primary-bg: #1a232e;
  --text-primary: #1a1a2e;
  --text-secondary: #555;
  --text-tertiary: #999;
}

/* 全局基础样式 */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  background: var(--bg-primary);
  min-height: 100vh;
}

/* 磨砂渐变背景层（覆盖整个页面）*/
body::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -2;
  background:
    radial-gradient(ellipse 80% 60% at 0% 20%, rgba(59, 130, 246, 0.08) 0%, transparent 60%),
    radial-gradient(ellipse 60% 50% at 100% 80%, rgba(139, 92, 246, 0.06) 0%, transparent 60%),
    radial-gradient(ellipse 50% 40% at 50% 50%, rgba(16, 185, 129, 0.04) 0%, transparent 50%),
    linear-gradient(180deg, #f8fafc 0%, #f1f5f9 40%, #e8ecf1 100%);
  pointer-events: none;
}

/* 噪点纹理叠加层 */
body::after {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: -1;
  opacity: 0.3;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
  background-repeat: repeat;
  background-size: 200px 200px;
  pointer-events: none;
}

html {
  overflow-x: hidden;
}
</style>
