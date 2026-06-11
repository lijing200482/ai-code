<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import * as chatHistoryApi from '@/api/chatHistoryController'

// 查询参数
const queryForm = reactive<API.ChatHistoryQueryRequest>({
  message: undefined,
  messageType: undefined,
  appId: undefined,
  userId: undefined,
  pageNum: 1,
  pageSize: 10,
})

const chatList = ref<API.ChatHistory[]>([])
const total = ref(0)
const loading = ref(false)

async function fetchChatHistories() {
  loading.value = true
  try {
    const res = await chatHistoryApi.listAllChatHistoryByPageForAdmin(queryForm)
    if (res.code === 0 && res.data) {
      chatList.value = res.data.records || []
      total.value = Number(res.data.totalRow) || 0
    } else {
      message.error(res.message || '获取对话列表失败')
    }
  } catch {
    message.error('请求异常')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.pageNum = 1
  fetchChatHistories()
}

function handleReset() {
  queryForm.message = undefined
  queryForm.messageType = undefined
  queryForm.appId = undefined
  queryForm.userId = undefined
  queryForm.pageNum = 1
  fetchChatHistories()
}

function handlePageChange(pageNum: number, pageSize: number) {
  queryForm.pageNum = pageNum
  queryForm.pageSize = pageSize
  fetchChatHistories()
}

// 消息类型映射
const MESSAGE_TYPE_MAP: Record<string, { label: string; color: string }> = {
  user: { label: '用户', color: 'blue' },
  ai: { label: 'AI', color: 'green' },
}
function messageTypeLabel(type?: string) {
  return (type && MESSAGE_TYPE_MAP[type]?.label) || type || '未知'
}
function messageTypeColor(type?: string) {
  return (type && MESSAGE_TYPE_MAP[type]?.color) || 'default'
}

function viewAppChat(record: API.ChatHistory) {
  if (record.appId) {
    window.open(`/app/chat/${record.appId}`, '_blank')
  }
}

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  {
    title: '消息内容',
    dataIndex: 'message',
    key: 'message',
    ellipsis: true,
    width: 300,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    key: 'messageType',
    width: 100,
  },
  { title: '应用 ID', dataIndex: 'appId', key: 'appId', width: 100 },
  { title: '用户 ID', dataIndex: 'userId', key: 'userId', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const },
]

onMounted(() => {
  fetchChatHistories()
})
</script>

<template>
  <div class="chat-manage">
    <h2 class="page-title">对话管理</h2>

    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="消息内容">
          <a-input
            v-model:value="queryForm.message"
            placeholder="按消息内容搜索"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="消息类型">
          <a-select
            v-model:value="queryForm.messageType"
            placeholder="全部"
            allow-clear
            style="width: 120px"
          >
            <a-select-option value="user">用户</a-select-option>
            <a-select-option value="ai">AI</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="应用 ID">
          <a-input-number
            v-model:value="queryForm.appId"
            placeholder="应用 ID"
            :min="1"
            style="width: 130px"
          />
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input-number
            v-model:value="queryForm.userId"
            placeholder="用户 ID"
            :min="1"
            style="width: 130px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 对话表格 -->
    <a-card :bordered="false">
      <a-table
        :dataSource="chatList"
        :columns="columns"
        :loading="loading"
        :pagination="{
          current: queryForm.pageNum,
          pageSize: queryForm.pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`,
          onChange: handlePageChange,
        }"
        rowKey="id"
        :scroll="{ x: 1100 }"
      >
        <template #bodyCell="{ column, record }: { column: any; record: API.ChatHistory }">
          <template v-if="column.dataIndex === 'messageType'">
            <a-tag :color="messageTypeColor(record.messageType)">
              {{ messageTypeLabel(record.messageType) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewAppChat(record)">
                查看对话
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<style scoped>
.chat-manage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--text-primary);
}
.search-card {
  margin-bottom: 16px;
}
</style>
