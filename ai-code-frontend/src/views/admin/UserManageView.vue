<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import * as userApi from '@/api/userController'

// 查询参数
const queryForm = reactive<API.UserQueryRequest>({
  userAccount: undefined,
  userName: undefined,
  userRole: undefined,
  pageNum: 1,
  pageSize: 10,
})

// 用户列表数据
const userList = ref<API.UserVO[]>([])
const total = ref(0)
const loading = ref(false)

// 新增 / 编辑弹窗
const modalVisible = ref(false)
const modalTitle = ref('新增用户')
const editingUser = ref<API.UserVO | null>(null)
const formModel = reactive<{
  userName: string
  userAccount: string
  userAvatar: string
  userProfile: string
  userRole: string
}>({
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
})
const submitLoading = ref(false)

async function fetchUsers() {
  loading.value = true
  try {
    const res = await userApi.listUserVoByPage(queryForm)
    if (res.code === 0 && res.data) {
      userList.value = res.data.records || []
      total.value = Number(res.data.totalRow) || 0
    } else {
      message.error(res.message || '获取用户列表失败')
    }
  } catch {
    message.error('请求异常')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  queryForm.pageNum = 1
  fetchUsers()
}

// 重置搜索
function handleReset() {
  queryForm.userAccount = undefined
  queryForm.userName = undefined
  queryForm.userRole = undefined
  queryForm.pageNum = 1
  fetchUsers()
}

// 分页变化
function handlePageChange(pageNum: number, pageSize: number) {
  queryForm.pageNum = pageNum
  queryForm.pageSize = pageSize
  fetchUsers()
}

// 打开新增弹窗
function openAddModal() {
  modalTitle.value = '新增用户'
  editingUser.value = null
  formModel.userName = ''
  formModel.userAccount = ''
  formModel.userAvatar = ''
  formModel.userProfile = ''
  formModel.userRole = 'user'
  modalVisible.value = true
}

// 打开编辑弹窗
function openEditModal(record: API.UserVO) {
  modalTitle.value = '编辑用户'
  editingUser.value = record
  formModel.userName = record.userName || ''
  formModel.userAccount = record.userAccount || ''
  formModel.userAvatar = record.userAvatar || ''
  formModel.userProfile = record.userProfile || ''
  formModel.userRole = record.userRole || 'user'
  modalVisible.value = true
}

// 提交表单
async function handleSubmit() {
  if (!formModel.userAccount) {
    message.warning('请输入账号')
    return
  }
  if (!formModel.userName) {
    message.warning('请输入用户昵称')
    return
  }
  submitLoading.value = true
  try {
    if (editingUser.value) {
      // 编辑
      const res = await userApi.updateUser({
        id: editingUser.value.id,
        userName: formModel.userName,
        userAvatar: formModel.userAvatar || undefined,
        userProfile: formModel.userProfile || undefined,
        userRole: formModel.userRole,
      })
      if (res.code === 0) {
        message.success('更新成功')
        modalVisible.value = false
        fetchUsers()
      } else {
        message.error(res.message || '更新失败')
      }
    } else {
      // 新增
      const res = await userApi.addUser({
        userName: formModel.userName,
        userAccount: formModel.userAccount,
        userAvatar: formModel.userAvatar || undefined,
        userProfile: formModel.userProfile || undefined,
        userRole: formModel.userRole,
      })
      if (res.code === 0) {
        message.success('新增成功')
        modalVisible.value = false
        fetchUsers()
      } else {
        message.error(res.message || '新增失败')
      }
    }
  } catch {
    message.error('提交异常')
  } finally {
    submitLoading.value = false
  }
}

// 删除用户
function handleDelete(record: API.UserVO) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户「${record.userName}」(ID: ${record.id}) 吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const res = await userApi.deleteUser({ id: String(record.id) })
        if (res.code === 0) {
          message.success('删除成功')
          fetchUsers()
        } else {
          message.error(res.message || '删除失败')
        }
      } catch {
        message.error('删除异常')
      }
    },
  })
}

// 角色配置映射
const ROLE_MAP: Record<string, { label: string; color: string }> = {
  user: { label: '普通用户', color: 'blue' },
  admin: { label: '管理员', color: 'red' },
  ban: { label: '封禁', color: 'default' },
}
function roleLabel(role?: string) {
  return (role && ROLE_MAP[role]?.label) || role || '未知'
}
function roleColor(role?: string) {
  return (role && ROLE_MAP[role]?.color) || 'default'
}

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 120 },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount', width: 150 },
  { title: '昵称', dataIndex: 'userName', key: 'userName', width: 150 },
  { title: '简介', dataIndex: 'userProfile', key: 'userProfile', ellipsis: true },
  {
    title: '角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 100,
  },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const },
]

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-manage">
    <h2 class="page-title">用户管理</h2>

    <!-- 搜索表单 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="账号">
          <a-input
            v-model:value="queryForm.userAccount"
            placeholder="按账号搜索"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input
            v-model:value="queryForm.userName"
            placeholder="按昵称搜索"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="queryForm.userRole"
            placeholder="全部"
            allow-clear
            style="width: 140px"
          >
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="ban">封禁</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作栏 -->
    <div class="action-bar">
      <a-button type="primary" @click="openAddModal">新增用户</a-button>
    </div>

    <!-- 用户表格 -->
    <a-card :bordered="false">
      <a-table
        :dataSource="userList"
        :columns="columns"
        :loading="loading"
        :pagination="{
          current: queryForm.pageNum,
          pageSize: queryForm.pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (total: number) => `共 ${total} 条`,
          onChange: handlePageChange,
        }"
        rowKey="id"
        :scroll="{ x: 1000 }"
      >
        <!-- 角色列 -->
        <template #bodyCell="{ column, record }: { column: any; record: API.UserVO }">
          <template v-if="column.dataIndex === 'userRole'">
            <a-tag :color="roleColor(record.userRole)">
              {{ roleLabel(record.userRole) }}
            </a-tag>
          </template>

          <!-- 操作列 -->
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEditModal(record)">编辑</a-button>
              <a-button type="link" danger size="small" @click="handleDelete(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增 / 编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      :confirm-loading="submitLoading"
      @ok="handleSubmit"
      @cancel="modalVisible = false"
      ok-text="确认"
      cancel-text="取消"
    >
      <a-form layout="vertical" :model="formModel">
        <a-form-item label="账号" required>
          <a-input
            v-model:value="formModel.userAccount"
            placeholder="请输入账号"
            :disabled="!!editingUser"
            :maxlength="32"
          />
        </a-form-item>
        <a-form-item label="昵称" required>
          <a-input
            v-model:value="formModel.userName"
            placeholder="请输入用户昵称"
            :maxlength="32"
          />
        </a-form-item>
        <a-form-item label="头像地址">
          <a-input
            v-model:value="formModel.userAvatar"
            placeholder="可选，输入头像 URL"
          />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea
            v-model:value="formModel.userProfile"
            placeholder="可选，输入用户简介"
            :rows="3"
            :maxlength="255"
          />
        </a-form-item>
        <a-form-item label="角色" required>
          <a-select v-model:value="formModel.userRole">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
            <a-select-option value="ban">封禁</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-manage {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1d1d1d;
}

.search-card {
  margin-bottom: 16px;
}

.action-bar {
  margin-bottom: 16px;
}
</style>
