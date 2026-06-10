<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'
import * as appApi from '@/api/appController'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appId = route.params.appId as string
const isAdmin = userStore.isAdmin

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()

const formData = reactive<{
  appName: string
  cover: string
  priority: number | undefined
}>({
  appName: '',
  cover: '',
  priority: undefined,
})

async function fetchApp() {
  loading.value = true
  try {
    const res = isAdmin
      ? await appApi.getAppVoByIdByAdmin({ id: appId })
      : await appApi.getAppVoById({ id: appId })
    if (res.code === 0 && res.data) {
      formData.appName = res.data.appName || ''
      formData.cover = res.data.cover || ''
      formData.priority = res.data.priority
    } else {
      message.error('获取应用信息失败')
    }
  } catch {
    message.error('请求异常')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!formData.appName.trim()) {
    message.warning('请输入应用名称')
    return
  }
  submitting.value = true
  try {
    let res
    if (isAdmin) {
      res = await appApi.updateAppByAdmin({
        id: appId,
        appName: formData.appName,
        cover: formData.cover || undefined,
        priority: formData.priority,
      })
    } else {
      res = await appApi.updateApp({
        id: appId,
        appName: formData.appName,
      })
    }
    if (res.code === 0) {
      message.success('保存成功')
      router.back()
    } else {
      message.error(res.message || '保存失败')
    }
  } catch {
    message.error('保存异常')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchApp()
})
</script>

<template>
  <div class="edit-page">
    <div class="edit-card">
      <h2 class="page-title">编辑应用</h2>

      <a-spin :spinning="loading">
        <a-form layout="vertical" :model="formData" ref="formRef">
          <a-form-item label="应用名称" required>
            <a-input
              v-model:value="formData.appName"
              placeholder="请输入应用名称"
              :maxlength="50"
            />
          </a-form-item>

          <a-form-item label="应用封面" v-if="isAdmin">
            <a-input
              v-model:value="formData.cover"
              placeholder="可选，输入封面图片 URL"
            />
          </a-form-item>

          <a-form-item label="优先级" v-if="isAdmin">
            <a-input-number
              v-model:value="formData.priority"
              :min="0"
              :max="999"
              placeholder="数值越大优先级越高"
              style="width: 200px"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" :loading="submitting" @click="handleSave">
                保存
              </a-button>
              <a-button @click="router.back()">取消</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-spin>
    </div>
  </div>
</template>

<style scoped>
.edit-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 40px 20px;
}
.edit-card {
  background: var(--card-bg);
  border: 1px solid var(--card-border);
  border-radius: var(--card-radius);
  padding: 32px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
  color: var(--text-primary);
}
</style>
