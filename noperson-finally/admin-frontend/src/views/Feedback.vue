<template>
  <div class="feedback-container">
    <h2>需求反馈管理</h2>
    
    <!-- 反馈列表 -->
    <el-table :data="feedbackList" style="width: 100%">
      <el-table-column prop="id" label="反馈ID" width="100"></el-table-column>
      <el-table-column prop="demandId" label="需求ID" width="100"></el-table-column>
      <el-table-column prop="farmerName" label="农户姓名"></el-table-column>
      <el-table-column prop="flyerName" label="飞手姓名"></el-table-column>
      <el-table-column prop="feedbackType" label="反馈类型">
        <template #default="scope">
          <el-tag type="danger">需求未完成</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="反馈时间"></el-table-column>
      <el-table-column prop="status" label="审核状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'success'">
            {{ scope.row.status === 'pending' ? '待审核' : '已审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="viewFeedbackDetail(scope.row)" type="primary">查看详情</el-button>
          <el-button size="small" @click="handleFeedback(scope.row)" v-if="scope.row.status === 'pending'" type="success">处理反馈</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 反馈详情对话框 -->
    <el-dialog
      title="反馈详情"
      v-model="dialogVisible"
      width="60%"
    >
      <div v-if="selectedFeedback" class="feedback-detail">
        <el-form label-position="left" label-width="120px">
          <el-form-item label="反馈ID">
            <span>{{ selectedFeedback.id }}</span>
          </el-form-item>
          <el-form-item label="需求ID">
            <span>{{ selectedFeedback.demandId }}</span>
          </el-form-item>
          <el-form-item label="农户姓名">
            <span>{{ selectedFeedback.farmerName }}</span>
          </el-form-item>
          <el-form-item label="飞手姓名">
            <span>{{ selectedFeedback.flyerName }}</span>
          </el-form-item>
          <el-form-item label="反馈类型">
            <el-tag type="danger">需求未完成</el-tag>
          </el-form-item>
          <el-form-item label="反馈时间">
            <span>{{ selectedFeedback.createTime }}</span>
          </el-form-item>
          <el-form-item label="审核状态">
            <el-tag :type="selectedFeedback.status === 'pending' ? 'warning' : 'success'">
              {{ selectedFeedback.status === 'pending' ? '待审核' : '已审核' }}
            </el-tag>
          </el-form-item>
          <el-form-item label="反馈内容">
            <div class="feedback-content">
              {{ selectedFeedback.feedbackContent || '暂无反馈内容' }}
            </div>
          </el-form-item>
          <el-form-item label="反馈图片">
            <div v-if="selectedFeedback.feedbackImages" class="feedback-images">
              <el-image
                v-for="(image, index) in selectedFeedback.feedbackImages.split(',')"
                :key="index"
                :src="image"
                fit="cover"
                style="width: 150px; height: 150px; margin-right: 10px; margin-bottom: 10px;"
                :preview-src-list="selectedFeedback.feedbackImages.split(',')"
              >
              </el-image>
            </div>
            <span v-else>暂无反馈图片</span>
          </el-form-item>
          <el-form-item label="飞手联系方式">
            <span>{{ selectedFeedback.flyerPhone || '暂无联系方式' }}</span>
          </el-form-item>
        </el-form>
        
        <!-- 操作按钮 -->
        <div class="feedback-actions" v-if="selectedFeedback.status === 'pending'">
          <el-button type="primary" @click="contactFlyer(selectedFeedback)">联系飞手</el-button>
          <el-button type="success" @click="approveFeedback(selectedFeedback)">审核通过</el-button>
          <el-button type="danger" @click="rejectFeedback(selectedFeedback)">审核不通过</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      feedbackList: [],
      dialogVisible: false,
      selectedFeedback: null
    }
  },
  mounted() {
    this.getFeedbackList()
  },
  methods: {
    async getFeedbackList() {
      try {
        const response = await axios.get('/admin/feedback/list', {
          params: {
            pageNum: 1,
            pageSize: 20
          }
        })
        if (response && response.code === 200) {
          this.feedbackList = response.data.records || []
        }
      } catch (error) {
        console.error('获取反馈列表失败:', error)
      }
    },
    async viewFeedbackDetail(feedback) {
      try {
        const response = await axios.get(`/admin/feedback/${feedback.id}`)
        if (response && response.code === 200) {
          this.selectedFeedback = response.data
          this.dialogVisible = true
        }
      } catch (error) {
        console.error('获取反馈详情失败:', error)
      }
    },
    handleFeedback(feedback) {
      this.viewFeedbackDetail(feedback)
    },
    contactFlyer(feedback) {
      this.$confirm('请选择联系方式', '联系飞手', {
        confirmButtonText: '在线聊天',
        cancelButtonText: '致电飞手',
        type: 'info'
      }).then(() => {
        // 在线聊天
        this.$router.push({
          path: '/dashboard/chat',
          query: { userId: feedback.flyerId, userName: feedback.flyerName }
        })
      }).catch(() => {
        // 致电飞手
        if (feedback.flyerPhone) {
          window.location.href = `tel:${feedback.flyerPhone}`
        } else {
          this.$message.warning('暂无飞手联系方式')
        }
      })
    },
    async approveFeedback(feedback) {
      try {
        await axios.put(`/admin/feedback/${feedback.id}/approve`)
        this.$message.success('审核通过')
        this.getFeedbackList()
        this.dialogVisible = false
      } catch (error) {
        console.error('审核通过失败:', error)
        this.$message.error('审核失败，请重试')
      }
    },
    async rejectFeedback(feedback) {
      try {
        await axios.put(`/admin/feedback/${feedback.id}/reject`)
        this.$message.success('审核不通过')
        this.getFeedbackList()
        this.dialogVisible = false
      } catch (error) {
        console.error('审核不通过失败:', error)
        this.$message.error('审核失败，请重试')
      }
    }
  }
}
</script>

<style scoped>
.feedback-container {
  padding: 20px;
}
.feedback-detail {
  line-height: 1.8;
}
.feedback-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>