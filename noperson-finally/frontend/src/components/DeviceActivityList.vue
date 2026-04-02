<template>
  <div class="device-activity-container">
    <div class="activity-header">
      <h3>最近设备动态</h3>
      <router-link to="/device-activities" class="view-all-link">查看全部</router-link>
    </div>
    
    <div class="activity-list" v-if="activities.length > 0">
      <div v-for="activity in activities" :key="activity.activityId" class="activity-item">
        <div class="activity-icon" :class="getActivityIconClass(activity.activityType)">
          <i :class="getActivityIcon(activity.activityType)"></i>
        </div>
        <div class="activity-content">
          <div class="activity-title">{{ activity.device?.deviceName || activity.device?.model || '设备' }}</div>
          <div class="activity-desc">{{ activity.activityDesc }}</div>
          <div class="activity-meta">
            <span class="activity-time">{{ formatTime(activity.createTime) }}</span>
            <span class="activity-operator" v-if="activity.operatorName">by {{ activity.operatorName }}</span>
          </div>
        </div>
        <div class="activity-status" v-if="activity.afterStatus" :class="getStatusClass(activity.afterStatus)">
          {{ getStatusText(activity.afterStatus) }}
        </div>
      </div>
    </div>
    
    <div class="empty-state" v-else>
      <i class="el-icon-document-checked"></i>
      <p>暂无设备动态</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from '../utils/axios'

export default {
  name: 'DeviceActivityList',
  props: {
    limit: {
      type: Number,
      default: 10
    }
  },
  setup(props) {
    const activities = ref([])
    const loading = ref(false)

    // 获取设备动态
    const fetchActivities = async () => {
      loading.value = true
      try {
        const response = await axios.get('/device-activities/my-related', {
          params: {
            limit: props.limit
          }
        })
        // 当响应拦截器已经处理了code不等于200的情况，这里可以直接使用响应数据
        activities.value = response.data || []
        console.log('获取设备动态成功，共', activities.value.length, '条记录')
      } catch (error) {
        console.error('获取设备动态异常:', error)
      } finally {
        loading.value = false
      }
    }

    // 获取动态图标类名
    const getActivityIconClass = (activityType) => {
      const iconClasses = {
        1: 'status-online', // 设备上线
        2: 'status-offline', // 设备下线
        3: 'status-maintain', // 设备维护
        4: 'status-return', // 设备归还
        5: 'status-rent', // 设备租借
        6: 'status-change' // 状态变更
      }
      return iconClasses[activityType] || 'status-default'
    }

    // 获取动态图标
    const getActivityIcon = (activityType) => {
      const icons = {
        1: 'el-icon-camera-solid',
        2: 'el-icon-video-camera-off',
        3: 'el-icon-setting',
        4: 'el-icon-refresh-left',
        5: 'el-icon-refresh-right',
        6: 'el-icon-switch-button'
      }
      return icons[activityType] || 'el-icon-info'
    }

    // 获取状态类名
    const getStatusClass = (status) => {
      const statusClasses = {
        1: 'status-normal', // 正常
        2: 'status-maintaining', // 维护中
        0: 'status-disabled' // 停用
      }
      return statusClasses[status] || ''
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusTexts = {
        1: '正常',
        2: '维护中',
        0: '停用'
      }
      return statusTexts[status] || ''
    }

    // 格式化时间
    const formatTime = (timeStr) => {
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) { // 小于1分钟
        return '刚刚'
      } else if (diff < 3600000) { // 小于1小时
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) { // 小于1天
        return `${Math.floor(diff / 3600000)}小时前`
      } else if (diff < 604800000) { // 小于7天
        return `${Math.floor(diff / 86400000)}天前`
      } else {
        return date.toLocaleDateString('zh-CN')
      }
    }

    onMounted(() => {
      fetchActivities()
    })

    return {
      activities,
      loading,
      getActivityIconClass,
      getActivityIcon,
      getStatusClass,
      getStatusText,
      formatTime
    }
  }
}
</script>

<style scoped>
.device-activity-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.activity-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
  font-weight: 500;
}

.view-all-link {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s;
}

.view-all-link:hover {
  color: #66b1ff;
}

.activity-list {
  space-y: 15px;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  flex-shrink: 0;
  font-size: 20px;
}

.status-online {
  background: #e6f7ff;
  color: #1890ff;
}

.status-offline {
  background: #f5f5f5;
  color: #8c8c8c;
}

.status-maintain {
  background: #fff7e6;
  color: #fa8c16;
}

.status-return {
  background: #f6ffed;
  color: #52c41a;
}

.status-rent {
  background: #fff1f0;
  color: #f5222d;
}

.status-change {
  background: #f0f5ff;
  color: #722ed1;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.activity-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
  line-height: 1.5;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #999;
}

.activity-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-left: 15px;
  flex-shrink: 0;
}

.status-normal {
  background: #f0f9ff;
  color: #009688;
}

.status-maintaining {
  background: #fff7e6;
  color: #fa8c16;
}

.status-disabled {
  background: #f5f5f5;
  color: #8c8c8c;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #e0e0e0;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}
</style>