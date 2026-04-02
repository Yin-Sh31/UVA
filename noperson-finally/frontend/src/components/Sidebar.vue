<template>
  <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar" :class="{ 'sidebar--collapsed': isCollapse }">
    <div class="logo-wrapper">
      <div class="logo" :class="{ 'logo--collapsed': isCollapse }">
        <svg class="logo-icon" viewBox="0 0 1024 1024" version="1.1">
          <path d="M512 0C229.23 0 0 229.23 0 512s229.23 512 512 512 512-229.23 512-512S794.77 0 512 0z" fill="#409EFF"/>
          <path d="M721.067 517.12a16 16 0 0 0 0-28.224L544 320l-177.067 168.896a16 16 0 0 0 0 28.224L544 640l177.067-122.88z" fill="#FFFFFF"/>
          <path d="M282.667 405.333c-17.728 0-32 14.272-32 32s14.272 32 32 32 32-14.272 32-32-14.272-32-32-32z" fill="#FFFFFF"/>
          <path d="M736 405.333c-17.728 0-32 14.272-32 32s14.272 32 32 32 32-14.272 32-32-14.272-32-32-32z" fill="#FFFFFF"/>
          <path d="M512 704c-17.728 0-32 14.272-32 32s14.272 32 32 32 32-14.272 32-32-14.272-32-32-32z" fill="#FFFFFF"/>
        </svg>
        <span v-if="!isCollapse" class="logo-text">无人机管理</span>
      </div>
    </div>
    
    <el-menu
      :collapse="isCollapse"
      :default-active="activePath"
      class="sidebar-menu"
      @select="handleSelect"
      :router="true"
      :collapse-transition="true"
      background-color="transparent"
      text-color="rgba(255, 255, 255, 0.65)"
      active-text-color="#409EFF"
    >
      <el-menu-item index="/dashboard" class="menu-item">
        <template #icon>
          <el-icon class="menu-icon"><Document /></el-icon>
        </template>
        <template #title>
          <span class="menu-text">仪表盘</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/profile" class="menu-item">
        <template #icon>
          <el-icon class="menu-icon"><User /></el-icon>
        </template>
        <template #title>
          <span class="menu-text">个人资料</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/qualification" class="menu-item">
        <template #icon>
          <el-icon class="menu-icon"><Wallet /></el-icon>
        </template>
        <template #title>
          <span class="menu-text">资质管理</span>
        </template>
      </el-menu-item>
      
      <el-sub-menu index="/devices" class="menu-item">
        <template #title>
          <el-icon class="menu-icon"><Setting /></el-icon>
          <span class="menu-text">设备管理</span>
        </template>
        <el-menu-item index="/devices" class="submenu-item">
          <span class="menu-text">设备列表</span>
        </el-menu-item>
        <el-menu-item index="/device/add" class="submenu-item">
          <span class="menu-text">添加设备</span>
        </el-menu-item>
        <el-menu-item index="/device-activities" class="submenu-item">
          <span class="menu-text">设备动态记录</span>
        </el-menu-item>
      </el-sub-menu>
      
      <el-menu-item index="/rentals" class="menu-item">
        <template #icon>
          <el-icon class="menu-icon"><Key /></el-icon>
        </template>
        <template #title>
          <span class="menu-text">租借管理</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/flight-plan" class="menu-item">
        <template #icon>
          <el-icon class="menu-icon"><Timer /></el-icon>
        </template>
        <template #title>
          <span class="menu-text">飞行计划</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/logs" class="menu-item">
        <template #icon>
          <el-icon class="menu-icon"><ChatDotRound /></el-icon>
        </template>
        <template #title>
          <span class="menu-text">操作日志</span>
        </template>
      </el-menu-item>
      
      <el-menu-item index="/chat" class="menu-item">
        <template #icon>
          <el-icon class="menu-icon"><Message /></el-icon>
        </template>
        <template #title>
          <span class="menu-text">消息中心</span>
        </template>
      </el-menu-item>
    </el-menu>
    
    <div class="toggle-button" @click="toggleCollapse" :class="{ 'toggle-button--collapsed': isCollapse }">
      <el-icon class="toggle-icon" :class="{ 'rotate': isCollapse }">
        <ArrowLeft />
      </el-icon>
    </div>
  </el-aside>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Document,
  User,
  Wallet,
  Setting,
  Timer,
  ChatDotRound,
  ArrowLeft,
  Key,
  Message
} from '@element-plus/icons-vue'

