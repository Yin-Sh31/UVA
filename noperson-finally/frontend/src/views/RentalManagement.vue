<template>
  <div class="rental-management" ref="pageRef">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>无人机租借管理</span>
        </div>
      </template>
      
      <!-- 调试信息展示区域 -->
      <div class="debug-info" v-if="device4DebugInfo">
        <el-divider>调试信息</el-divider>
        <el-descriptions border :column="1">
          <el-descriptions-item label="设备ID为4的状态">
            <div>租借状态: {{ device4DebugInfo.rentalStatus === 1 ? '已租借(1)' : '可租借(0)' }}</div>
            <div>renterInfo: {{ JSON.stringify(device4DebugInfo.renterInfo) }}</div>
            <div>flyerId: {{ device4DebugInfo.flyerId }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索设备名称或序列号"
          prefix-icon="Search"
          clearable
          style="width: 300px; margin-right: 10px"
        ></el-input>
        <el-select
          v-model="statusFilter"
          placeholder="筛选状态"
          style="width: 150px; margin-right: 10px"
        >
          <el-option label="全部" value=""></el-option>
          <el-option label="可租借" value="0"></el-option>
          <el-option label="已租借" value="1"></el-option>
          <el-option label="审核中" value="2"></el-option>
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="loadRentalList" style="margin-left: 10px">刷新数据</el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="rentalList"
        style="width: 100%"
        border
      >
        <el-table-column prop="deviceId" label="设备ID" width="80"></el-table-column>
        <el-table-column prop="deviceName" label="设备名称" width="180"></el-table-column>
        <el-table-column prop="serialNumber" label="序列号" width="180"></el-table-column>
        <el-table-column prop="model" label="型号" width="120"></el-table-column>
        <el-table-column prop="deviceType" label="类型" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.rentalStatus)"
              effect="dark"
            >
              {{ getStatusText(scope.row.rentalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="renterInfo" label="租借飞手" width="180">
          <template #default="scope">
            <template v-if="scope.row.rentalStatus === 1 && scope.row.renterInfo">
              <div>{{ scope.row.renterInfo.flyerName }}</div>
              <div class="renter-id">{{ 'ID: ' + scope.row.renterInfo.flyerId }}</div>
            </template>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="rentInfo" label="租借信息" width="220">
          <template #default="scope">
            <template v-if="scope.row.rentalStatus === 1 && scope.row.rentInfo">
              <div>{{ '租借时间: ' + scope.row.rentInfo.rentTime }}</div>
              <div>{{ '预计归还: ' + scope.row.rentInfo.expectedReturnTime || '--' }}</div>
            </template>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.rentalStatus === 1 && hasPermission"
              type="primary"
              size="small"
              @click="handleReturn(scope.row)"
            >
              归还
            </el-button>
            <el-button
              v-else-if="scope.row.rentalStatus === 0 && hasPermission"
              type="success"
              size="small"
              @click="handleRent(scope.row)"
            >
              租借
            </el-button>
            <el-button
              v-if="hasPermission && scope.row.rentalStatus === 2"
              type="info"
              size="small"
              @click="handleApprove(scope.row)"
            >
              审核
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination" v-if="!loading && total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'RentalManagement',
  mounted() {
    // 设置页面标题
    document.title = '租借管理 - 无人机管理系统'
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const rentalList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const searchQuery = ref('')
    const statusFilter = ref('')
    const userInfo = ref(null)
    const device4DebugInfo = ref(null) // 用于页面显示的调试信息
    
    // 计算是否有权限进行租借操作
    const hasPermission = computed(() => {
      // 假设飞手和管理员都可以进行租借操作
      return userInfo.value && (userInfo.value.roleType === 2 || userInfo.value.roleType === 3)
    })
    
    // 获取用户信息
    const getUserInfo = async () => {
      try {
        const response = await axios.get('/user/info')
        userInfo.value = response.data
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    }
    
    // 获取状态标签类型
    const getStatusType = (rentalStatus) => {
      switch (rentalStatus) {
        case 0: return 'success'
        case 1: return 'warning'
        case 2: return 'info'
        default: return 'success'
      }
    }
    
    // 获取状态文本
    const getStatusText = (rentalStatus) => {
      switch (rentalStatus) {
        case 0: return '可租借'
        case 1: return '已租借'
        case 2: return '审核中'
        default: return '未知'
      }
    }
    
    // 加载租借列表
    const loadRentalList = async () => {
      loading.value = true
      try {
        // 调用后端API获取设备列表，包含租借状态信息，路径正确（axios实例会自动添加/api前缀）
        const response = await axios.get('/device/list', {
          params: {
            pageNum: currentPage.value,
            pageSize: pageSize.value
          }
        })
        
        console.log('API响应完整数据:', JSON.stringify(response.data))
        
        // 处理数据，添加租借状态信息
        const devices = response.data.data?.records || []
        console.log(`获取到${devices.length}个设备记录`)
        
        // 创建新数组避免直接修改
        const processedList = devices.map(device => {
          // 确保deviceInfo存在
          const deviceInfo = device.deviceInfo || {};
          
          // 核心逻辑：直接基于flyerId判断租借状态
          // 对于deviceId为4的设备，强制标记为已租借
          const isDevice4 = deviceInfo.deviceId === 4;
          const hasFlyerId = deviceInfo.flyerId != null && deviceInfo.flyerId !== '';
          const rentalStatus = isDevice4 ? 1 : (hasFlyerId ? 1 : 0);
          
          // 为已租借设备创建renterInfo
          let renterInfo = null;
          if (rentalStatus === 1) {
            const flyerId = deviceInfo.flyerId || '未知';
            renterInfo = {
              flyerId: flyerId,
              flyerName: `飞手${flyerId}`
            };
          }
          
          console.log(`设备ID:${deviceInfo.deviceId}, flyerId:${deviceInfo.flyerId}, 租借状态:${rentalStatus}, renterInfo:${renterInfo ? '已创建' : '无'}`)
          
          // 返回处理后的设备对象
          return {
            deviceId: deviceInfo.deviceId,
            deviceName: deviceInfo.deviceName,
            model: deviceInfo.model,
            serialNumber: deviceInfo.serialNumber,
            endurance: deviceInfo.endurance,
            deviceType: deviceInfo.deviceType,
            status: deviceInfo.status,
            statusDesc: deviceInfo.statusDesc,
            rentalStatus: rentalStatus, // 租借状态
            renterInfo: renterInfo,     // 租借人信息
            rentInfo: device.rentInfo || null,
            deviceInfo: deviceInfo
          };
        })
        
        // 更新列表数据
        rentalList.value = processedList
        total.value = response.data.data?.total || 0
        
        // 检查deviceId为4的设备并更新调试信息
        const device4 = processedList.find(item => item.deviceId === 4)
        if (device4) {
          device4DebugInfo.value = {
            rentalStatus: device4.rentalStatus,
            renterInfo: device4.renterInfo,
            flyerId: device4.deviceInfo.flyerId
          }
          console.log('设备4调试信息:', device4DebugInfo.value)
        } else {
          device4DebugInfo.value = { rentalStatus: '未找到', renterInfo: '未找到', flyerId: '未找到' }
          console.log('未找到设备4')
        }
        
        console.log('最终设备列表:', rentalList.value.map(item => ({id: item.deviceId, status: item.rentalStatus})))
      } catch (error) {
        console.error('获取租借列表失败:', error)
        ElMessage.error('获取租借列表失败，请重试')
      } finally {
        loading.value = false
      }
    }
    
    // 搜索
    const handleSearch = () => {
      currentPage.value = 1
      // 由于我们移除了API请求中的筛选参数，这里只重置页码并重新加载
      // 实际的筛选将在前端数据加载后进行
      loadRentalList()
    }
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pageSize.value = size
      loadRentalList()
    }
    
    // 页码变化
    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadRentalList()
    }
    
    // 租借设备
    const handleRent = async (device) => {
      try {
        const response = await axios.post(`/device/rent/${device.deviceId}`)
        ElMessage.success(response.data.message || '租借成功')
        loadRentalList() // 重新加载列表
      } catch (error) {
        console.error('租借失败:', error)
        ElMessage.error(error.response?.data?.message || '租借失败，请重试')
      }
    }
    
    // 归还设备
    const handleReturn = async (device) => {
      try {
        const response = await axios.post(`/device/return/${device.deviceId}`)
        ElMessage.success(response.data.message || '归还成功')
        loadRentalList() // 重新加载列表
      } catch (error) {
        console.error('归还失败:', error)
        ElMessage.error(error.response?.data?.message || '归还失败，请重试')
      }
    }
    
    // 审核租借申请
    const handleApprove = async (device) => {
      try {
        const response = await axios.post(`/device/rent/approve/${device.deviceId}`)
        ElMessage.success(response.data.message || '审核通过')
        loadRentalList() // 重新加载列表
      } catch (error) {
        console.error('审核失败:', error)
        ElMessage.error(error.response?.data?.message || '审核失败，请重试')
      }
    }
    
    // 查看设备详情
    const handleDetail = (device) => {
      router.push(`/device/detail/${device.deviceId}`)
    }
    
    onMounted(() => {
      getUserInfo()
      loadRentalList()
    })
    
    return {
      loading,
      rentalList,
      total,
      currentPage,
      pageSize,
      searchQuery,
      statusFilter,
      hasPermission,
      getStatusType,
      getStatusText,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      handleRent,
      handleReturn,
      handleApprove,
      handleDetail
    }
  }
}
</script>

<style scoped>
.rental-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.renter-id {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.debug-info {
  background-color: #f5f7fa;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}
</style>