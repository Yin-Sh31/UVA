<template>
  <el-header class="navbar">
    <div class="navbar-gradient"></div>
    <div class="navbar-content">
      <div class="left-section">
        <el-button 
          type="text" 
          class="menu-button"
          @click="toggleSidebar"
          size="large"
        >
          <el-icon class="menu-icon"><Menu /></el-icon>
        </el-button>
        <h1 class="page-title" v-if="currentPageTitle">
          <span class="title-icon">{{ getPageIcon(currentPageTitle) }}</span>
          <span>{{ currentPageTitle }}</span>
        </h1>
      </div>
      
      <div class="right-section">
        <!-- 只有登录状态才显示通知 -->
        <el-dropdown trigger="click" class="notification-dropdown" placement="bottom-end" v-if="userStore.isLoggedIn">
          <div class="notification-button">
            <el-icon class="bell-icon"><Bell /></el-icon>
            <el-badge 
              :value="notificationCount" 
              class="notification-badge" 
              :max="99"
              v-if="notificationCount > 0"
            />
          </div>
          <template #dropdown>
            <el-dropdown-menu class="notification-dropdown-menu">
              <div class="notification-header">
                <span class="notification-title">通知消息</span>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="markAllAsRead"
                  class="mark-all-btn"
                >
                  全部已读
                </el-button>
              </div>
              
              <div v-if="notifications.length === 0" class="empty-notification">
                <el-empty description="暂无通知" class="empty-content" />
              </div>
              
              <transition-group name="notification">
                <el-dropdown-item
                  v-for="notification in notifications"
                  :key="notification.id"
                  class="notification-item"
                  :class="{ 'unread': !notification.read }"
                  @click="markAsRead(notification.id)"
                >
                  <div class="notification-content">
                    <div class="notification-badge-dot" v-if="!notification.read"></div>
                    <div class="notification-body">
                      <div class="notification-title-text">{{ notification.title }}</div>
                      <div class="notification-desc">{{ notification.description }}</div>
                      <div class="notification-time">{{ formatTime(notification.time) }}</div>
                    </div>
                  </div>
                </el-dropdown-item>
              </transition-group>
              
              <div class="notification-footer" v-if="notifications.length > 0">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="viewAllNotifications"
                  class="view-all-btn"
                >
                  查看全部
                </el-button>
              </div>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <el-dropdown trigger="click" class="user-dropdown" placement="bottom-end">
          <div class="user-info" @click.stop>
            <el-avatar 
              :size="40" 
              :src="userAvatar"
              class="user-avatar"
              :class="{ 'not-logged-avatar': !userStore.isLoggedIn }"
            >
              {{ userInitial }}
            </el-avatar>
            <div class="user-details">
              <span class="user-name">{{ userName }}</span>
              <span class="user-role" v-if="userRole">管理员</span>
            </div>
            <el-icon class="arrow-icon"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown-menu">
              <!-- 根据登录状态显示不同的菜单项 -->
              <template v-if="userStore.isLoggedIn">
                <el-dropdown-item @click="navigateToProfile" class="dropdown-item">
                  <el-icon class="item-icon"><User /></el-icon>
                  <span>个人资料</span>
                </el-dropdown-item>
                <el-dropdown-item @click="navigateToSettings" class="dropdown-item">
                  <el-icon class="item-icon"><Setting /></el-icon>
                  <span>系统设置</span>
                </el-dropdown-item>
                <el-dropdown-item class="dropdown-item" @click="showAbout">
                  <el-icon class="item-icon"><InfoFilled /></el-icon>
                  <span>关于系统</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout" class="dropdown-item logout-item">
                  <el-icon class="item-icon logout-icon"><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </template>
              <template v-else>
                <el-dropdown-item @click="handleLogin" class="dropdown-item login-item">
                  <el-icon class="item-icon"><User /></el-icon>
                  <span>登录</span>
                </el-dropdown-item>
                <el-dropdown-item @click="handleRegister" class="dropdown-item">
                  <el-icon class="item-icon"><Plus /></el-icon>
                  <span>注册</span>
                </el-dropdown-item>
                <el-dropdown-item class="dropdown-item" @click="showAbout">
                  <el-icon class="item-icon"><InfoFilled /></el-icon>
                  <span>关于系统</span>
                </el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </el-header>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { Bell, ArrowDown, User, Setting, SwitchButton, Menu, InfoFilled, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'Navbar',
  components: {
    Bell,
    ArrowDown,
    User,
    Setting,
    SwitchButton,
    Menu,
    InfoFilled,
    Plus
  },
  emits: ['toggle-sidebar'],
  setup(props, { emit }) {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    
    // 导航到登录页面
    const handleLogin = () => {
      router.push('/login')
    }
    
    // 导航到注册页面
    const handleRegister = () => {
      router.push('/register')
    }
    
    const notificationCount = ref(3)
    const notifications = ref([
      {
        id: 1,
        title: '资质审核通过',
        description: '您的无人机驾驶证已通过审核，可以开始飞行作业',
        time: '2024-01-20 14:30:00',
        read: false
      },
      {
        id: 2,
        title: '设备固件更新',
        description: '您的设备DJI Mavic 3有新的固件版本可以更新',
        time: '2024-01-19 09:15:00',
        read: true
      },
      {
        id: 3,
        title: '飞行区域限制',
        description: '您计划的飞行区域已被临时限制，请重新规划路线',
        time: '2024-01-18 16:45:00',
        read: false
      }
    ])
    
    // 计算属性：获取当前页面标题
    const currentPageTitle = computed(() => {
      const path = route.path
      const titleMap = {
        '/dashboard': '仪表盘',
        '/profile': '个人资料',
        '/qualification': '资质管理',
        '/devices': '设备列表',
        '/add-device': '添加设备',
        '/flight-plan': '飞行计划',
        '/logs': '操作日志'
      }
      
      for (const [key, title] of Object.entries(titleMap)) {
        if (path.startsWith(key)) {
          return title
        }
      }
      return '无人机管理系统'
    })
    
    // 获取用户名称
    const userName = computed(() => {
      // 检查登录状态和用户信息是否存在
      if (userStore.isLoggedIn && userStore.userInfo) {
        // 优先使用username字段，如果不存在则使用name字段作为备选
        return userStore.userInfo.username || userStore.userInfo.name || '用户'
      }
      return '未登录'
    })
    
    // 获取用户角色
    const userRole = computed(() => {
      return userStore.userInfo?.role || null
    })
    
    // 获取用户头像
    const userAvatar = computed(() => {
      return userStore.userInfo?.avatar || ''
    })
    
    // 获取用户名字首字母
    const userInitial = computed(() => {
      if (userStore.userInfo && userStore.userInfo.username) {
        // 只使用username字段获取首字母
        return userStore.userInfo.username.charAt(0).toUpperCase()
      }
      return 'U'
    })
    
    // 根据页面标题获取对应的图标
    const getPageIcon = (title) => {
      const iconMap = {
        '仪表盘': '📊',
        '个人资料': '👤',
        '资质管理': '📋',
        '设备列表': '🛠️',
        '添加设备': '➕',
        '租借管理': '🔑',
        '飞行计划': '✈️',
        '操作日志': '📝'
      }
      return iconMap[title] || ''
    }
    
    // 切换侧边栏
    const toggleSidebar = () => {
      emit('toggle-sidebar')
      // 添加按钮点击动画效果
      const button = event.target.closest('.menu-button')
      if (button) {
        button.classList.add('menu-button--clicked')
        setTimeout(() => {
          button.classList.remove('menu-button--clicked')
        }, 200)
      }
    }
    
    // 标记为已读
    const markAsRead = (id) => {
      const notification = notifications.value.find(n => n.id === id)
      if (notification && !notification.read) {
        // 添加动画效果
        const item = document.querySelector(`[key="${id}"]`)
        if (item) {
          item.style.transition = 'all 0.3s ease'
          item.style.opacity = '0.6'
          setTimeout(() => {
            item.style.opacity = '1'
          }, 300)
        }
        
        notification.read = true
        notificationCount.value = notifications.value.filter(n => !n.read).length
        ElMessage.success('已标记为已读', { duration: 1500 })
      }
    }
    
    // 标记全部已读
    const markAllAsRead = () => {
      notifications.value.forEach(n => {
        n.read = true
      })
      notificationCount.value = 0
      ElMessage.success('全部已标记为已读', { duration: 1500 })
    }
    
    // 查看全部通知
    const viewAllNotifications = () => {
      ElMessage.info('跳转到通知中心')
      // 这里应该跳转到通知中心页面
      // router.push('/notifications')
    }
    
    // 导航到个人资料
    const navigateToProfile = () => {
      router.push('/profile')
    }
    
    // 导航到系统设置
    const navigateToSettings = () => {
      ElMessage.info('系统设置功能开发中')
    }
    
    // 显示关于系统对话框
    const showAbout = () => {
      ElMessageBox.alert(
        '<div class="about-content">' +
        '<h3>无人机机主管理系统</h3>' +
        '<p>版本: 1.0.0</p>' +
        '<p>© 2024 无人机管理团队</p>' +
        '<p>为无人机机主提供全方位的设备管理和资质认证服务</p>' +
        '</div>',
        '关于系统',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '确定',
          center: true
        }
      )
    }
    
    // 退出登录
    const handleLogout = () => {
      ElMessageBox.confirm(
        '确定要退出登录吗？',
        '退出确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
          center: true
        }
      ).then(() => {
        userStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录', { duration: 1500 })
      }).catch(() => {
        // 取消退出
      })
    }
    
    // 格式化时间
    const formatTime = (timeStr) => {
      const date = new Date(timeStr)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMins / 60)
      const diffDays = Math.floor(diffHours / 24)
      
      if (diffMins < 1) {
        return '刚刚'
      } else if (diffMins < 60) {
        return `${diffMins}分钟前`
      } else if (diffHours < 24) {
        return `${diffHours}小时前`
      } else if (diffDays < 7) {
        return `${diffDays}天前`
      } else {
        return date.toLocaleDateString()
      }
    }
    
    // 监听路由变化
    watch(() => route.path, (newPath) => {
      // 路由变化时可以添加页面切换效果
    })
    
    return {
      currentPageTitle,
      userName,
      userRole,
      userAvatar,
      userInitial,
      notificationCount,
      notifications,
      toggleSidebar,
      markAsRead,
      markAllAsRead,
      viewAllNotifications,
      navigateToProfile,
      navigateToSettings,
      showAbout,
      handleLogout,
      formatTime,
      getPageIcon,
      userStore,
      handleLogin,
      handleRegister
    }
  }
}
</script>

