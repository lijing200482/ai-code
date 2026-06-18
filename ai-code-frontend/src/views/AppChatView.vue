<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import axios from 'axios'
import * as appApi from '@/api/appController'
import * as chatHistoryApi from '@/api/chatHistoryController'
import { connectSSE } from '@/utils/sse'
import { useVisualEdit } from '@/utils/visualEdit'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const appId = route.params.appId as string
const userStore = useUserStore()

const appDetail = ref<API.AppVO | null>(null)
const loading = ref(true)
const isOwner = ref(true)

const messages = ref<Array<{ role: 'user' | 'ai'; content: string }>>([])
const currentInput = ref('')
const isGenerating = ref(false)
const chatContainerRef = ref<HTMLElement | null>(null)
const cancelSse = ref<(() => void) | null>(null)

const generatedCode = ref('')
const showPreview = ref(false)
const previewIframe = ref<HTMLIFrameElement | null>(null)
const previewUrl = ref('')

const deployUrl = ref('')
const deployLoading = ref(false)
const downloadLoading = ref(false)

// 可视化编辑
const {
  isEditMode,
  selectedElements,
  editLoading,
  editError,
  toggleEditMode,
  exitEditMode,
  removeElement,
  clearSelection,
  buildElementPrompt,
  onIframeLoad: onVisualEditIframeLoad,
} = useVisualEdit(previewIframe)

/** 是否为 Vue 项目 */
const isVueProject = computed(() => appDetail.value?.codeGenType === 'vue_project')

/** 生成类型标签映射 */
const codeGenTypeLabel = computed(() => {
  const map: Record<string, string> = {
    html: '原生 HTML',
    multi_file: '多文件模式',
    vue_project: 'Vue 工程',
  }
  return appDetail.value?.codeGenType ? (map[appDetail.value.codeGenType] || appDetail.value.codeGenType) : ''
})

/**
 * 将 AI 生成的代码格式化为可读的聊天消息
 */
function formatCodeContent(content: string): string {
  if (!content) return content
  try {
    const parsed = JSON.parse(content)

    // 处理带 htmlCode/cssCode/jsCode 的结构化 JSON
    if (parsed.htmlCode !== undefined || parsed.cssCode !== undefined || parsed.jsCode !== undefined) {
      const parts: string[] = []

      if (parsed.description) {
        parts.push('📝 ' + parsed.description)
        parts.push('')
      }

      if (parsed.htmlCode) {
        parts.push('━━━ index.html ━━━')
        parts.push(parsed.htmlCode)
        parts.push('')
      }
      if (parsed.cssCode) {
        parts.push('━━━ style.css ━━━')
        parts.push(parsed.cssCode)
        parts.push('')
      }
      if (parsed.jsCode) {
        parts.push('━━━ script.js ━━━')
        parts.push(parsed.jsCode)
        parts.push('')
      }

      return parts.join('\n')
    }

    if (typeof parsed === 'object' && parsed !== null) {
      return '```json\n' + JSON.stringify(parsed, null, 2) + '\n```'
    }
  } catch {
    // 不是合法 JSON，原样返回
  }
  return content
}


// 游标分页状态
const loadingHistory = ref(false)
const hasMore = ref(false)
const cursor = ref<string | undefined>(undefined)
const totalChatCount = ref(0)
const initialLoadDone = ref(false)
const historyLoadFailed = ref(false)

async function fetchAppDetail() {
  loading.value = true
  try {
    const res = await appApi.getAppVoById({ id: appId })
    if (res.code === 0 && res.data) {
      appDetail.value = res.data
      // 判断是否为应用所有者
      isOwner.value = !res.data.userId || res.data.userId === userStore.loginUser?.id

      // 加载对话历史
      await loadChatHistory()
      initialLoadDone.value = true

      // 自己的应用且没有对话历史，自动发送初始消息
      if (isOwner.value && !historyLoadFailed.value && messages.value.length === 0 && res.data.initPrompt) {
        messages.value.push({ role: 'user', content: res.data.initPrompt })
        await nextTick()
        startGeneration(res.data.initPrompt)
      } else if (totalChatCount.value >= 2) {
        // 至少有 2 条对话记录，展示已生成的网站
        loadExistingPreview()
      }
    } else {
      message.error('获取应用信息失败')
    }
  } catch {
    message.error('请求异常')
  } finally {
    loading.value = false
  }
}

