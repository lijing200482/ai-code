<script setup lang="ts">
import { useRouter } from 'vue-router'

defineProps<{
  app: API.AppVO
}>()

const router = useRouter()

function goToChat(app: API.AppVO) {
  if (app.id) router.push(`/app/chat/${app.id}`)
}

function viewDeployed(app: API.AppVO) {
  if (app.deployKey) window.open(`http://localhost/${app.deployKey}`, '_blank')
}
</script>

<template>
  <div class="app-card" @click="goToChat(app)">
    <div class="app-cover" v-if="app.cover">
      <img :src="app.cover" :alt="app.appName" />
    </div>
    <div class="app-cover-placeholder" v-else>
      <span class="cover-icon">{{ (app.appName || 'A').charAt(0).toUpperCase() }}</span>
    </div>
    <div class="app-info">
      <div class="app-name" :title="app.appName">{{ app.appName || '未命名应用' }}</div>
      <div class="app-prompt" :title="app.initPrompt">{{ app.initPrompt || '暂无描述' }}</div>
      <div class="app-meta">
        <span class="app-time">{{ app.createTime ? new Date(app.createTime).toLocaleDateString() : '' }}</span>
        <span v-if="app.deployKey" class="deployed-badge">已部署</span>
      </div>
    </div>
    <div class="app-actions">
      <a-button type="link" size="small" @click.stop="goToChat(app)">查看对话</a-button>
      <a-button v-if="app.deployKey" type="link" size="small" @click.stop="viewDeployed(app)">查看作品</a-button>
    </div>
  </div>
</template>

<style scoped>
.app-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: var(--card-radius);
  transition: box-shadow 0.2s, background 0.2s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.app-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.app-cover {
  width: 100%;
  height: 120px;
  overflow: hidden;
  background: #f0f0f0;
}
.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-cover-placeholder {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8e9e5 0%, #f0f1ed 100%);
}
.cover-icon {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-tertiary);
}

.app-info {
  padding: 12px 14px 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.app-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.app-prompt {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.app-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: var(--text-tertiary);
}
.deployed-badge {
  color: #52c41a;
  font-size: 11px;
}
.app-actions {
  display: flex;
  gap: 4px;
  padding: 4px 10px 10px;
}
</style>
