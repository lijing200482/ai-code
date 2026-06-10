<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import * as appApi from '@/api/appController'

// 查询参数
const queryForm = reactive<API.AppQueryRequest>({
  appName: undefined,
  pageNum: 1,
  pageSize: 10,
})

const appList = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

// 编辑弹窗
const modalVisible = ref(false)
const editingApp = ref<API.AppVO | null>(null)
const editForm = reactive({
  appName: '',
  cover: '',
  priority: 0,
})
const submitLoading = ref(false)

async function fetchApps() {
  loading.value = true
  try {
    const res = await appApi.listAppVoByPageByAdmin(queryForm)
    if (res.code === 0 && res.data) {
      appList.value = res.data.records || []
      total.value = Number(res.data.totalRow) || 0
    } else {
      message.error(res.message || '获取应用列表失败')
    }
  } catch {
    message.error('请求异常')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.pageNum = 1
  fetchApps()
}

function handleReset() {
  queryForm.appName = undefined
  queryForm.pageNum = 1
  fetchApps()
}

function handlePageChange(pageNum: number, pageSize: number) {
  queryForm.pageNum = pageNum
  queryForm.pageSize = pageSize
  fetchApps()
}

function openEditModal(record: API.AppVO) {
  editingApp.value = record
  editForm.appName = record.appName || ''
  editForm.cover = record.cover || ''
  editForm.priority = record.priority || 0
  modalVisible.value = true
}

async function handleEditSubmit() {
  if (!editingApp.value?.id) return
  submitLoading.value = true
  try {
    const res = await appApi.updateAppByAdmin({
      id: editingApp.value.id,
      appName: editForm.appName,
      cover: editForm.cover || undefined,
      priority: editForm.priority,
    })
    if (res.code === 0) {
      message.success('更新成功')
      modalVisible.value = false
      fetchApps()
    } else {
      message.error(res.message || '更新失败')
    }
  } catch {
    message.error('提交异常')
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(record: API.AppVO) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用「${record.appName}」(ID: ${record.id}) 吗？`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const res = await appApi.deleteAppByAdmin({ id: record.id })
        if (res.code === 0) {
          message.success('删除成功')
          fetchApps()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch {
        message.error('删除异常')
      }
    },
  })
}

function handleSetFeatured(record: API.AppVO) {
  Modal.confirm({
    title: '设为精选',
    content: `将「${record.appName}」设为精选应用？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await appApi.updateAppByAdmin({
          id: record.id,
          priority: 99,
        })
        if (res.code === 0) {
          message.success('已设为精选')
          fetchApps()
        } else {
          message.error(res.message || '操作失败')
        }
      } catch {
        message.error('操作异常')
      }
    },
  })
}

function viewDetail(record: API.AppVO) {
  if (record.id) {
    window.open(`/app/chat/${record.id}`, '_blank')
  }
}

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '应用名称', dataIndex: 'appName', key: 'appName', width: 150 },
  { title: '用户', dataIndex: ['user', 'userName'], key: 'userName', width: 100 },
  { title: '提示词', dataIndex: 'initPrompt', key: 'initPrompt', ellipsis: true },
  { title: '优先级', dataIndex: 'priority', key: 'priority', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' as const },
]

onMounted(() => {
  fetchApps()
})
</script>

<template>
  <div class="app-manage">
    <h2 class="page-title">应用管理</h2>

    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="应用名称">
          <a-input
            v-model:value="queryForm.appName"
            placeholder="按名称搜索"
            allow-clear
            style="width: 200px"
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

    <a-card :bordered="false">
      <a-table
        :dataSource="appList"
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
        <template #bodyCell="{ column, record }: { column: any; record: API.AppVO }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewDetail(record)">查看</a-button>
              <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleSetFeatured(record)">设为精选</a-button>
              <a-button type="link" danger size="small" @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      title="编辑应用"
      :confirm-loading="submitLoading"
      @ok="handleEditSubmit"
      @cancel="modalVisible = false"
      ok-text="确认"
      cancel-text="取消"
    >
      <a-form layout="vertical" :model="editForm">
        <a-form-item label="应用名称" required>
          <a-input v-model:value="editForm.appName" :maxlength="50" />
        </a-form-item>
        <a-form-item label="应用封面">
          <a-input v-model:value="editForm.cover" placeholder="封面图片 URL" />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model:value="editForm.priority" :min="0" :max="999" style="width: 200px" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.app-manage {
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
