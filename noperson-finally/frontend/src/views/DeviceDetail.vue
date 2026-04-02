<template>
  <div class="device-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">设备详情</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item><a href="/dashboard">首页</a></el-breadcrumb-item>
        <el-breadcrumb-item><a href="/devices">设备管理</a></el-breadcrumb-item>
        <el-breadcrumb-item>设备详情</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="device-detail-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <div class="device-main-info">
            <h2 class="device-name">{{ device.deviceName }}</h2>
            <div class="device-model">{{ device.deviceModel }}</div>
          </div>
          <el-tag :type="getStatusTagType(device.status)" class="status-badge">
            {{ getStatusText(device.status) }}
          </el-tag>
        </div>
      </template>
      
      <!-- 设备图片展示区域 -->
      <div class="device-image-section" style="margin: 20px 0; text-align: center;">
        <el-image 
          :src="getDeviceImage(device.picture)"
          fit="contain" 
          :preview-src-list="[getDeviceImage(device.picture)]"
          class="device-detail-image"
          lazy
        />
      </div>

      <!-- 设备基本信息 -->
      <el-descriptions :column="2" border class="device-descriptions">
        <el-descriptions-item label="设备ID">{{ device.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ device.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ device.deviceType }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ device.brand }}</el-descriptions-item>
        <el-descriptions-item label="最大载重(kg)">{{ device.maxLoad }}</el-descriptions-item>
        <el-descriptions-item label="续航时间(min)">{{ device.endurance }}</el-descriptions-item>
        <el-descriptions-item label="制造商">{{ device.manufacturer }}</el-descriptions-item>
        <el-descriptions-item label="购买日期">{{ formatDate(device.purchaseDate) }}</el-descriptions-item>
        <el-descriptions-item label="设备位置">{{ device.location }}</el-descriptions-item>
        <el-descriptions-item label="坐标">
          <div class="location-info">
            <span class="coordinates">经度: {{ device.longitude }}</span>
            <span class="coordinates">纬度: {{ device.latitude }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="最后在线时间">{{ device.lastOnlineTime }}</el-descriptions-item>
        <el-descriptions-item label="飞行时长">{{ device.flightHours }}小时</el-descriptions-item>
        <el-descriptions-item label="飞行次数">{{ device.flightCount }}次</el-descriptions-item>
        <el-descriptions-item label="备注">{{ device.remark }}</el-descriptions-item>
      </el-descriptions>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button @click="router.push(`/device/${device.deviceId}/edit`)" type="primary">
          编辑设备
        </el-button>
        <el-button @click="router.push('/devices')">
          返回列表
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '../utils/axios' // 使用项目中配置的axios实例
import { ElMessage } from 'element-plus'

export default {
  name: 'DeviceDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const loading = ref(true)
    const device = ref({
      deviceId: '',
      deviceName: '',
      deviceModel: '',
      deviceType: '',
      brand: '',
      serialNumber: '',
      status: 0,
      purchaseDate: '',
      location: '',
      longitude: '',
      latitude: '',
      lastOnlineTime: '',
      flightHours: 0,
      flightCount: 0,
      maxLoad: 0,
      endurance: 0,
      manufacturer: '',
      remark: '',
      picture: ''
    })

    // 获取设备详情
    const fetchDeviceDetail = async () => {
      const deviceId = route.params.deviceId
      loading.value = true
      
      try {
        // 检查并设置token
        const token = localStorage.getItem('token')
        console.log('当前localStorage中的token:', token ? '存在' : '不存在')
        if (!token) {
          console.log('未检测到token，设置mock-token')
          localStorage.setItem('token', 'mock-token')
        }
        
        // 调用实际的API
        console.log(`准备调用设备详情API: /device/detail/${deviceId}`)
        const response = await axios.get(`/device/detail/${deviceId}`)
        
        // 处理嵌套的数据结构
        const apiData = response.data || {}
        const deviceInfo = apiData.deviceInfo || apiData
        
        // 映射数据到设备对象
        device.value = {
          deviceId: deviceInfo.device_id || deviceInfo.deviceId || deviceId,
          deviceName: deviceInfo.device_name || '未知设备',
          deviceModel: deviceInfo.model || '未知型号',
          deviceType: deviceInfo.deviceType || deviceInfo.device_type || '未知类型',
          brand: deviceInfo.brand || '',
          serialNumber: deviceInfo.serialNumber || deviceInfo.device_no || '',
          status: deviceInfo.status || 0,
          purchaseDate: deviceInfo.purchase_time ? 
            (typeof deviceInfo.purchase_time === 'string' ? 
              deviceInfo.purchase_time.split(' ')[0] : 
              deviceInfo.purchase_time.toLocaleDateString()) : '',
          location: '', // 后端可能没有提供位置信息
          longitude: '',
          latitude: '',
          lastOnlineTime: deviceInfo.last_maintain_time || deviceInfo.lastMaintainTime || '',
          flightHours: deviceInfo.working_hours || 0,
          flightCount: 0, // 后端可能没有提供飞行次数
          maxLoad: deviceInfo.max_load || deviceInfo.maxLoad || 0,
          endurance: deviceInfo.enduranceTime || deviceInfo.endurance || 0,
          manufacturer: deviceInfo.manufacturer || '',
          remark: '',
          picture: deviceInfo.picture || ''
        }
      } catch (error) {
        console.error('获取设备详情失败:', error)
        console.error('错误类型:', error.name)
        console.error('错误消息:', error.message)
        console.error('响应状态:', error.response ? error.response.status : '无响应')
        console.error('响应数据:', error.response ? error.response.data : '无数据')
        ElMessage.error('获取设备详情失败，使用模拟数据显示')
        
        // 使用模拟数据作为fallback
        device.value = {
          deviceId: deviceId,
          deviceName: '示例无人机',
          deviceModel: 'Mavic 3',
          deviceType: '多旋翼无人机',
          brand: 'DJI',
          serialNumber: 'SN1234567890',
          status: 1,
          purchaseDate: '2023-06-15',
          location: '北京市海淀区',
          longitude: '116.3974',
          latitude: '39.9093',
          lastOnlineTime: '2024-01-20 15:30:45',
          flightHours: 120,
          flightCount: 85,
          maxLoad: 2.0,
          endurance: 45,
          manufacturer: '大疆创新',
          remark: '设备运行状态良好',
          picture: ''
        }
      } finally {
        loading.value = false
      }
    }

    // 格式化日期
    const formatDate = (date) => {
      if (!date) return ''
      if (typeof date === 'string') {
        return date.split(' ')[0]
      }
      return date.toLocaleDateString()
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        0: '未激活',
        1: '正常',
        2: '维护中',
        3: '故障',
        4: '停用'
      }
      return statusMap[status] || '未知'
    }

    // 获取状态标签类型
    const getStatusTagType = (status) => {
      const typeMap = {
        0: 'info',
        1: 'success',
        2: 'warning',
        3: 'danger',
        4: 'info'
      }
      return typeMap[status] || 'info'
    }
    
    // 获取设备图片URL，处理默认图片逻辑
    const getDeviceImage = (picture) => {
      // 如果有图片URL且不为空，返回原始URL
      if (picture && picture.trim()) {
        // 检查是否是完整URL，如果不是则使用相对路径
        if (picture.startsWith('http://') || picture.startsWith('https://')) {
          return picture
        }
        // 使用相对路径，通过前端代理访问图片资源
        return picture
      }
      // 如果没有图片，返回默认图片
      return 'https://via.placeholder.com/300x300?text=设备+图片+未设置'
    }

    onMounted(() => {
      fetchDeviceDetail()
    })

    return {
      device,
      loading,
      router,
      formatDate,
      getStatusText,
      getStatusTagType,
      getDeviceImage
    }
  }
}
</script>

