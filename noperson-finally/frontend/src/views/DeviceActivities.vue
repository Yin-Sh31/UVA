<template>
  <div class="device-activities-page">
    <div class="page-header">
      <h2>设备动态记录</h2>
    </div>

    <div class="search-filters">
      <el-form :inline="true" :model="searchParams" class="demo-form-inline">
        <el-form-item label="设备ID">
          <el-input v-model="searchParams.deviceId" placeholder="请输入设备ID" clearable></el-input>
        </el-form-item>
        
        <el-form-item label="租借状态">
          <el-select v-model="searchParams.rentalStatus" placeholder="请选择租借状态" clearable>
            <el-option label="租借中" :value="1"></el-option>
            <el-option label="已归还" :value="2"></el-option>
            <el-option label="已取消" :value="3"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="activities-table">
      <el-table :data="activitiesList" style="width: 100%">
        <el-table-column prop="rentalId" label="记录ID" width="100"></el-table-column>
        
        <el-table-column prop="deviceId" label="设备ID" width="100">
          <template #default="scope">
            <el-button type="text" @click="viewDevice(scope.row.deviceId)">{{ scope.row.deviceId }}</el-button>
          </template>
        </el-table-column>
        
        <el-table-column prop="flyerId" label="飞手ID" width="100"></el-table-column>
        
        <el-table-column prop="ownerId" label="机主ID" width="100"></el-table-column>
        
        <el-table-column prop="rentalStatus" label="租借状态" width="100">
          <template #default="scope">
            <el-tag :type="getRentalStatusTag(scope.row.rentalStatus)">
              {{ getRentalStatusText(scope.row.rentalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="getPaymentStatusTag(scope.row.paymentStatus)">
              {{ getPaymentStatusText(scope.row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="rentalStartTime" label="租借开始时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.rentalStartTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="rentalEndTime" label="租借结束时间" width="180">
          <template #default="scope">
            {{ scope.row.rentalEndTime ? formatDateTime(scope.row.rentalEndTime) : '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="rentalAmount" label="租借费用" width="100">
          <template #default="scope">
            {{ scope.row.rentalAmount || '-' }}
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="remark" label="备注"></el-table-column>
      </el-table>
    </div>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'

export default {
  name: 'DeviceActivities',
  setup() {
    const router = useRouter()
    const activitiesList = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const loading = ref(false)

    const searchParams = reactive({
      deviceId: '',
      rentalStatus: ''
    })

    // 获取设备租赁记录列表
    const fetchActivities = async () => {
      loading.value = true
      try {
        const params = {
          pageNum: currentPage.value,
          pageSize: pageSize.value
        }
        
        // 如果有搜索条件，可以在这里添加到params中
        // 目前后端接口还不支持按条件筛选，后面可以根据需要扩展

        const response = await axios.get('/rental/dynamics', { params })
        // 当响应拦截器已经处理了code不等于200的情况，这里可以直接使用响应数据
        activitiesList.value = response.data.records || []
        total.value = response.data.total || 0
        console.log('获取设备租赁记录成功，共', total.value, '条记录')
      } catch (error) {
        console.error('获取设备租赁记录异常:', error)
      } finally {
        loading.value = false
      }
    }

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1
      fetchActivities()
    }

    // 重置搜索
    const resetSearch = () => {
      searchParams.deviceId = ''
      searchParams.rentalStatus = ''
      currentPage.value = 1
      fetchActivities()
    }

    // 分页大小变更
    const handleSizeChange = (size) => {
      pageSize.value = size
      fetchActivities()
    }

    // 当前页码变更
    const handleCurrentChange = (current) => {
      currentPage.value = current
      fetchActivities()
    }

    // 查看设备详情
    const viewDevice = (deviceId) => {
      router.push(`/device/detail/${deviceId}`)
    }

    // 获取租借状态标签类型
    const getRentalStatusTag = (status) => {
      const tagTypes = {
        1: 'danger',  // 租借中 - 红色
        2: 'success', // 已归还 - 绿色
        3: 'warning'  // 已取消 - 黄色
      }
      return tagTypes[status] || 'info'
    }

    // 获取租借状态文本
    const getRentalStatusText = (status) => {
      const texts = {
        1: '租借中',
        2: '已归还',
        3: '已取消'
      }
      return texts[status] || '未知'
    }

    // 获取支付状态标签类型
    const getPaymentStatusTag = (status) => {
      const tagTypes = {
        0: 'warning',  // 未支付 - 黄色
        1: 'success',  // 已支付 - 绿色
        2: 'danger'    // 支付失败 - 红色
      }
      return tagTypes[status] || 'info'
    }

    // 获取支付状态文本
    const getPaymentStatusText = (status) => {
      const texts = {
        0: '未支付',
        1: '已支付',
        2: '支付失败'
      }
      return texts[status] || '未知'
    }

    // 格式化日期时间
    const formatDateTime = (timeStr) => {
      if (!timeStr) return '-'
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }

    onMounted(() => {
      fetchActivities()
    })

    return {
      activitiesList,
      currentPage,
      pageSize,
      total,
      loading,
      searchParams,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      viewDevice,
      getRentalStatusTag,
      getRentalStatusText,
      getPaymentStatusTag,
      getPaymentStatusText,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.device-activities-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
  font-weight: 500;
}

.search-filters {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.activities-table {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.text-sm {
  font-size: 12px;
}

.text-gray-500 {
  color: #909399;
}
</style>