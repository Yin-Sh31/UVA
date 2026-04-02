<template>
  <div class="dashboard">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h2 class="welcome-title">
          <span class="welcome-icon">👋</span>
          您好，欢迎回来！
        </h2>
        <p class="welcome-subtitle">{{ getGreetingMessage() }} 今天是 {{ getCurrentDate() }}</p>
      </div>
    </div>
    
    <!-- 设备统计概览卡片 -->
    <el-card class="card statistics-card">
      <template #header>
        <div class="card-header">
          <h3 class="section-title">设备统计概览</h3>
          <span class="header-icon">📊</span>
        </div>
      </template>
      
      <div class="stats-container">
        <transition-group name="stat-item" class="stat-grid">
          <div v-for="(item, index) in statItems" :key="item.key" class="stat-item">
            <div class="stat-card" :class="`stat-card-${index + 1}`">
              <div class="stat-icon">{{ item.icon }}</div>
              <div class="stat-content">
                <div class="stat-title">{{ item.title }}</div>
                <div class="stat-value">
                  <el-statistic 
                    :value="item.value" 
                    :precision="0"
                    animation 
                    animationDuration="2000"
                    class="stat-number"
                  />
                  <span v-if="item.change !== undefined" class="stat-change" :class="{ positive: item.change > 0, negative: item.change < 0 }">
                    {{ item.change > 0 ? '↑' : '↓' }}{{ Math.abs(item.change) }}%
                  </span>
                </div>
                <div class="stat-description">{{ item.description }}</div>
              </div>
            </div>
          </div>
        </transition-group>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <div class="chart-container">
      <!-- 设备类型分布卡片 -->
      <el-card class="card chart-card type-card">
        <template #header>
          <div class="card-header">
            <h3 class="section-title">设备类型分布</h3>
            <span class="header-icon">📱</span>
          </div>
        </template>
        <div class="chart-wrapper">
          <div class="chart-content">
            <!-- 条件渲染：有数据显示饼图 -->
            <div v-if="deviceTypeDistribution && Object.keys(deviceTypeDistribution).length > 0">
              <!-- 饼图模拟 -->
              <div class="pie-chart-container">
                <div class="pie-chart">
                  <div class="chart-inner-circle"></div>
                  <div v-for="(value, key, index) in deviceTypeDistribution" 
                       :key="key" 
                       class="chart-sector" 
                       :style="getSectorStyle(index, value)">
                  </div>
                </div>
                
                <div class="chart-legend">
                  <div v-for="(value, key, index) in deviceTypeDistribution" 
                       :key="key" 
                       class="legend-item">
                    <span class="legend-color" :style="{ backgroundColor: progressColors[index] }"></span>
                    <span class="legend-label">{{ key }}</span>
                    <span class="legend-value">{{ value }}台</span>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 无数据时显示空状态 -->
            <div v-if="!deviceTypeDistribution || Object.keys(deviceTypeDistribution).length === 0" class="empty-data">
              <el-empty description="暂无设备数据" :image-size="100" />
            </div>
          </div>
        </div>
      </el-card>

      <!-- 资质审核状态卡片 -->
      <el-card class="card chart-card status-card">
        <template #header>
          <div class="card-header">
            <h3 class="section-title">资质审核状态</h3>
            <span class="header-icon">📋</span>
          </div>
        </template>
        <div class="audit-status">
          <div class="status-container" :class="`status-${auditStatus}`">
            <div class="status-icon">
              {{ auditStatus === 0 ? '⏳' : auditStatus === 1 ? '✅' : '❌' }}
            </div>
            <h4 class="status-title">{{ auditStatusText }}</h4>
            <p class="status-description">{{ auditStatusDesc }}</p>
            <template v-if="auditStatus === 0 || auditStatus === 2">
              <el-button 
                type="primary" 
                @click="$router.push('/qualification')"
                class="status-button"
                :class="{ 'secondary': auditStatus === 2 }"
              >
                {{ auditStatus === 0 ? '查看审核进度' : '重新提交资料' }}
                <el-icon class="button-icon"><ArrowRight /></el-icon>
              </el-button>
            </template>
            <div v-else class="status-badge">
              <el-tag type="success" effect="plain">认证成功</el-tag>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 最近设备动态卡片 -->
    <el-card class="card activity-card">
      <template #header>
        <div class="card-header">
          <h3 class="section-title">最近设备动态</h3>
          <el-button 
            type="text" 
            @click="viewAllLogs"
            class="view-all-button"
          >
            查看全部 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      
      <div class="activity-content">
        <DeviceActivityList />
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
// 直接导入配置好的axios实例
import axios from '../utils/axios'
// 设置正确的baseURL以匹配后端实际端口，不包含/api前缀
axios.defaults.baseURL = 'http://localhost:8082'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import DeviceActivityList from '../components/DeviceActivityList.vue'

