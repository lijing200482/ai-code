<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

defineProps<{
  selectedKey: string
}>()

const router = useRouter()
const userStore = useUserStore()

function handleMenuClick(e: { key: string }) {
  if (e.key === 'my-apps') {
    router.push('/')
  } else {
    router.push(e.key)
  }
}

function handleLogin() {
  router.push('/user/login')
}

function handleLogout() {
  userStore.logout()
  router.push('/user/login')
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
        mode="horizontal"
        :selected-keys="[selectedKey]"
        class="main-menu"
      >
        <a-menu-item key="/" @click="handleMenuClick({ key: '/' })">首页</a-menu-item>
        <a-menu-item v-if="userStore.isLoggedIn" key="my-apps" @click="handleMenuClick({ key: 'my-apps' })">我的应用</a-menu-item>
        <a-menu-item v-if="userStore.isAdmin" key="/admin/user" @click="handleMenuClick({ key: '/admin/user' })">用户管理</a-menu-item>
        <a-menu-item v-if="userStore.isAdmin" key="/admin/app" @click="handleMenuClick({ key: '/admin/app' })">应用管理</a-menu-item>
        <a-menu-item v-if="userStore.isAdmin" key="/admin/chat" @click="handleMenuClick({ key: '/admin/chat' })">对话管理</a-menu-item>
      </a-menu>
    </div>

    <div class="user-wrapper">
      <template v-if="userStore.isLoggedIn">
        <a-dropdown>
          <a-button type="text" class="user-btn">
            {{ userStore.userName || userStore.loginUser?.userAccount }}
          </a-button>
          <template #overlay>
            <a-menu>
              <a-menu-item key="logout" @click="handleLogout">
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </template>
      <template v-else>
        <a-button type="primary" @click="handleLogin">登录</a-button>
      </template>
    </div>
  </a-layout-header>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  height: var(--header-height);
  padding: 0 24px;
  background: var(--bg-primary, #F6F7F2);
  border-bottom: 1px solid var(--card-border);
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
