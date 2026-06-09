import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/user/login',
      name: 'userLogin',
      component: () => import('../views/user/UserLoginView.vue'),
    },
    {
      path: '/admin/user',
      name: 'adminUser',
      component: () => import('../views/admin/UserManageView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
  ],
})

// 路由守卫
router.beforeEach(async (to) => {
  // 不需要登录的页面放行
  if (!to.meta.requiresAuth) {
    return true
  }

  // 尝试获取登录用户信息（如果 store 中还没有）
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    await userStore.fetchLoginUser()
  }

  // 未登录 → 跳转登录页
  if (!userStore.isLoggedIn) {
    return { path: '/user/login', query: { redirect: to.fullPath } }
  }

  // 需要管理员权限但不是管理员 → 跳首页
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return { path: '/' }
  }

  return true
})

export default router