export default {
  name: 'Dashboard',
  components: {
    DeviceActivityList,
    ArrowRight
  },
  setup() {
      const router = useRouter()
      
    // 响应式数据
    const stats = ref({
      total: 0,
      available: 0,
      working: 0,
      maintaining: 0
    })
    const deviceTypeDistribution = ref({})
    const deviceLogs = ref([
      {
        deviceName: 'DJI Mavic 3',
        operation: '上线',
        operator: '系统',
        operateTime: '2025-10-21 09:30:00',
        status: '正常'
      },
      {
        deviceName: 'DJI Phantom 4',
        operation: '维护',
        operator: '张师傅',
        operateTime: '2025-10-20 16:45:00',
        status: '维护中'
      }
    ])
    const auditStatus = ref(1) // 0待审核，1通过，2拒绝
    const progressColors = [
      '#13ce66',
      '#5cb8ff',
      '#ffc82c',
      '#ff4949',
      '#a0d911'
    ]

    // 计算属性
    const statItems = computed(() => [
      {
        key: 'total',
        title: '设备总数',
        value: stats.value.total,
        icon: '📱',
        change: 12,
        description: '较上月增长'
      },
      {
        key: 'available',
        title: '可用设备数',
        value: stats.value.available,
        icon: '✅',
        change: 5,
        description: '设备健康状态良好'
      },
      {
        key: 'working',
        title: '工作中设备数',
        value: stats.value.working,
        icon: '⚡',
        change: 20,
        description: '正在执行任务'
      },
      {
        key: 'maintaining',
        title: '维护中设备数',
        value: stats.value.maintaining,
        icon: '🔧',
        change: -100,
        description: '定期维护已完成'
      }
    ])

    const mockDeviceTypeDistribution = {
      'DJI Mavic系列': 3,
      'DJI Phantom系列': 2,
      'DJI Mini系列': 2,
      '其他品牌': 1
    }

    const auditStatusIcon = computed(() => {
      switch (auditStatus.value) {
        case 0: return 'info'
        case 1: return 'success'
        case 2: return 'error'
        default: return 'info'
      }
    })

    const auditStatusText = computed(() => {
      switch (auditStatus.value) {
        case 0: return '待审核'
        case 1: return '审核通过'
        case 2: return '审核未通过'
        default: return '未知状态'
      }
    })

    const auditStatusDesc = computed(() => {
      switch (auditStatus.value) {
        case 0: return '您的机主资质正在审核中，请耐心等待'
        case 1: return '恭喜，您的机主资质已通过审核，可正常使用系统'
        case 2: return '请检查并重新提交您的机主资质资料'
        default: return ''
      }
    })

    // 工具函数
    const getPercentage = (value) => {
      const total = stats.value.total || 8
      if (total === 0) return 0
      return Math.round((value / total) * 100)
    }

    const getSectorStyle = (index, value) => {
      const total = stats.value.total || 8
      const percentage = (value / total) * 100
      const startAngle = index > 0 ? 
        Object.values(deviceTypeDistribution.value || mockDeviceTypeDistribution)
          .slice(0, index)
          .reduce((sum, v) => sum + (v / total) * 360, 0) : 0
      const endAngle = startAngle + (percentage / 100) * 360
      
      return {
        background: progressColors[index % progressColors.length],
        clipPath: `polygon(
          50% 50%,
          50% 0,
          ${50 + 50 * Math.cos((startAngle - 90) * Math.PI / 180)}% ${50 - 50 * Math.sin((startAngle - 90) * Math.PI / 180)}%,
          ${50 + 50 * Math.cos((endAngle - 90) * Math.PI / 180)}% ${50 - 50 * Math.sin((endAngle - 90) * Math.PI / 180)}%
        )`
      }
    }

    const getGreetingMessage = () => {
      const hour = new Date().getHours()
      if (hour < 6) return '深夜好'
      if (hour < 12) return '早上好'
      if (hour < 14) return '中午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    }

    const getCurrentDate = () => {
      const date = new Date()
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    }

    const formatTime = (timeStr) => {
      const date = new Date(timeStr)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMins / 60)
      const diffDays = Math.floor(diffHours / 24)
      
      if (diffMins < 1) return '刚刚'
      if (diffMins < 60) return `${diffMins}分钟前`
      if (diffHours < 24) return `${diffHours}小时前`
      if (diffDays < 7) return `${diffDays}天前`
      return date.toLocaleDateString()
    }

    const getStatusClass = (status) => {
      switch (status) {
        case '正常': return 'success'
        case '维护中': return 'warning'
        case '故障': return 'danger'
        default: return 'info'
      }
    }

    const getStatusIcon = (status) => {
      switch (status) {
        case '正常': return '✓'
        case '维护中': return '⚙'
        case '故障': return '⚠'
        default: return 'ℹ'
      }
    }

    const getOperationClass = (operation) => {
      switch (operation) {
        case '上线': return 'success'
        case '维护': return 'warning'
        case '故障': return 'danger'
        default: return 'info'
      }
    }

    const viewAllLogs = () => {
      router.push('/device-activities')
    }

    // API调用函数
    const fetchDeviceStat = async () => {
      try {
        console.log('开始请求设备统计数据...')
        console.log('请求URL:', axios.defaults.baseURL + '/owner/device/stat')
        console.log('localStorage中的token:', localStorage.getItem('token') ? '存在' : '不存在')
        
        if (!localStorage.getItem('token')) {
          console.log('设置测试token')
          localStorage.setItem('token', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEwLCJpYXQiOjE3MDEyMDg2OTMsImV4cCI6MTcwMTIwODc5M30.XYZ')
        }
        
        const response = await axios.get('/owner/device/stat')
        
        console.log('设备统计API响应:', response)
        // 注意：由于axios拦截器返回的是response.data，所以这里的response就是后端返回的数据对象
        console.log('后端返回的code:', response.code)
        console.log('后端返回的数据:', response)
        
        // 只检查后端返回的code即可
        if (response.code === 200) {
          let deviceStatData = response.data || response
          
          if (deviceStatData.total !== undefined) {
            stats.value = {
              total: deviceStatData.total || 0,
              available: deviceStatData.available || 0,
              working: deviceStatData.working || 0,
              maintaining: deviceStatData.maintaining || 0
            }
            
            console.log('处理后的统计数据:', stats.value)
            
            if (deviceStatData.typeDistribution) {
              deviceTypeDistribution.value = deviceStatData.typeDistribution
              console.log('设备类型分布:', deviceTypeDistribution.value)
            }
          } else {
            console.warn('API返回数据格式不正确')
            ElMessage.warning('获取设备统计数据格式不正确')
          }
        } else {
          console.warn('后端返回code不为200:', response.code)
          ElMessage.warning('获取设备统计数据失败')
        }
      } catch (error) {
        console.error('获取设备统计失败:', error)
        if (error.response) {
          console.error('响应状态:', error.response.status)
          console.error('响应数据:', error.response.data)
          ElMessage.error(`获取设备统计失败: ${error.response.statusText}`)
        } else if (error.request) {
          console.error('请求已发送但无响应:', error.request)
          ElMessage.error('服务器无响应，请检查后端服务是否运行')
        } else {
          console.error('请求配置错误:', error.message)
          ElMessage.error('请求配置错误，请刷新页面重试')
        }
      }
    }

    const fetchOwnerDetail = async () => {
      try {
        console.log('开始请求机主详情数据...')
        console.log('请求URL:', '/owner/detail')
        
        const response = await axios.get('/owner/detail')
        
        console.log('机主详情API响应:', response.data)
        auditStatus.value = response.data.data?.auditStatus || null
        console.log('处理后的审核状态:', auditStatus.value)
      } catch (error) {
        console.error('获取机主详情失败:', error)
        if (error.response) {
          console.error('响应状态:', error.response.status)
          console.error('响应数据:', error.response.data)
          ElMessage.error(`获取机主详情失败: ${error.response.statusText}`)
        } else if (error.request) {
          console.error('请求已发送但无响应:', error.request)
          ElMessage.error('服务器无响应，请检查后端服务是否运行')
        } else {
          console.error('请求配置错误:', error.message)
          ElMessage.error('请求配置错误，请刷新页面重试')
        }
      }
    }

    // 生命周期
    onMounted(() => {
      fetchDeviceStat()
      fetchOwnerDetail()
    })

    // 返回组件使用的状态和方法
    return {
      stats,
      statItems,
      deviceTypeDistribution,
      deviceLogs,
      auditStatus,
      auditStatusIcon,
      auditStatusText,
      auditStatusDesc,
      progressColors,
      getPercentage,
      getSectorStyle,
      getGreetingMessage,
      getCurrentDate,
      formatTime,
      getStatusClass,
      getStatusIcon,
      getOperationClass,
      viewAllLogs
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
.dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e6eaf0 100%);
  min-height: calc(100vh - 64px);
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 30px;
  animation: fadeInUp 0.6s ease-out;
}

.welcome-content {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
  padding: 30px 40px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.3);
  color: white;
  position: relative;
  overflow: hidden;
}

.welcome-content::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite;
}