/**
 * 加载对话历史（游标查询，默认加载最近 10 条，后端按创建时间降序返回）
 * 首次加载时保存总记录数用于判断 hasMore
 * 加载更多时将旧消息插入列表头部（时间升序排列）
 */
async function loadChatHistory() {
  loadingHistory.value = true
  historyLoadFailed.value = false
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId as unknown as number,
      pageSize: 10,
    }
    if (cursor.value) {
      params.lastCreateTime = cursor.value
    }
    const res = await chatHistoryApi.listAppChatHistory(params)
    if (res.code === 0 && res.data) {
      const records = res.data.records || []

      // 首次加载时保存总记录数（优先用实际接收到的数量）
      if (!cursor.value) {
        totalChatCount.value = records.length
      }

      // 格式化为本地消息并反转（DESC → ASC），AI 消息需格式化 JSON
      const historyMessages = records
        .map(r => {
          const role = (r.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai'
          const content = role === 'ai' ? formatCodeContent(r.message || '') : (r.message || '')
          return { role, content }
        })
        .reverse()

      if (cursor.value) {
        // 加载更多：在现有消息之前插入（更早的消息在上方）
        messages.value = [...historyMessages, ...messages.value]
      } else {
        messages.value = historyMessages
      }

      // 更新游标：取本次返回的最后一条（即最早一条）的创建时间
      if (records.length > 0) {
        cursor.value = records[records.length - 1].createTime
      } else {
        cursor.value = undefined
      }

      // 判断是否还有更多（基于首次加载的总数）
      hasMore.value = messages.value.length < totalChatCount.value
    } else {
      historyLoadFailed.value = true
      message.error(res.message || '获取对话历史失败')
    }
  } catch (e) {
    historyLoadFailed.value = true
    message.error('获取对话历史请求异常')
  } finally {
    loadingHistory.value = false
  }
}

function loadMore() {
  if (!loadingHistory.value && hasMore.value) {
    loadChatHistory()
  }
}

// 加载已有作品预览
function loadExistingPreview() {
  // Vue 项目已部署：直接展示部署链接
  if (isVueProject.value && appDetail.value?.deployKey) {
    // 重新构造部署 URL（与 handleDeploy 一致）
    deployUrl.value = `http://localhost/${appDetail.value.deployKey}`
    previewUrl.value = `/deploy-preview/${appDetail.value.deployKey}`
    showPreview.value = true
    nextTick(() => { updatePreview() })
    return
  }
  if (isVueProject.value) {
    // Vue 项目未部署，不显示 iframe 预览
    showPreview.value = false
    return
  }
  const codeGenType = appDetail.value?.codeGenType || 'multi_file'
  previewUrl.value = `/api/static/${codeGenType}_${appId}/`
  showPreview.value = true
  nextTick(() => { updatePreview() })
}

