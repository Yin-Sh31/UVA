<template>
  <div class="system-management">
    <h2>系统管理</h2>
    <div class="system-content">
      <!-- 飞手接单控制 -->
      <div class="system-section">
        <h3>飞手接单控制</h3>
        <div class="control-item">
          <div class="control-label">
            <span>全局禁止飞手接单</span>
            <el-tooltip content="启用后，所有飞手将无法接取新订单" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          <el-switch
            v-model="isFlyerOrderBanned"
            active-text="已禁止"
            inactive-text="允许接单"
            @change="handleFlyerOrderChange"
            :loading="loading"
          ></el-switch>
        </div>
        <div v-if="isFlyerOrderBanned" class="warning-tip">
          <el-icon><WarningFilled /></el-icon>
          <span>警告：当前已禁止所有飞手接单，飞手将无法看到待接订单列表</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios'
import { QuestionFilled, WarningFilled } from '@element-plus/icons-vue'

export default {
  components: {
    QuestionFilled,
    WarningFilled
  },
  data() {
    return {
      isFlyerOrderBanned: false,
      loading: false
    }
  },
  mounted() {
    this.loadFlyerOrderStatus()
  },
  methods: {
    async loadFlyerOrderStatus() {
      try {
        const response = await axios.get('/admin/system/flyer-order-status')
        if (response.code === 200) {
          this.isFlyerOrderBanned = response.data
        }
      } catch (error) {
        console.error('获取飞手接单状态失败:', error)
        this.$message.error('获取状态失败')
      }
    },
    async handleFlyerOrderChange(value) {
      this.loading = true
      try {
        const response = await axios.put('/admin/system/flyer-order-status', null, {
          params: { banned: value }
        })
        if (response.code === 200) {
          this.$message.success('设置成功')
        } else {
          this.$message.error(response.message || '设置失败')
          this.isFlyerOrderBanned = !value // 恢复原来的状态
        }
      } catch (error) {
        console.error('设置飞手接单状态失败:', error)
        this.$message.error('设置失败')
        this.isFlyerOrderBanned = !value // 恢复原来的状态
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.system-management {
  padding: 20px;
}

.system-management h2 {
  color: #333;
  margin-bottom: 20px;
}

.system-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.system-section {
  margin-bottom: 30px;
}

.system-section h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 16px;
}

.control-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.control-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.warning-tip {
  margin-top: 10px;
  padding: 10px;
  background-color: #fff7e6;
  border: 1px solid #ffd591;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #ff6b35;
}
</style>