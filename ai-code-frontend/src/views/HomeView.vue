<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import * as appApi from '@/api/appController'
import AppCard from '@/components/app/AppCard.vue'

const router = useRouter()
const userStore = useUserStore()

// 提示词输入
const initPrompt = ref('')
const creating = ref(false)

// 我的应用
const myApps = ref<API.AppVO[]>([])
const myAppsLoading = ref(false)
const myAppsTotal = ref(0)
const myAppsPage = reactive({ pageNum: 1, pageSize: 8 })

// 精选应用
const featuredApps = ref<API.AppVO[]>([])
const featuredLoading = ref(false)
const featuredTotal = ref(0)
const featuredPage = reactive({ pageNum: 1, pageSize: 8 })

// 预设提示词示例
const promptSuggestions = [
  '生成一个登录页面',
  '生成一个个人博客',
  '生成一个任务管理面板',
  '生成一个在线简历',
  '生成一个天气预报卡片',
]

async function fetchMyApps() {
  if (!userStore.isLoggedIn) return
  myAppsLoading.value = true
  try {
    const res = await appApi.listMyAppVoByPage({
      pageNum: myAppsPage.pageNum,
      pageSize: myAppsPage.pageSize,
    })
    if (res.code === 0 && res.data) {
      myApps.value = res.data.records || []
      myAppsTotal.value = res.data.totalRow || 0
    }
  } catch {
    // ignore
  } finally {
    myAppsLoading.value = false
  }
}

async function fetchFeaturedApps() {
  featuredLoading.value = true
  try {
    const res = await appApi.listGoodAppVoByPage({
      pageNum: featuredPage.pageNum,
      pageSize: featuredPage.pageSize,
    })
    if (res.code === 0 && res.data) {
      featuredApps.value = res.data.records || []
      featuredTotal.value = res.data.totalRow || 0
    }
  } catch {
    // ignore
  } finally {
    featuredLoading.value = false
  }
}

async function handleCreate() {
  if (!initPrompt.value.trim()) {
    message.warning('请输入想要生成的应用描述')
    return
  }
  if (!userStore.isLoggedIn) {
    router.push('/user/login')
    return
  }
  creating.value = true
  try {
    const res = await appApi.addApp({ initPrompt: initPrompt.value.trim() })
    if (res.code === 0 && res.data) {
      message.success('应用创建成功')
      router.push(`/app/chat/${res.data}`)
    } else {
      message.error(res.message || '创建失败')
    }
  } catch {
    message.error('创建异常')
  } finally {
    creating.value = false
  }
}

function useSuggestion(text: string) {
  initPrompt.value = text
}

onMounted(() => {
  fetchFeaturedApps()
  if (userStore.isLoggedIn) {
    fetchMyApps()
  }
})
</script>

<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <h1 class="hero-title">小景的AI应用生成</h1>
      <p class="hero-subtitle">输入描述，AI 自动生成网页应用</p>

      <div class="input-area">
        <a-textarea
          v-model:value="initPrompt"
          placeholder="描述你想要生成的网页应用，例如：生成一个美观的登录页面..."
          :rows="3"
          :maxlength="500"
          class="prompt-input"
        />
        <a-button
          type="primary"
          :loading="creating"
          @click="handleCreate"
          class="generate-btn"
        >
          开始生成
        </a-button>
      </div>

      <div class="suggestions">
        <span
          v-for="(text, i) in promptSuggestions"
          :key="i"
          class="suggestion-tag"
          @click="useSuggestion(text)"
        >
          {{ text }}
        </span>
      </div>
    </div>

    <!-- 我的应用 -->
    <div class="section" v-if="userStore.isLoggedIn">
      <div class="section-header">
        <h2 class="section-title">我的应用</h2>
        <a-button type="link" v-if="myAppsTotal > myAppsPage.pageSize" @click="fetchMyApps">
          查看更多
        </a-button>
      </div>

      <a-spin :spinning="myAppsLoading">
        <div class="app-grid" v-if="myApps.length > 0">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
          />
        </div>
        <div class="empty-state" v-else-if="!myAppsLoading">
          <p>暂无应用，快去创建一个吧！</p>
        </div>
      </a-spin>
    </div>

    <!-- 精选应用 -->
    <div class="section">
      <div class="section-header">
        <h2 class="section-title">精选应用</h2>
      </div>

      <a-spin :spinning="featuredLoading">
        <div class="app-grid" v-if="featuredApps.length > 0">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
          />
        </div>
        <div class="empty-state" v-else-if="!featuredLoading">
          <p>暂无精选应用</p>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  max-width: var(--max-content-width);
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* Hero 区域 */
.hero-section {
  min-height: 50vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 60px 0 40px;
  text-align: center;
}

.hero-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.hero-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.input-area {
  width: 100%;
  max-width: 680px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.prompt-input {
  border-radius: var(--card-radius);
  border-color: var(--card-border);
  font-size: 15px;
  padding: 12px 16px;
  resize: none;
}
.prompt-input:focus {
  border-color: var(--btn-primary-bg);
  box-shadow: 0 0 0 2px rgba(26, 35, 50, 0.1);
}

.generate-btn {
  align-self: flex-end;
  background: var(--btn-primary-bg);
  border-radius: var(--btn-primary-radius);
  height: 40px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 500;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 16px;
  max-width: 680px;
}

.suggestion-tag {
  background: #FAFAF9;
  border-radius: 8px;
  padding: 6px 14px;
  font-size: 12px;
  color: var(--text-primary);
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}
.suggestion-tag:hover {
  background: #EEEEEE;
}

/* 分区 */
.section {
  margin-top: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: var(--text-tertiary);
  font-size: 14px;
}
</style>

