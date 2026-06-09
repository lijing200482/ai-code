import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import * as userApi from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  // 状态
  const loginUser = ref<API.LoginUserVO | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => loginUser.value !== null)
  const isAdmin = computed(() => loginUser.value?.userRole === 'admin')
  const userName = computed(() => loginUser.value?.userName ?? '')

  // 登录
  async function login(body: API.UserLoginRequest) {
    const res = await userApi.userLogin(body)
    if (res.code === 0 && res.data) {
      loginUser.value = res.data
    }
    return res
  }

  // 注册
  async function register(body: API.UserRegisterRequest) {
    const res = await userApi.userRegister(body)
    return res
  }

  // 获取当前登录用户信息
  let fetchingPromise: Promise<API.LoginUserVO | null> | null = null
  async function fetchLoginUser() {
    // 防止并发重复请求
    if (fetchingPromise) return fetchingPromise
    fetchingPromise = (async () => {
      try {
        const res = await userApi.getLoginUser()
        if (res.code === 0 && res.data) {
          loginUser.value = res.data
          return res.data
        } else {
          loginUser.value = null
          return null
        }
      } catch {
        loginUser.value = null
        return null
      } finally {
        fetchingPromise = null
      }
    })()
    return fetchingPromise
  }

  // 登出
  async function logout() {
    const res = await userApi.userLogout()
    if (res.code === 0) {
      loginUser.value = null
    }
    return res
  }

  return {
    loginUser,
    isLoggedIn,
    isAdmin,
    userName,
    login,
    register,
    fetchLoginUser,
    logout,
  }
})