<style scoped>
.device-detail-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e6eaf0 100%);
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
  position: relative;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #303133, #606266);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 设备详情卡片 */
.device-detail-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: #ffffff;
}

.device-detail-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f0f2f5 100%);
  border-bottom: 1px solid #f0f0f0;
}

.device-main-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-name {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.device-model {
  font-size: 16px;
  color: #606266;
}

.status-badge {
  border-radius: 12px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
}

/* 设备图片样式 */
.device-detail-image {
  width: 300px;
  height: 300px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

/* 设备描述表格 */
.device-descriptions {
  margin-top: 20px;
}

.device-descriptions :deep(.el-descriptions-item__label) {
  color: #303133;
  font-weight: 600;
  font-size: 14px;
  padding: 16px 12px;
}

.device-descriptions :deep(.el-descriptions-item__content) {
  color: #606266;
  font-size: 14px;
  padding: 16px 12px;
}

.location-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coordinates {
  font-size: 13px;
  color: #909399;
}

/* 操作按钮 */
.action-buttons {
  margin-top: 30px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 20px 24px 0;
}

.action-buttons :deep(.el-button) {
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-buttons :deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.action-buttons :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66B1FF, #409EFF);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .device-detail-page {
    padding: 12px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px 20px;
  }
  
  .device-name {
    font-size: 20px;
  }
  
  .device-descriptions :deep(.el-descriptions) {
    :deep(.el-descriptions-item) {
      padding: 12px 8px;
    }
    :deep(.el-descriptions-item__label) {
      font-size: 13px;
    }
    :deep(.el-descriptions-item__content) {
      font-size: 13px;
    }
  }
  
  .action-buttons {
    flex-direction: column;
    padding: 16px 20px 0;
  }
  
  .action-buttons :deep(.el-button) {
    width: 100%;
  }
}
</style>