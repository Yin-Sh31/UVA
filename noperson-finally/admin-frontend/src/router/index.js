import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  // 添加dashboard根路径的重定向
  {
    path: '/dashboard',
    redirect: '/dashboard/farmers',
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'farmers',
        name: 'Farmers',
        component: () => import('../views/Farmers.vue')
      },
      {
        path: 'pilots',
        name: 'Pilots',
        component: () => import('../views/Pilots.vue')
      },
      {
        path: 'owners',
        name: 'Owners',
        component: () => import('../views/Owners.vue')
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('../views/Devices.vue')
      },
      {
        path: 'demands',
        name: 'Demands',
        component: () => import('../views/Demands.vue')
      },
      {
        path: 'banners',
        name: 'Banners',
        component: () => import('../views/Banners.vue')
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/Chat.vue')
      },
      {
          path: 'feedback',
          name: 'Feedback',
          component: () => import('../views/Feedback.vue')
        },
        {
          path: 'weather',
          name: 'Weather',
          component: () => import('../views/WeatherManagement.vue')
        },
        {
          path: 'system',
          name: 'System',
          component: () => import('../views/SystemManagement.vue')
        }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 需要认证的页面
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否有token
    const token = localStorage.getItem('adminToken')
    if (!token) {
      // 没有token，重定向到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else {
      // 有token，继续访问
      next()
    }
  } else {
    // 不需要认证的页面，直接访问
    next()
  }
})

// 导出路由实例
export default router