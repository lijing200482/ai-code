<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活的 Tab：login / register
const activeTab = ref<'login' | 'register'>('login')

// 登录表单
const loginForm = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})
const loginLoading = ref(false)

// 注册表单
const registerForm = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
const registerLoading = ref(false)

async function handleLogin() {
  if (!loginForm.userAccount || loginForm.userAccount.length < 4) {
    message.warning('账号不能少于4位')
    return
  }
  if (!loginForm.userPassword || loginForm.userPassword.length < 8) {
    message.warning('密码不能少于8位')
    return
  }
  loginLoading.value = true
  try {
    const res = await userStore.login(loginForm)
    if (res.code === 0) {
      message.success('登录成功')
      // 跳转到 redirect 参数指定的页面，或首页
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } else {
      message.error(res.message || '登录失败')
    }
  } catch {
    message.error('登录异常，请稍后重试')
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.userAccount || registerForm.userAccount.length < 4) {
    message.warning('账号不能少于4位')
    return
  }
  if (!registerForm.userPassword || registerForm.userPassword.length < 8) {
    message.warning('密码不能少于8位')
    return
  }
  if (registerForm.userPassword !== registerForm.checkPassword) {
    message.warning('两次输入的密码不一致')
    return
  }
  registerLoading.value = true
  try {
    const res = await userStore.register(registerForm)
    if (res.code === 0) {
      message.success('注册成功，请登录')
      // 注册成功后切换到登录 Tab，并填充账号密码
      activeTab.value = 'login'
      loginForm.userAccount = registerForm.userAccount
      loginForm.userPassword = registerForm.userPassword
      registerForm.userAccount = ''
      registerForm.userPassword = ''
      registerForm.checkPassword = ''
    } else {
      message.error(res.message || '注册失败')
    }
  } catch {
    message.error('注册异常，请稍后重试')
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="card-title">欢迎使用 Ai零代码生成平台</h2>

      <a-tabs v-model:activeKey="activeTab" centered>
        <!-- 登录 -->
        <a-tab-pane key="login" tab="登录">
          <a-form layout="vertical" :model="loginForm" @finish="handleLogin">
            <a-form-item label="账号" name="userAccount">
              <a-input
                v-model:value="loginForm.userAccount"
                placeholder="请输入账号（至少4位）"
                :maxlength="32"
              />
            </a-form-item>

            <a-form-item label="密码" name="userPassword">
              <a-input-password
                v-model:value="loginForm.userPassword"
                placeholder="请输入密码（至少8位）"
                :maxlength="32"
              />
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="loginLoading" block>
                登录
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 注册 -->
        <a-tab-pane key="register" tab="注册">
          <a-form layout="vertical" :model="registerForm" @finish="handleRegister">
            <a-form-item label="账号" name="userAccount">
              <a-input
                v-model:value="registerForm.userAccount"
                placeholder="请输入账号（至少4位）"
                :maxlength="32"
              />
            </a-form-item>

            <a-form-item label="密码" name="userPassword">
              <a-input-password
                v-model:value="registerForm.userPassword"
                placeholder="请输入密码（至少8位）"
                :maxlength="32"
              />
            </a-form-item>

            <a-form-item label="确认密码" name="checkPassword">
              <a-input-password
                v-model:value="registerForm.checkPassword"
                placeholder="请再次输入密码"
                :maxlength="32"
              />
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="registerLoading" block>
                注册
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px - 60px);
  background: #f5f5f5;
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 8px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1d1d1d;
}
</style>
