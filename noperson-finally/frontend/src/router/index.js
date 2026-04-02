import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  // 简单的测试路由，直接返回HTML内容
  {
    path: '/test',
    name: 'Test',
    component: {
      template: `<div style="padding: 20px; text-align: center;"><h1>测试页面</h1><p>这个页面用于测试路由功能是否正常工作。</p><a href="/register">前往注册页面</a></div>`
    },
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/qualification',
    name: 'Qualification',
    component: () => import('../views/Qualification.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/devices',
    name: 'Devices',
    component: () => import('../views/Devices.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/device/add',
    name: 'AddDevice',
    component: () => import('../views/AddDevice.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/device/:id/edit',
    name: 'EditDevice',
    component: () => import('../views/EditDevice.vue'),
    meta: { requiresAuth: true }
  },
  { path: '/detail/:deviceId', name: 'DeviceDetail', component: () => import('../views/DeviceDetail.vue'), meta: { requiresAuth: true } },
  { path: '/rentals', name: 'RentalManagement', component: () => import('../views/RentalManagement.vue'), meta: { requiresAuth: true } },
  { path: '/device-activities', name: 'DeviceActivities', component: () => import('../views/DeviceActivities.vue'), meta: { requiresAuth: true } },
  { path: '/chat', name: 'Chat', component: () => import('../views/Chat.vue'), meta: { requiresAuth: true } }]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 对于测试、登录和注册页面，直接放行，不做任何检查
  if (to.path === '/test' || to.path === '/login' || to.path === '/register') {
    console.log('路由守卫：直接放行到公共页面:', to.path)
    next()
    return
  }
  
  // 动态导入useUserStore，避免在路由守卫中直接使用
  import('../store/user').then(({ useUserStore }) => {
    const userStore = useUserStore()
    const token = localStorage.getItem('token')
    
    // 检查是否需要认证
    if (to.meta.requiresAuth) {
      if (token && userStore.isLoggedIn) {
        next()
      } else {
        next('/login')
      }
    } else {
      // 其他非认证页面，如果已经登录且要去登录页，重定向到首页
      if (token && to.path === '/login') {
        next('/dashboard')
      } else {
        next()
      }
    }
  })
})

export default router