.welcome-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  position: relative;
  z-index: 1;
}

.welcome-icon {
  font-size: 32px;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  position: relative;
  z-index: 1;
}

/* 卡片基础样式 */
.card {
  margin-bottom: 24px;
  border-radius: 12px !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #67C23A, #E6A23C, #F56C6C);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card:hover::before {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px 24px !important;
  background-color: transparent;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 20px;
  opacity: 0.8;
}

.view-all-button {
  color: #409EFF;
  font-size: 14px;
  padding: 0;
  height: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.view-all-button:hover {
  color: #66B1FF;
  transform: translateX(3px);
}

/* 统计卡片 */
.statistics-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
}

.stats-container {
  padding: 20px 0;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  padding: 0 20px;
}

.stat-item {
  position: relative;
}

.stat-card {
  padding: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(240, 245, 250, 0.8) 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card-1 {
  border-left: 4px solid #409EFF;
}

.stat-card-2 {
  border-left: 4px solid #67C23A;
}

.stat-card-3 {
  border-left: 4px solid #E6A23C;
}

.stat-card-4 {
  border-left: 4px solid #F56C6C;
}

.stat-icon {
  font-size: 36px;
  padding: 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.stat-number {
  font-size: 28px !important;
  font-weight: 600;
  color: #303133;
}

.stat-change {
  font-size: 14px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 10px;
}

.stat-change.positive {
  color: #67C23A;
  background-color: #F0F9EB;
}

.stat-change.negative {
  color: #F56C6C;
  background-color: #FEF0F0;
}

.stat-description {
  font-size: 12px;
  color: #909399;
}

/* 图表容器 */
.chart-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.chart-card {
  height: 100%;
  min-height: 350px;
}

.chart-wrapper {
  padding: 20px;
  height: calc(100% - 70px);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 设备类型分布 */
.type-card .chart-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.pie-chart-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.pie-chart {
  position: relative;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e6eaf0 100%);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.chart-inner-circle {
  position: absolute;
  width: 60%;
  height: 60%;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.95);
  top: 20%;
  left: 20%;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #606266;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.08);
}

.chart-sector {
  position: absolute;
  width: 100%;
  height: 100%;
  transition: transform 0.6s ease;
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 300px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
}

.legend-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(4px);
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  flex-shrink: 0;
}

.legend-label {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.legend-value {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

/* 审核状态 */
.status-card .chart-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.audit-status {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.status-container {
  text-align: center;
  padding: 32px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  max-width: 350px;
  width: 100%;
  transition: all 0.3s ease;
}

.status-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.status-0 {
  border-top: 4px solid #409EFF;
}

.status-1 {
  border-top: 4px solid #67C23A;
}

.status-2 {
  border-top: 4px solid #F56C6C;
}

.status-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.9;
}

.status-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.status-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 24px;
}

.status-button {
  border-radius: 8px;
  padding: 8px 24px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.status-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.status-button.secondary {
  background-color: #E6A23C;
  border-color: #E6A23C;
}

.status-button.secondary:hover {
  background-color: #ebb563;
  border-color: #ebb563;
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

.button-icon {
  margin-left: 4px;
}

.status-badge {
  margin-top: 8px;
}

/* 活动时间线 */
.activity-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
}

.activity-content {
  padding: 20px 0;
}

.activity-timeline {
  position: relative;
  padding-left: 40px;
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
  display: flex;
  gap: 20px;
}

.timeline-item--last {
  margin-bottom: 0;
}

.timeline-left {
  position: absolute;
  left: -40px;
  top: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.timeline-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: bold;
  z-index: 1;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.timeline-icon.success {
  background: #67C23A;
}

.timeline-icon.warning {
  background: #E6A23C;
}

.timeline-icon.danger {
  background: #F56C6C;
}

.timeline-icon.info {
  background: #409EFF;
}

.timeline-line {
  position: relative;
  width: 2px;
  flex: 1;
  background: linear-gradient(to bottom, #f0f0f0 60%, transparent);
  margin-top: 8px;
}

.timeline-content {
  flex: 1;
}

.timeline-card {
  padding: 16px 20px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.timeline-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.device-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.operation-type {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #f0f0f0;
  color: #606266;
}

.operation-info {
  background: #E6F7FF;
  color: #409EFF;
}

.operation-primary {
  background: #F0F9FF;
  color: #409EFF;
}

.operation-success {
  background: #F0F9EB;
  color: #67C23A;
}

.operation-warning {
  background: #FDF6EC;
  color: #E6A23C;
}

.timeline-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.operation-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.operator {
  font-size: 13px;
  color: #606266;
}

.time {
  font-size: 12px;
  color: #909399;
}

.status-info {
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.status-success {
  background: #F0F9EB;
  color: #67C23A;
}

.status-warning {
  background: #FDF6EC;
  color: #E6A23C;
}

.status-danger {
  background: #FEF0F0;
  color: #F56C6C;
}

.status-info {
  background: #F0F9FF;
  color: #409EFF;
}

/* 空状态 */
.empty-data {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

.stat-item-enter-active,
.stat-item-leave-active {
  transition: all 0.5s ease;
}

.stat-item-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.stat-item-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
  }
  
  .chart-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .welcome-content {
    padding: 24px 20px;
    border-radius: 12px;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .card-header {
    padding: 16px 20px 12px 20px !important;
  }
  
  .section-title {
    font-size: 16px;
  }
  
  .stat-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0 16px;
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .chart-wrapper {
    padding: 16px;
  }
  
  .activity-timeline {
    padding-left: 36px;
  }
  
  .timeline-left {
    left: -36px;
  }
  
  .timeline-icon {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .timeline-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .timeline-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .welcome-content {
    padding: 20px 16px;
  }
  
  .welcome-title {
    font-size: 20px;
  }
  
  .welcome-subtitle {
    font-size: 14px;
  }
  
  .card {
    margin-bottom: 16px;
  }
  
  .chart-container {
    gap: 16px;
  }
  
  .status-container {
    padding: 24px 16px;
  }
  
  .status-icon {
    font-size: 36px;
  }
  
  .status-title {
    font-size: 18px;
  }
}
</style>