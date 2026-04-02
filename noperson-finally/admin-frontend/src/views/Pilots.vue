<template>
  <div class="pilots-container">
    <h2>飞手管理</h2>
    <el-card>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索飞手姓名或电话" style="width: 300px; margin-right: 10px;"></el-input>
        <el-select v-model="statusFilter" placeholder="状态" style="width: 150px; margin-right: 10px;">
          <el-option label="全部" value=""></el-option>
          <el-option label="待审核" value="pending"></el-option>
          <el-option label="已通过" value="approved"></el-option>
          <el-option label="已拒绝" value="rejected"></el-option>
        </el-select>
        <el-button type="primary" @click="searchPilots">搜索</el-button>
      </div>
      <el-table :data="pilotsList" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="flyerId" label="ID" width="80"></el-table-column>
        <el-table-column prop="userName" label="姓名"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column prop="licenseType" label="执照类型"></el-table-column>
        <el-table-column prop="licenseNo" label="执照号码"></el-table-column>
        <el-table-column prop="skillLevel" label="技能等级"></el-table-column>
        <el-table-column prop="pricePerAcre" label="亩单价(元)"></el-table-column>
        <el-table-column prop="experience" label="飞行经验(年)"></el-table-column>
        <el-table-column prop="location" label="所在地"></el-table-column>
        <el-table-column prop="createTime" label="注册时间"></el-table-column>
        <el-table-column prop="auditStatus" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewPilotDetail(scope.row)">查看</el-button>
            <template v-if="scope.row.auditStatus === 0">
              <el-button type="success" size="small" @click="approvePilot(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectPilot(scope.row)">拒绝</el-button>
            </template>
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
    <el-dialog v-model="dialogVisible" title="飞手详情" width="800px">
      <el-descriptions :column="2" v-if="selectedPilot">
        <!-- 基本信息 -->
        <el-descriptions-item label="姓名" :span="1">{{ selectedPilot.userName }}</el-descriptions-item>
        <el-descriptions-item label="电话" :span="1">{{ selectedPilot.phone }}</el-descriptions-item>
        
        <el-descriptions-item label="头像" :span="2">
          <el-image 
            v-if="selectedPilot.avatar" 
            :src="selectedPilot.avatar" 
            style="width: 100px; height: 100px; border-radius: 50%;"
          />
          <span v-else>暂无头像</span>
        </el-descriptions-item>
        
        <!-- 飞手专业信息 -->
        <el-descriptions-item label="执照类型">{{ selectedPilot.licenseType }}</el-descriptions-item>
        <el-descriptions-item label="执照号码">{{ selectedPilot.licenseNo }}</el-descriptions-item>
        <el-descriptions-item label="保险号码">{{ selectedPilot.insuranceNo || '未提供' }}</el-descriptions-item>
        <el-descriptions-item label="技能等级">{{ selectedPilot.skillLevel || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="亩单价(元)">{{ selectedPilot.pricePerAcre || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="飞行经验">{{ selectedPilot.experience || '0' }} 年</el-descriptions-item>
        <el-descriptions-item label="所在地">{{ selectedPilot.location || '未提供' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selectedPilot.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getStatusType(selectedPilot.auditStatus)">
            {{ getStatusText(selectedPilot.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核备注">{{ selectedPilot.auditRemark || '无' }}</el-descriptions-item>
        
        <!-- 执照证明 -->
        <el-descriptions-item label="执照证明" :span="2">
          <el-image 
            v-if="selectedPilot.licenseUrl" 
            :src="selectedPilot.licenseUrl" 
            style="width: 300px; height: 200px;"
            :preview-src-list="[selectedPilot.licenseUrl]"
          />
          <span v-else>暂无执照证明</span>
        </el-descriptions-item>
        
        <!-- 保险证明 -->
        <el-descriptions-item label="保险证明" :span="2">
          <el-image 
            v-if="selectedPilot.insuranceUrl && selectedPilot.insuranceUrl.trim() !== ''" 
            :src="selectedPilot.insuranceUrl" 
            style="width: 300px; height: 200px; margin-top: 10px;"
            :preview-src-list="[selectedPilot.insuranceUrl]"
          />
          <span v-else>暂无保险证明</span>
        </el-descriptions-item>
        
        <!-- 其他信息 -->
        <el-descriptions-item label="用户ID" :span="1">{{ selectedPilot.userId }}</el-descriptions-item>
        <el-descriptions-item label="飞手ID" :span="1">{{ selectedPilot.flyerId }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系人" :span="1">{{ selectedPilot.emergencyContact || '未提供' }}</el-descriptions-item>
        <el-descriptions-item label="紧急联系电话" :span="1">{{ selectedPilot.emergencyPhone || '未提供' }}</el-descriptions-item>
        <el-descriptions-item label="自我介绍" :span="2">{{ selectedPilot.introduction || '暂无介绍' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      pilotsList: [],
      searchKeyword: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      selectedPilot: null
    }
  },
  mounted() {
    this.getPilotsList()
  },
  methods: {
    async getPilotsList() {
      try {
        // 转换状态筛选参数
        const auditStatus = this.statusFilter === 'pending' ? 0 : 
                          this.statusFilter === 'approved' ? 1 : 
                          this.statusFilter === 'rejected' ? 2 : null
        
        // 调用后端API获取飞手列表
        const response = await axios.get('/admin/flyers/page', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            auditStatus: auditStatus,
            keyword: this.searchKeyword
          }
        })
        
        if (response.code === 200) {
          this.pilotsList = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error(response.message || '获取飞手列表失败')
          this.pilotsList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取飞手列表失败:', error)
        this.$message.error('获取飞手列表失败，请稍后重试')
        this.pilotsList = []
        this.total = 0
      }
    },
    searchPilots() {
      this.getPilotsList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getPilotsList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getPilotsList()
    },
    viewPilotDetail(pilot) {
      this.selectedPilot = pilot
      this.dialogVisible = true
    },
    async approvePilot(pilot) {
      console.log('飞手审核数据:', pilot);
      this.$confirm(`确定要通过飞手 ${pilot.userName} 的审核吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API进行审核
          const response = await axios.post('/admin/flyers/audit', {
            targetId: pilot.userId, // 修改为使用userId而不是flyerId
            roleType: 2, // 2-飞手
            auditResult: 1, // 1-通过
            auditRemark: '审核通过'
          })
          
          if (response.code === 200) {
            this.$message.success('审核通过')
            this.getPilotsList() // 重新获取列表
          } else {
            this.$message.error(response.message || '审核失败')
          }
        } catch (error) {
          console.error('审核失败:', error)
          this.$message.error('审核失败，请稍后重试')
        }
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },
    async rejectPilot(pilot) {
      console.log('飞手拒绝数据:', pilot);
      this.$prompt(
        `请输入拒绝理由`,
        `拒绝飞手 ${pilot.userName} 的审核`,
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^.{1,50}$/,
          inputErrorMessage: '拒绝理由不能为空且不超过50个字符'
        }
      ).then(async ({ value }) => {
        try {
          // 调用后端API进行审核
          const response = await axios.post('/admin/flyers/audit', {
            targetId: pilot.userId, // 修改为使用userId而不是flyerId
            roleType: 2, // 2-飞手
            auditResult: 2, // 2-拒绝
            auditRemark: value
          })
          
          if (response.code === 200) {
            this.$message.success('已拒绝')
            this.getPilotsList() // 重新获取列表
          } else {
            this.$message.error(response.message || '拒绝失败')
          }
        } catch (error) {
          console.error('拒绝失败:', error)
          this.$message.error('拒绝失败，请稍后重试')
        }
      }).catch(() => {
        this.$message.info('已取消操作')
      })
    },
    getStatusType(status) {
      const typeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger',
        pending: 'warning',
        approved: 'success',
        rejected: 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝',
        pending: '待审核',
        approved: '已通过',
        rejected: '已拒绝'
      }
      return textMap[status] || '未知'
    }
  }
}
</script>

<style scoped>
.pilots-container {
  height: 100%;
}

.pilots-container h2 {
  margin-bottom: 20px;
  color: #303133;
}

.search-bar {
  display: flex;
  align-items: center;
}
</style>