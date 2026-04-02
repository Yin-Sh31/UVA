<template>
  <div class="demands-container">
    <h2>需求管理</h2>
    <el-table :data="demandsList" style="width: 100%">
      <el-table-column prop="demandId" label="需求ID"></el-table-column>
      <el-table-column prop="farmerName" label="农户姓名"></el-table-column>
      <el-table-column prop="landLocation" label="作业区域"></el-table-column>
      <el-table-column prop="landName" label="地块名称"></el-table-column>
      <el-table-column prop="cropType" label="作物类型"></el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button size="small" @click="viewDemandDetail(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 需求详情对话框 -->
    <el-dialog
      title="需求详情"
      v-model="dialogVisible"
      width="60%"
    >
      <div v-if="selectedDemand" class="demand-detail">
        <el-form label-position="left" label-width="100px">
          <el-form-item label="需求ID">
            <span>{{ selectedDemand.demandId }}</span>
          </el-form-item>
          <el-form-item label="农户姓名">
            <span>{{ selectedDemand.farmerName }}</span>
          </el-form-item>
          <el-form-item label="作业区域">
            <span>{{ selectedDemand.landLocation }}</span>
          </el-form-item>
          <el-form-item label="地块名称">
            <span>{{ selectedDemand.landName }}</span>
          </el-form-item>
          <el-form-item label="作物类型">
            <span>{{ selectedDemand.cropType }}</span>
          </el-form-item>
          <el-form-item label="订单类型">
            <span>{{ selectedDemand.orderTypeDesc }}</span>
          </el-form-item>
          <el-form-item label="作业面积">
            <span>{{ selectedDemand.landArea }} 亩</span>
          </el-form-item>
          <el-form-item label="期望时间">
            <span>{{ selectedDemand.expectedTime }}</span>
          </el-form-item>
          <el-form-item label="需求状态">
            <el-tag :type="getStatusType(selectedDemand.status)">{{ getStatusText(selectedDemand.status) }}</el-tag>
          </el-form-item>
          <el-form-item label="发布时间">
            <span>{{ selectedDemand.createTime }}</span>
          </el-form-item>
          <el-form-item label="需求描述">
            <span>{{ selectedDemand.inspectionPurpose || selectedDemand.pestType || '暂无描述' }}</span>
          </el-form-item>
          <el-form-item label="飞手信息" v-if="selectedDemand.flyerName">
            <span>{{ selectedDemand.flyerName }}</span>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      demandsList: [],
      dialogVisible: false,
      selectedDemand: null
    }
  },
  mounted() {
    this.getDemandsList()
  },
  methods: {
    async getDemandsList() {
      try {
        const response = await axios.get('/admin/demands/page', {
          params: {
            pageNum: 1,
            pageSize: 10
          }
        })
        if (response && response.code === 200) {
          this.demandsList = response.data.records || []
        }
      } catch (error) {
        console.error('获取需求列表失败:', error)
      }
    },
    async viewDemandDetail(demand) {
      try {
        const response = await axios.get(`/admin/demands/${demand.demandId}`)
        if (response && response.code === 200) {
          this.selectedDemand = response.data
          this.dialogVisible = true
        }
      } catch (error) {
        console.error('获取需求详情失败:', error)
      }
    },
    getStatusType(status) {
      const typeMap = {
        0: 'warning',
        1: 'primary',
        2: 'info',
        3: 'warning',
        4: 'success',
        5: 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        0: '待接取',
        1: '处理中',
        2: '作业中',
        3: '待确认',
        4: '已完成',
        5: '已取消'
      }
      return textMap[status] || '未知'
    }
  }
}
</script>

<style scoped>
.demands-container {
  padding: 20px;
}
.demand-detail {
  line-height: 1.8;
}
</style>