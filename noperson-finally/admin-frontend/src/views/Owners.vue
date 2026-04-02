<template>
  <div class="owners-container">
    <h2>机主管理</h2>
    <el-card>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索机主姓名或电话" style="width: 300px; margin-right: 10px;"></el-input>
        <el-select v-model="statusFilter" placeholder="状态" style="width: 150px; margin-right: 10px;">
          <el-option label="全部" value=""></el-option>
          <el-option label="待审核" value="pending"></el-option>
          <el-option label="已通过" value="approved"></el-option>
          <el-option label="已拒绝" value="rejected"></el-option>
        </el-select>
        <el-button type="primary" @click="searchOwners">搜索</el-button>
      </div>
      <el-table :data="ownersList" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="realName" label="姓名"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column prop="userId" label="用户ID"></el-table-column>
        <el-table-column prop="licenseType" label="执照类型"></el-table-column>
        <el-table-column prop="licenseNumberMasked" label="执照编号"></el-table-column>
        <el-table-column prop="commonArea" label="经营区域"></el-table-column>
        <el-table-column prop="deviceTotal" label="设备总数" width="100"></el-table-column>
        <el-table-column prop="auditStatus" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.auditStatus)">
              {{ getStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewOwnerDetail(scope.row)">查看</el-button>
            <template v-if="scope.row.auditStatus === 0">
              <el-button type="success" size="small" @click="approveOwner(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectOwner(scope.row)">拒绝</el-button>
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
    
    <!-- 机主详情对话框 -->
    <el-dialog v-model="dialogVisible" title="机主详情" width="800px">
      <div v-if="selectedOwner">
        <!-- 头像和基本信息 -->
        <div class="owner-header-info" style="display: flex; align-items: center; margin-bottom: 20px; padding: 16px; background-color: #f5f7fa; border-radius: 8px;">
          <el-avatar 
            :src="selectedOwner.avatarUrl || '/avatar.png'" 
            size="100" 
            style="margin-right: 20px;"
          />
          <div>
            <h3 style="margin: 0; font-size: 20px;">{{ selectedOwner.realName }}</h3>
            <div style="margin-top: 10px; line-height: 1.8;">
              <div><strong>用户ID:</strong> {{ selectedOwner.userId }}</div>
              <div><strong>状态:</strong> <el-tag :type="getStatusType(selectedOwner.auditStatus)">{{ getStatusText(selectedOwner.auditStatus) }}</el-tag></div>
            </div>
          </div>
        </div>

        <!-- 基本信息部分 -->
        <h4 style="margin-top: 0; margin-bottom: 16px; color: #303133;">基本信息</h4>
        <el-descriptions :column="2" border class="info-section" style="margin-bottom: 24px;">
          <el-descriptions-item label="姓名">{{ selectedOwner.realName }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ selectedOwner.phone || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedOwner.email || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ selectedOwner.address || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ selectedOwner.createTime || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ selectedOwner.auditRemark || '暂无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 专业信息部分 -->
        <h4 style="margin-top: 0; margin-bottom: 16px; color: #303133;">专业信息</h4>
        <el-descriptions :column="2" border class="info-section" style="margin-bottom: 24px;">
          <el-descriptions-item label="执照类型">{{ selectedOwner.licenseType || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="执照编号">{{ selectedOwner.licenseNumberMasked || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="经营区域">{{ selectedOwner.commonArea || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="设备总数">{{ selectedOwner.deviceTotal || 0 }}</el-descriptions-item>
          <el-descriptions-item label="可用设备数">{{ selectedOwner.availableDeviceCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="信用评分">{{ selectedOwner.creditScore || 0 }}</el-descriptions-item>
          <el-descriptions-item label="飞行经验（年）">{{ selectedOwner.flyingExperience || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ selectedOwner.emergencyContact || '暂无' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 资质证明部分 -->
        <h4 style="margin-top: 0; margin-bottom: 16px; color: #303133;">资质证明</h4>
        <div v-if="selectedOwner.licenseUrls && selectedOwner.licenseUrls.length > 0" class="license-images">
          <el-image 
            v-for="(url, index) in selectedOwner.licenseUrls" 
            :key="index"
            :src="url" 
            style="width: 250px; height: 200px; margin-right: 15px; margin-bottom: 15px;"
            :preview-src-list="selectedOwner.licenseUrls"
          />
        </div>
        <span v-else style="color: #909399;">暂无资质证明图片</span>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      ownersList: [],
      searchKeyword: '',
      statusFilter: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogVisible: false,
      selectedOwner: null
    }
  },
  mounted() {
    this.getOwnersList()
  },
  methods: {
    async getOwnersList() {
      try {
        // 转换状态筛选参数
        const auditStatus = this.statusFilter === 'pending' ? 0 : 
                          this.statusFilter === 'approved' ? 1 : 
                          this.statusFilter === 'rejected' ? 2 : null
        
        // 调用后端API获取机主列表
        const response = await axios.get('/admin/owners/page', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            auditStatus: auditStatus,
            area: this.searchKeyword // 后端接受area参数而不是keyword
          }
        })
        
        if (response && response.code === 200) {
          const data = response.data
          this.ownersList = data.records || []
          this.total = data.total || 0
        } else if (response) {
          this.$message.error(response.message || '获取机主列表失败')
          this.ownersList = []
          this.total = 0
        } else {
          this.$message.error('获取机主列表失败：无效的响应')
          this.ownersList = []
          this.total = 0
        }
      } catch (error) {
        console.error('获取机主列表失败:', error)
        this.$message.error('获取机主列表失败，请稍后重试')
        this.ownersList = []
        this.total = 0
      }
    },
    searchOwners() {
      this.getOwnersList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getOwnersList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getOwnersList()
    },
    viewOwnerDetail(owner) {
          this.selectedOwner = owner
          this.dialogVisible = true
        },
        approveOwner(owner) {
          this.$confirm('确定要审核通过该机主吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(async () => {
            try {
        const response = await axios.post('/admin/owners/audit', {
                targetId: owner.id,
                auditResult: 1,
                auditRemark: '审核通过'
              })
              if (response.code === 200) {
                this.$message.success('审核通过成功')
                this.getOwnersList()
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
        rejectOwner(owner) {
          this.$prompt('请输入拒绝原因', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputPattern: /^\S/,
            inputErrorMessage: '拒绝原因不能为空'
          }).then(async ({ value }) => {
            try {
        const response = await axios.post('/admin/owners/audit', {
                targetId: owner.id,
                auditResult: 2,
                auditRemark: value
              })
              if (response.code === 200) {
                this.$message.success('拒绝成功')
                this.getOwnersList()
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
.owners-container {
  height: 100%;
  padding: 20px;
}

.owners-container h2 {
  margin-bottom: 20px;
  color: #303133;
  font-size: 24px;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.license-images {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.info-section {
  margin-bottom: 20px;
}

.info-section :deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
}

.owner-header-info {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}
</style>