function startGeneration(messageText: string) {
  if (isGenerating.value) return
  isGenerating.value = true
  showPreview.value = false
  generatedCode.value = ''

  messages.value.push({ role: 'ai', content: '' })
  const aiMsgIndex = messages.value.length - 1
  scrollToBottom()

  cancelSse.value = connectSSE(
    appId,
    messageText,
    {
      onMessage: (data) => {
        // 流式阶段只收集代码，不直接显示原始 JSON 片段
        const parsed = JSON.parse(data)
        if (parsed.d !== undefined) {
          const chunk = typeof parsed.d === 'string' ? parsed.d : String(parsed.d)
          generatedCode.value += chunk
        }
      },
      onError: () => {
        message.error('生成连接中断，请重试')
        isGenerating.value = false
      },
      onComplete: () => {
        isGenerating.value = false
        const formatted = formatCodeContent(generatedCode.value)
        if (isVueProject.value) {
          // Vue 项目无法直接静态预览，需要构建部署
          messages.value[aiMsgIndex].content = '✅ Vue 项目生成完成！\n\n' + (formatted !== generatedCode.value ? formatted : generatedCode.value) + '\n\n---\n点击「部署」按钮构建并查看效果'
          showPreview.value = false
        } else {
          showPreview.value = true
          const codeGenType = appDetail.value?.codeGenType || 'multi_file'
          previewUrl.value = `/api/static/${codeGenType}_${appId}/`
          nextTick(() => { updatePreview() })
          // 始终展示格式化后的内容，避免流式阶段污染的原始 JSON
          messages.value[aiMsgIndex].content = '✅ 代码生成完成！\n\n' + formatted
        }
        scrollToBottom()
      },
    },
  )
}

function updatePreview() {
  if (previewIframe.value) {
    previewIframe.value.src = previewUrl.value
  }
}

function handleSend() {
  if (!currentInput.value.trim() || isGenerating.value) return
  const baseText = currentInput.value.trim()
  const elementPrompt = buildElementPrompt()
  const fullText = baseText + elementPrompt
  messages.value.push({ role: 'user', content: fullText })
  currentInput.value = ''
  // 发送后清除选中元素并退出编辑模式
  clearSelection()
  exitEditMode()
  startGeneration(fullText)
}

async function handleDeploy() {
  deployLoading.value = true
  try {
    const res = await appApi.deployApp({ appId })
    if (res.code === 0 && res.data) {
      deployUrl.value = res.data
      // Vue 项目部署后在预览 iframe 中展示（使用代理路径确保同域）
      if (isVueProject.value) {
        showPreview.value = true
        // res.data 格式为 http://localhost/deployKey，转换为代理路径
        const deployKey = res.data.split('/').pop()
        previewUrl.value = `/deploy-preview/${deployKey}`
        nextTick(() => { updatePreview() })
      }
      message.success('部署成功')
    } else {
      message.error(res.message || '部署失败')
    }
  } catch {
    message.error('部署请求异常')
  } finally {
    deployLoading.value = false
  }
}

async function handleDownload() {
  downloadLoading.value = true
  try {
    const response = await axios.get(`http://localhost:8445/api/app/download/${appId}`, {
      responseType: 'blob',
      withCredentials: true,
    })
    const contentDisposition = response.headers['content-disposition'] as string | undefined
    let fileName = `${appId}.zip`
    if (contentDisposition) {
      const match = contentDisposition.match(/filename[^;=\n]*=["']?([^"';\n]*)["']?/i)
      if (match && match[1]) {
        fileName = match[1]
      }
    }
    const url = window.URL.createObjectURL(response.data)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    message.success('下载成功')
  } catch {
    message.error('下载失败')
  } finally {
    downloadLoading.value = false
  }
}

function handleEdit() {
  router.push('/app/edit/' + appId)
}

