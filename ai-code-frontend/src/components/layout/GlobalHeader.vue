<script setup lang="ts">
import type { MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'

defineProps<{
  menuItems?: MenuProps['items']
  selectedKey: string
}>()

const emit = defineEmits<{
  'menu-click': [e: { key: string }]
}>()

const router = useRouter()

function handleMenuClick(e: { key: string }) {
  router.push(e.key)
  emit('menu-click', e)
}
</script>

<template>
  <a-layout-header class="header">
    <div class="logo-wrapper">
      <img src="@/assets/logo.svg" alt="logo" class="logo-img" />
      <h1 class="site-title">Ai零代码生成平台</h1>
    </div>

    <div class="menu-wrapper">
      <a-menu
        v-if="menuItems?.length"
        mode="horizontal"
        :selected-keys="[selectedKey]"
        :items="menuItems"
        @click="handleMenuClick"
        class="main-menu"
      />
    </div>

    <div class="user-wrapper">
      <a-button type="primary" @click="handleLogin">登录</a-button>
    </div>
  </a-layout-header>
</template>

<script lang="ts">
export default {
  methods: {
    handleLogin() {
      console.log('登录按钮点击')
    },
  },
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  z-index: 10;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1d;
  white-space: nowrap;
}

.menu-wrapper {
  flex: 1;
  margin: 0 24px;
  overflow: hidden;
}

.main-menu {
  border-bottom: none !important;
  background: transparent !important;
}

.user-wrapper {
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .header {
    padding: 0 12px;
  }

  .menu-wrapper {
    margin: 0 8px;
  }

  .site-title {
    font-size: 15px;
  }
}
</style>
