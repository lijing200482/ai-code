<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import GlobalHeader from '@/components/layout/GlobalHeader.vue'
import GlobalFooter from '@/components/layout/GlobalFooter.vue'

const router = useRouter()
const userStore = useUserStore()

// 页面加载时自动恢复登录状态（Pinia 内存状态在刷新后丢失）
onMounted(() => {
  userStore.fetchLoginUser()
})

const selectedKeys = ref<string[]>([router.currentRoute.value.path || '/'])

function updateSelected(path: string) {
  selectedKeys.value = [path]
}

router.afterEach((to) => {
  updateSelected(to.path)
})
</script>

<template>
  <a-layout class="layout">
    <GlobalHeader :selected-key="selectedKeys[0]" />
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