function scrollToBottom() {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

onMounted(() => { fetchAppDetail() })
onUnmounted(() => { if (cancelSse.value) cancelSse.value() })
</script>

<template>
  <div class="chat-page">
    <div class="top-bar">
      <div class="top-left">
        <a-button type="text" @click="router.push('/')" class="back-btn">← 返回</a-button>
        <h2 class="app-title">{{ appDetail?.appName || '加载中...' }}</h2>
        <a-tag v-if="codeGenTypeLabel" class="type-tag">{{ codeGenTypeLabel }}</a-tag>
      </div>
      <div class="top-actions">
        <a-button @click="handleEdit" v-if="appDetail">编辑</a-button>
        <a-button :loading="downloadLoading" @click="handleDownload">下载代码</a-button>
        <a-button type="primary" :loading="deployLoading" @click="handleDeploy" :disabled="!showPreview && !isVueProject">部署</a-button>
      </div>
    </div>

    <div class="main-body" :class="{ 'has-preview': showPreview }">
      <div class="chat-panel">
        <div class="chat-body">
          <div v-if="loading" class="chat-loading">
            <a-spin tip="加载中..." />
          </div>
          <div class="messages" ref="chatContainerRef" v-show="!loading">
            <div v-if="messages.length === 0 && !loading" class="empty-chat">
              <p>输入描述开始生成网页应用</p>
            </div>
            <div v-if="hasMore" class="load-more-bar">
              <a-button type="link" :loading="loadingHistory" @click="loadMore" class="load-more-btn">
                加载更多对话...
              </a-button>
            </div>
            <div v-for="(msg, i) in messages" :key="i" class="message-row" :class="msg.role">
              <div class="message-bubble">
                <div class="message-label">{{ msg.role === 'user' ? '你' : 'AI' }}</div>
                <pre class="message-content">{{ msg.content || (isGenerating && i === messages.length - 1 ? '...' : '') }}</pre>
              </div>
            </div>
            <div v-if="isGenerating" class="generating-indicator">AI 正在生成...</div>
          </div>
        </div>
        <div class="input-area">
          <!-- 编辑模式错误提示 -->
          <a-alert v-if="editError" type="warning" closable @close="editError = ''" class="edit-error-alert" :message="editError" />
          <!-- 选中元素提示 -->
          <div v-if="selectedElements.length > 0" class="selected-elements">
            <a-alert
              v-for="(el, i) in selectedElements"
              :key="el.cssSelector"
              type="info"
              closable
              @close="removeElement(i)"
              class="element-alert"
            >
              <template #message>
                <span class="element-tag">&lt;{{ el.tagName }}&gt;</span>
                <span class="element-selector">{{ el.cssSelector }}</span>
                <span v-if="el.textContent" class="element-text">{{ el.textContent }}</span>
              </template>
            </a-alert>
          </div>
          <div class="input-area-bottom">
            <a-button
              :type="isEditMode ? 'primary' : 'default'"
              :class="{ 'edit-active': isEditMode }"
              :disabled="isGenerating || loading || !isOwner || !showPreview"
              :loading="editLoading"
              @click="toggleEditMode(previewUrl)"
              class="edit-mode-btn"
            >
              {{ editLoading ? '加载中...' : (isEditMode ? '退出编辑' : '可视化编辑') }}
            </a-button>
            <a-tooltip :title="isOwner ? '' : '无法在别人的作品下对话哦~'">
              <a-textarea
                v-model:value="currentInput"
                :placeholder="isOwner ? '输入修改要求，按 Enter 发送...' : '无法在别人的作品下对话哦~'"
                :disabled="isGenerating || loading || !isOwner"
                :rows="2"
                @keydown="handleKeydown"
                class="chat-input"
              />
            </a-tooltip>
            <a-button type="primary" :disabled="isGenerating || loading || !currentInput.trim() || !isOwner" @click="handleSend" class="send-btn">发送</a-button>
          </div>
        </div>
      </div>

      <div class="preview-panel" v-if="showPreview">
        <div class="preview-header">
          <span class="preview-label">预览</span>
          <a-button type="link" size="small" v-if="deployUrl" :href="deployUrl" target="_blank">打开部署的应用 ↗</a-button>
        </div>
        <div class="preview-container">
          <iframe ref="previewIframe" class="preview-iframe" title="代码预览" @load="onVisualEditIframeLoad" />
        </div>
      </div>
      <div class="preview-placeholder" v-else-if="!loading">
        <div class="placeholder-content">
          <div class="placeholder-icon">🖥️</div>
          <p v-if="isVueProject && totalChatCount > 0">Vue 项目需部署后才能预览</p>
          <p v-else-if="initialLoadDone && totalChatCount === 0">输入描述开始生成网页应用</p>
          <p v-else>生成完成后，页面效果将展示在这里</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-page { height: calc(100vh - var(--header-height)); display: flex; flex-direction: column; background: var(--bg-primary); }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 12px 20px; background: var(--card-bg); border-bottom: 1px solid var(--card-border); flex-shrink: 0; }