export default {
  name: 'Sidebar',
  components: {
    Document,
    User,
    Wallet,
    Setting,
    Timer,
    ChatDotRound,
    ArrowLeft,
    Key,
    Message
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const isCollapse = ref(false)
    
    // 获取当前激活的路由路径
    const activePath = computed(() => {
      let path = route.path
      // 处理嵌套路径的激活状态
      if (path.startsWith('/devices/')) {
        return '/devices'
      }
      return path
    })
    
    // 切换折叠状态
    const toggleCollapse = () => {
      isCollapse.value = !isCollapse.value
      // 可以将状态保存到localStorage中
      localStorage.setItem('sidebarCollapse', String(isCollapse.value))
    }
    
    // 处理菜单选择
    const handleSelect = (key, keyPath) => {
      // 这里可以添加菜单选择时的处理逻辑
      console.log('Menu selected:', key, keyPath)
    }
    
    // 从localStorage读取折叠状态
    const savedCollapse = localStorage.getItem('sidebarCollapse')
    if (savedCollapse !== null) {
      isCollapse.value = savedCollapse === 'true'
    }
    
    // 监听路由变化，可以在这里添加一些额外的处理
    watch(() => route.path, (newPath) => {
      console.log('Route changed to:', newPath)
    })
    
    return {
      isCollapse,
      activePath,
      toggleCollapse,
      handleSelect
    }
  }
}
</script>

<style scoped>
.sidebar {
  height: 100vh;
  background: linear-gradient(135deg, #001529 0%, #002855 100%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at top right, rgba(64, 158, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.logo-wrapper {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background: rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  width: 100%;
  transition: all 0.3s ease;
  padding: 8px 0;
}

.logo--collapsed {
  justify-content: center;
}

.logo-icon {
  width: 32px;
  height: 32px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.sidebar--collapsed .logo-icon {
  width: 28px;
  height: 28px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  white-space: nowrap;
  margin-left: 12px;
  background: linear-gradient(135deg, #fff 0%, #e0f7fa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 64px);
  padding: 16px 0;
  background: transparent;
}

.menu-item {
  transition: all 0.3s ease;
  position: relative;
  background: transparent !important;
  margin: 4px 0;
}

.menu-item:hover {
  background: rgba(64, 158, 255, 0.1) !important;
}

.menu-item.is-active {
  background: rgba(64, 158, 255, 0.15) !important;
}

.menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #409EFF;
}

.menu-icon {
  font-size: 18px !important;
  transition: all 0.3s ease;
}

.menu-text {
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.submenu-item {
  padding-left: 50px !important;
  background: rgba(255, 255, 255, 0.02) !important;
  transition: all 0.3s ease;
}

.submenu-item:hover {
  background: rgba(255, 255, 255, 0.05) !important;
}

/* 修复Element Plus的一些默认样式 */
:deep(.el-menu) {
  background: transparent !important;
}

:deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, 0.15) !important;
  border-right: none;
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(64, 158, 255, 0.1) !important;
}

:deep(.el-sub-menu__title.is-active) {
  background: rgba(64, 158, 255, 0.15) !important;
}

:deep(.el-menu-item.is-active) {
  color: #409EFF !important;
}

/* 折叠按钮美化 */
.toggle-button {
  position: absolute;
  right: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 40px;
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  box-shadow: 2px 0 8px rgba(64, 158, 255, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  outline: none;
}

.toggle-button:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409EFF 100%);
  box-shadow: 2px 0 12px rgba(64, 158, 255, 0.4);
  transform: translateY(-50%) scale(1.05);
}

.toggle-button:active {
  transform: translateY(-50%) scale(0.95);
}

.toggle-icon {
  color: #ffffff;
  transition: transform 0.3s ease;
  font-size: 16px;
}

.toggle-icon.rotate {
  transform: rotate(180deg);
}

/* 美化滚动条 */
:deep(.el-menu-vertical__content) {
  overflow-y: auto;
  overflow-x: hidden;
}

:deep(.el-menu-vertical__content)::-webkit-scrollbar {
  width: 4px;
}

:deep(.el-menu-vertical__content)::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 2px;
}

:deep(.el-menu-vertical__content)::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  transition: background 0.3s ease;
}

:deep(.el-menu-vertical__content)::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* 响应式优化 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
  }
  
  .sidebar--mobile-open {
    transform: translateX(0);
  }
  
  .toggle-button {
    display: none;
  }
}
</style>