<style scoped>
.navbar {
  height: 64px;
  background-color: #ffffff;
  position: relative;
  z-index: 100;
  transition: all 0.3s ease;
  overflow: hidden;
}

.navbar-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  z-index: -1;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 24px;
  position: relative;
  z-index: 1;
}

/* 左侧区域 */
.left-section {
  display: flex;
  align-items: center;
  flex: 1;
}

.menu-button {
  margin-right: 20px;
  color: #606266;
  padding: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
  border: none;
  background: transparent;
}

.menu-button:hover {
  background-color: #f5f7fa;
  color: #409EFF;
  transform: scale(1.05);
}

.menu-button--clicked {
  transform: scale(0.95);
}

.menu-icon {
  font-size: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 22px;
  opacity: 0.85;
}

/* 右侧区域 */
.right-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 通知按钮 */
.notification-button {
  position: relative;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #606266;
}

.notification-button:hover {
  background-color: #f5f7fa;
  color: #409EFF;
  transform: scale(1.05);
}

.bell-icon {
  font-size: 20px;
}

.notification-badge {
  position: absolute;
  top: 0;
  right: 0;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4);
  }
  70% {
    transform: scale(1.05);
    box-shadow: 0 0 0 6px rgba(245, 108, 108, 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}

/* 通知下拉菜单 */
.notification-dropdown-menu {
  width: 380px;
  padding: 0;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border: none;
  overflow: hidden;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.notification-title {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.mark-all-btn {
  color: #409EFF;
  padding: 0;
  font-size: 12px;
  height: auto;
  line-height: normal;
}

.mark-all-btn:hover {
  color: #66b1ff;
  background-color: transparent;
}

.empty-notification {
  padding: 40px 20px;
  text-align: center;
}

.empty-content {
  margin: 0;
}

.notification-item {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.notification-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: transparent;
  transition: background-color 0.3s ease;
}

.notification-item:hover {
  background-color: #fafafa;
  transform: translateX(2px);
}

.notification-item.unread {
  background-color: #f0f9ff;
}

.notification-item.unread::before {
  background-color: #409EFF;
}

.notification-content {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.notification-badge-dot {
  width: 8px;
  height: 8px;
  background-color: #409EFF;
  border-radius: 50%;
  margin-top: 6px;
  flex-shrink: 0;
}

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-title-text {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  font-size: 14px;
}

.notification-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-footer {
  padding: 16px 20px;
  text-align: center;
  background-color: #fafafa;
}

.view-all-btn {
  width: 100%;
  font-size: 14px;
}

/* 通知动画 */
.notification-enter-active,
.notification-leave-active {
  transition: all 0.3s ease;
}

.notification-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.notification-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 用户下拉菜单 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.user-info:hover {
  background-color: #f5f7fa;
  transform: translateY(-1px);
}

.user-avatar {
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.user-info:hover .user-avatar {
  transform: scale(1.05);
  border-color: #409EFF;
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
}

.user-role {
  font-size: 12px;
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
  padding: 1px 6px;
  border-radius: 10px;
}

.arrow-icon {
  font-size: 16px;
  color: #909399;
  transition: transform 0.3s ease;
}

.el-dropdown-menu__popper.user-dropdown-open .arrow-icon {
  transform: rotate(180deg);
}

/* 用户下拉菜单内容 */
.user-dropdown-menu {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border: none;
  overflow: hidden;
  padding: 4px 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #606266;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
  color: #409EFF;
  padding-left: 24px;
}

.item-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.logout-item {
  color: #f56c6c;
}

.logout-item:hover {
  background-color: #fef0f0;
  color: #f56c6c;
}

.logout-icon {
  color: #f56c6c;
}

/* 关于对话框样式 */
:deep(.about-content) {
  text-align: center;
}

:deep(.about-content h3) {
  margin-bottom: 12px;
  color: #303133;
}

:deep(.about-content p) {
  margin-bottom: 8px;
  color: #606266;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .notification-dropdown-menu {
    width: 340px;
  }
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 0 16px;
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .title-icon {
    font-size: 20px;
  }
  
  .right-section {
    gap: 12px;
  }
  
  .user-details {
    display: none;
  }
  
  .notification-dropdown-menu {
    width: 300px;
    max-width: 90vw;
  }
  
  .notification-header {
    padding: 12px 16px;
  }
  
  .notification-item {
    padding: 12px 16px;
  }
}

@media (max-width: 480px) {
  .navbar-content {
    padding: 0 12px;
  }
  
  .page-title {
    font-size: 16px;
    gap: 8px;
  }
  
  .menu-button {
    margin-right: 12px;
    padding: 6px;
  }
  
  .right-section {
    gap: 8px;
  }
}

/* 未登录状态的头像样式 */
.not-logged-avatar {
  background-color: #dcdfe6 !important;
  color: #909399 !important;
}

/* 登录按钮样式 */
.login-item {
  color: var(--primary-color) !important;
}

.login-item:hover {
  background-color: var(--primary-color-light) !important;
}
</style>