.top-left { display: flex; align-items: center; gap: 12px; }
.type-tag { font-size: 12px; }
.back-btn { font-size: 14px; color: var(--text-secondary); }
.app-title { font-size: 16px; font-weight: 600; margin: 0; color: var(--text-primary); }
.top-actions { display: flex; gap: 8px; }
.main-body { display: flex; flex: 1; overflow: hidden; }
.chat-panel { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.has-preview .chat-panel { max-width: 50%; border-right: 1px solid var(--card-border); }
.chat-body { flex: 1; display: flex; flex-direction: column; min-height: 0; position: relative; }
.chat-loading { flex: 1; display: flex; align-items: center; justify-content: center; }
.messages { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.load-more-bar { text-align: center; padding: 4px 0; }
.load-more-btn { font-size: 13px; }
.message-row { display: flex; }
.message-row.user { justify-content: flex-end; }
.message-row.ai { justify-content: flex-start; }
.message-bubble { max-width: 95%; padding: 10px 14px; border-radius: 10px; background: var(--card-bg); border: 1px solid var(--card-border); overflow: hidden; }
.message-row.user .message-bubble { background: var(--btn-primary-bg); color: #fff; border-color: var(--btn-primary-bg); max-width: 80%; }
.message-label { font-size: 11px; font-weight: 600; margin-bottom: 4px; opacity: 0.7; }
.message-content { font-size: 13px; line-height: 1.5; white-space: pre-wrap; word-break: break-word; margin: 0; font-family: inherit; overflow-x: auto; max-height: 60vh; overflow-y: auto; }
.generating-indicator { padding: 8px 20px; font-size: 13px; color: var(--text-secondary); }
.input-area { display: flex; flex-direction: column; gap: 6px; padding: 10px 20px 12px; background: var(--card-bg); border-top: 1px solid var(--card-border); }
.selected-elements { display: flex; flex-wrap: wrap; gap: 6px; }
.edit-error-alert { font-size: 13px; }
.element-alert { padding: 2px 8px; font-size: 12px; border-radius: 6px; }
.element-alert :deep(.ant-alert-message) { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.element-tag { font-family: monospace; font-weight: 600; color: #1890ff; background: #e6f7ff; padding: 1px 6px; border-radius: 4px; font-size: 11px; }
.element-selector { font-family: monospace; color: var(--text-secondary); font-size: 11px; }
.element-text { color: var(--text-tertiary); font-size: 11px; max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.input-area-bottom { display: flex; gap: 8px; align-items: flex-end; }
.chat-input { flex: 1; border-radius: 8px; font-size: 14px; }
.edit-mode-btn { flex-shrink: 0; align-self: flex-end; font-size: 13px; }
.edit-active { border-color: #e8471c; color: #e8471c; }
.send-btn { align-self: flex-end; border-radius: var(--btn-primary-radius); }
.preview-panel { flex: 1; display: flex; flex-direction: column; background: #fff; }
.preview-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 16px; border-bottom: 1px solid var(--card-border); font-size: 13px; font-weight: 600; color: var(--text-secondary); }
.preview-container { flex: 1; overflow-y: auto; background: #fff; }
.preview-iframe { width: 100%; height: 100%; min-height: 500px; border: none; display: block; }
.preview-placeholder { flex: 1; display: flex; align-items: center; justify-content: center; background: var(--card-bg); }
.placeholder-content { text-align: center; color: var(--text-tertiary); }
.placeholder-icon { font-size: 48px; margin-bottom: 12px; }
.empty-chat { text-align: center; color: var(--text-tertiary); padding: 40px; }
</style>
