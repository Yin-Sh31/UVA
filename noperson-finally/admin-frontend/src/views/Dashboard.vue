<template>
  <el-container class="dashboard-container">
    <el-header class="header">
      <div class="header-left">
        <span class="logo">管理员系统</span>
      </div>
      <div class="header-right">
        <span>欢迎您，管理员</span>
        <el-button type="text" @click="handleLogout">退出登录</el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="200px" class="sidebar">
        <el-menu :default-active="activeMenu" class="el-menu-vertical" router @select="handleMenuSelect">
          <el-sub-menu index="1">
            <template #title>
              <el-icon><User /></el-icon>
              <span>农户管理</span>
            </template>
            <el-menu-item index="/dashboard/farmers">
              <el-icon><User /></el-icon>
              <span>农户列表</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="2">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>飞手管理</span>
            </template>
            <el-menu-item index="/dashboard/pilots">
              <el-icon><UserFilled /></el-icon>
              <span>飞手列表</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="3">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>机主管理</span>
            </template>
            <el-menu-item index="/dashboard/owners">
              <el-icon><Setting /></el-icon>
              <span>机主列表</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/devices">
              <el-icon><Phone /></el-icon>
              <span>设备列表</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="4">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>需求管理</span>
            </template>
            <el-menu-item index="/dashboard/requirements">
              <el-icon><Document /></el-icon>
              <span>需求列表</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="5">
            <template #title>
              <el-icon><Picture /></el-icon>
              <span>轮播图管理</span>
            </template>
            <el-menu-item index="/dashboard/banners">
              <el-icon><Picture /></el-icon>
              <span>轮播图列表</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="6">
            <template #title>
              <el-icon><Message /></el-icon>
              <span>聊天反馈</span>
            </template>
            <el-menu-item index="/dashboard/chat">
              <el-icon><Message /></el-icon>
              <span>聊天管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/feedback">
              <el-icon><Message /></el-icon>
              <span>反馈管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="7">
            <template #title>
              <el-icon><Sunny /></el-icon>
              <span>地图+天气</span>
            </template>
            <el-menu-item index="/dashboard/weather">
              <el-icon><Sunny /></el-icon>
              <span>天气管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="8">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/dashboard/system">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { User, UserFilled, Setting, Phone, Document, Picture, Message, Sunny } from '@element-plus/icons-vue'

export default {
  components: {
    User,
    UserFilled,
    Setting,
    Phone,
    Document,
    Picture,
    Message,
    Sunny
  },
  computed: {
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    handleLogout() {
      localStorage.removeItem('adminToken')
      this.$router.push('/login')
    },
    handleMenuSelect(key, keyPath) {
      console.log('menu select', key, keyPath)
      // 特殊处理requirements路径
      if (key === '/dashboard/requirements') {
        this.$router.push('/dashboard/demands')
        return
      }
      // 根据实际的路由路径进行跳转
      if (key.startsWith('/dashboard/')) {
        this.$router.push(key)
      } else {
        // 兼容旧的索引格式
        switch (key) {
          case '1':
          case '/dashboard/farmers':
            this.$router.push('/dashboard/farmers')
            break
          case '2':
          case '/dashboard/pilots':
            this.$router.push('/dashboard/pilots')
            break
          case '3':
          case '/dashboard/owners':
            this.$router.push('/dashboard/owners')
            break
          case '4':
          case '/dashboard/devices':
            this.$router.push('/dashboard/devices')
            break
          case '5':
          case '/dashboard/banners':
            this.$router.push('/dashboard/banners')
            break
          case '6':
          case '/dashboard/demands':
            this.$router.push('/dashboard/demands')
            break
          case '7':
          case '/dashboard/chat':
            this.$router.push('/dashboard/chat')
            break
          case '8':
          case '/dashboard/weather':
            this.$router.push('/dashboard/weather')
            break
          default:
            break
        }
      }
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left .logo {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.sidebar {
  background-color: #545c64;
}

.sidebar .el-menu {
  background-color: #545c64;
  border-right: none;
}

.sidebar .el-menu-item,
.sidebar .el-sub-menu__title {
  color: #fff;
}

.sidebar .el-menu-item:hover,
.sidebar .el-sub-menu__title:hover {
  background-color: #677683;
}

.sidebar .el-menu-item.is-active {
  background-color: #409eff;
}

.main {
  padding: 20px;
  background-color: #f0f2f5;
}
</style>