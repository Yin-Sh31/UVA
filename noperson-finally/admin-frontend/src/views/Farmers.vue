<template>
  <div class="farmers-container">
    <h2>农户管理</h2>
    <el-card>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索农户姓名或电话" style="width: 300px; margin-right: 10px;"></el-input>
        <el-button type="primary" @click="getFarmersList">搜索</el-button>
      </div>
      <el-table :data="farmersList" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
        <el-table-column prop="username" label="用户名"></el-table-column>
        <el-table-column prop="realName" label="真实姓名"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额"></el-table-column>
        <el-table-column prop="createTime" label="创建时间"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewFarmerDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 飞手详情对话框 -->
    <el-dialog v-model="dialogVisible" title="飞手详情" width="600px">
      <el-descriptions :column="1" v-if="selectedFarmer">
        <el-descriptions-item label="用户ID">{{ selectedFarmer.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ selectedFarmer.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ selectedFarmer.realName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ selectedFarmer.phone }}</el-descriptions-item>
        <el-descriptions-item label="技能等级">{{ selectedFarmer.skillLevel }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(selectedFarmer.auditStatus)">
            {{ getAuditStatusText(selectedFarmer.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ selectedFarmer.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      farmersList: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      selectedFarmer: null,
      demandsList: []
    }
  },
  mounted() {
    this.getFarmersList()
  },
  methods: {
    async getFarmersList() {
      try {
        // 调用后端管理员API获取农户列表
        const response = await axios.get('/admin/farmers/page', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            keyword: this.searchKeyword || ''
          }
        })
        if (response.code === 200) {
          this.farmersList = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error(response.message || '获取农户列表失败')
          // 如果获取失败，使用空数组
          this.farmersList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取农户列表失败:', error)
        this.$message.error('获取农户列表失败，请稍后重试')
        // 错误情况下使用空数组
        this.farmersList = []
        this.total = 0
      }
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getFarmersList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getFarmersList()
    },

    viewFarmerDetail(farmer) {
      // 直接使用表格中的数据显示详情
      this.selectedFarmer = farmer
      this.dialogVisible = true
    }
  }
}
</script>

<style scoped>
.farmers-container {
  height: 100%;
}

.farmers-container h2 {
  margin-bottom: 20px;
  color: #303133;
}

.search-bar {
  display: flex;
  align-items: center;
}
</style>