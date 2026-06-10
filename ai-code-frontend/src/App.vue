<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
:root {
  --bg-primary: #F6F7F2;
  --text-primary: #111925;
  --text-secondary: rgba(17, 25, 37, 0.65);
  --text-tertiary: rgba(17, 25, 37, 0.30);
  --card-bg: #ffffff;
  --card-border: #EBECF0;
  --card-radius: 10px;
  --btn-primary-bg: #1A2332;
  --btn-primary-radius: 8px;
  --header-height: 80px;
  --max-content-width: 900px;
  --hover-tint: rgba(17, 25, 37, 0.03);
}

body {
  margin: 0;
  padding: 0;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', 'Segoe UI', Roboto, sans-serif;
}
</style>
