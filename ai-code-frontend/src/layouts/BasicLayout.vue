<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()

// 菜单项配置 — 可通过此配置增删菜单
const menuItems: MenuProps['items'] = [
  {
    key: '/',
    label: '首页',
  },
  {
    key: '/about',
    label: '关于',
  },
]

const selectedKeys = ref<string[]>(['/'])

// 监听路由变化，同步菜单选中状态
const routePath = '/'
function updateSelected(path: string) {
  selectedKeys.value = [path]
}

router.afterEach((to) => {
  updateSelected(to.path)
})

function handleMenuClick(e: { key: string }) {
  // 路由跳转由 RouterView + 路由匹配处理
}
</script>

<template>
  <a-layout class="layout">
    <GlobalHeader :menu-items="menuItems" :selected-key="routePath" />
    <a-layout-content>
      <RouterView />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
</style>
