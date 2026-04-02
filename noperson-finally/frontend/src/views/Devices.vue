<template>
  <div class="devices">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">设备管理</h1>
      <p class="page-description">管理您的无人机设备列表，查看详细信息并进行设备维护</p>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="device-card">
      <!-- 卡片头部 -->
      <template #header>
        <div class="card-header">
          <div class="header-title-section">
            <h2 class="header-title">设备列表</h2>
            <div class="header-stats">
              <span class="total-devices">
                <span class="stats-number">{{ deviceList.length }}</span>
                <span class="stats-label">设备</span>
              </span>
              <div class="status-summary">
                <div class="status-item" v-for="status in statusSummary" :key="status.value">
                  <span class="status-dot" :class="`status-${status.type}`"></span>
                  <span class="status-text">{{ status.text }} ({{ status.count }})</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 添加设备按钮 -->
          <el-button 
            type="primary" 
            @click="handleAddDevice"
            class="add-device-btn"
            :icon="Plus"
          >
            添加设备
          </el-button>
          <el-button 
            type="success" 
            @click="loadMockData"
            class="load-mock-btn"
          >
            加载演示数据
          </el-button>
        </div>
      </template>
      
      <!-- 搜索和筛选区域 -->
      <div class="search-filter-container">
        <div class="search-section">
          <el-input 
            v-model="searchForm.deviceName" 
            placeholder="搜索设备名称、ID或型号" 
            class="search-input"
            clearable
            prefix-icon="Search"
          ></el-input>
          
          <el-select 
            v-model="searchForm.status" 
            placeholder="设备状态" 
            class="status-select"
            clearable
          >
            <el-option label="正常" value="1" />
            <el-option label="维护中" value="2" />
            <el-option label="故障" value="3" />
            <el-option label="停用" value="4" />
          </el-select>
        </div>
        
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="handleSearch"
            class="search-btn"
            :icon="Search"
          >
            查询
          </el-button>
          
          <el-button 
            @click="handleReset"
            class="reset-btn"
          >
            重置
          </el-button>
          
          <el-button 
            type="info" 
            @click="showDebugInfo = !showDebugInfo"
            class="debug-btn"
          >
            {{ showDebugInfo ? '隐藏调试信息' : '显示调试信息' }}
          </el-button>
        </div>
      </div>
      
      <!-- 调试信息区域 -->
      <el-collapse v-if="showDebugInfo" class="debug-info" style="margin-bottom: 20px;">
        <el-collapse-item title="设备数据结构调试">
          <div v-if="firstDeviceData">
            <h4>第一个设备的完整数据:</h4>
            <pre>{{ JSON.stringify(firstDeviceData, null, 2) }}</pre>
            <h4>设备对象的所有键:</h4>
            <pre>{{ deviceKeys }}</pre>
            <h4>deviceInfo对象的所有键:</h4>
            <pre>{{ deviceInfoKeys }}</pre>
            <h4>找到的续航时间值:</h4>
            <pre>{{ foundEnduranceValue }}</pre>
          </div>
          <div v-else>
            暂无设备数据
          </div>
        </el-collapse-item>
      </el-collapse>
      
      <!-- 数据验证区域 -->
      <div class="data-validation" style="background: #f0f0f0; padding: 20px; margin: 20px 0; border: 2px solid #333; z-index: 9999; position: relative;">
        <h3 style="color: #000;">数据验证：</h3>
        <p style="color: #000; font-size: 16px;">设备数量: {{ deviceList.length }}</p>
        <div v-if="deviceList.length > 0">
          <p style="color: #000;">第一个设备名称: {{ deviceList[0].deviceName }}</p>
          <p style="color: #000;">第一个设备型号: {{ deviceList[0].deviceModel }}</p>
          <p style="color: #000;">第一个设备品牌: {{ deviceList[0].brand }}</p>
        </div>
      </div>

      <!-- 表格区域 -->
      <div class="table-section">
        <!-- 数据统计信息 -->
        <div class="data-info" style="margin-bottom: 16px; font-size: 14px; color: #606266;">
          共 <span style="color: #409eff; font-weight: bold;">{{pagination.total}}</span> 台设备，
          当前显示 <span style="color: #409eff; font-weight: bold;">{{deviceList.length}}</span> 台
        </div>
        
        <el-table 
          :data="deviceList" 
          style="width: 100%; background: white; border: 1px solid #ccc;"
        >
          <!-- 复选框列 -->
          <el-table-column type="selection" width="55" align="center" />
          
          <!-- 设备信息列 -->
          <el-table-column prop="deviceId" label="设备ID" width="100" class-name="device-id-column" />
          <el-table-column prop="deviceName" label="设备名称" width="180" />
          <el-table-column prop="deviceModel" label="设备型号" width="150" />
          <el-table-column prop="brand" label="设备品牌" width="120" />
          <el-table-column prop="serialNumber" label="设备编号" width="180" />
          
          <!-- 设备图片列 -->
          <el-table-column label="设备图片" width="120" align="center">
            <template #default="scope">
              <el-image 
                :src="getDeviceImage(scope.row.picture)"
                fit="cover" 
                :preview-src-list="[getDeviceImage(scope.row.picture)]"
                class="device-table-image"
              />
            </template>
          </el-table-column>
          
          <!-- 状态列 -->
          <el-table-column prop="status" label="设备状态" width="120">
            <template #default="scope">
              <div class="status-container">
                <el-tag 
                  :type="getStatusTagType(scope.row.status)"
                  class="status-tag"
                  effect="light"
                >
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="maxLoad" label="最大载重(kg)" width="120">
            <template #default="scope">
              {{ scope.row.maxLoad || 0 }}
            </template>
          </el-table-column>
          
          <el-table-column prop="endurance" label="续航时间(分钟)" width="140">
            <template #default="scope">
              {{ scope.row.endurance || 0 }}
            </template>
          </el-table-column>
          
          <el-table-column prop="manufacturer" label="设备制造商" width="150" />
          <el-table-column prop="purchaseDate" label="购买日期" width="140" />
          

          
          <!-- 操作列 -->
          <el-table-column label="操作" width="220" fixed="right" class-name="operation-column">
            <template #default="scope">
              <div class="operation-buttons">
                <el-tooltip content="编辑设备信息">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="handleEditDevice(scope.row.deviceId)"
                    class="edit-btn"
                  >
                    编辑
                  </el-button>
                </el-tooltip>
                
                <el-tooltip content="查看设备详情">
                  <el-button 
                    size="small" 
                    @click="handleViewDetails(scope.row)"
                    class="detail-btn"
                  >
                    详情
                  </el-button>
                </el-tooltip>
                
                <el-popconfirm
                  title="确定要删除该设备吗？"
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  @confirm="handleDeleteDevice(scope.row.deviceId)"
                >
                  <template #reference>
                    <el-button type="danger" size="small" class="delete-btn">删除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页区域 -->
      <div class="pagination-container">
        <div class="pagination-info">
          <span>显示第 {{ (pagination.currentPage - 1) * pagination.pageSize + 1 }}-{{ Math.min(pagination.currentPage * pagination.pageSize, pagination.total) }} 条，共 {{ pagination.total }} 条</span>
        </div>
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination-control"
        />
      </div>
    </el-card>
    
    <!-- 设备详情对话框 -->
    <el-dialog
      v-model="deviceDetailDialogVisible"
      title="设备详情"
      width="70%"
      class="device-detail-dialog"
      custom-class="modern-dialog"
    >
      <div class="dialog-header-info">
        <div class="device-main-info">
          <h3 class="dialog-device-name">{{ currentDevice.deviceName }}</h3>
          <div class="dialog-device-model">{{ currentDevice.deviceModel }}</div>
        </div>
        <div class="device-status-badge">
          <el-tag :type="getStatusTagType(currentDevice.status)" class="status-badge">
            {{ getStatusText(currentDevice.status) }}
          </el-tag>
        </div>
      </div>
      
      <!-- 设备图片展示区域 -->
      <div class="device-image-section" style="margin-bottom: 20px; text-align: center;">
        <el-image 
          :src="getDeviceImage(currentDevice.picture)"
          fit="contain" 
          :preview-src-list="[getDeviceImage(currentDevice.picture)]"
          class="device-detail-image"
          lazy
        />
      </div>
      
      <el-descriptions :column="2" border class="device-descriptions">
        <el-descriptions-item label="设备ID">{{ currentDevice.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ currentDevice.serialNumber }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">{{ currentDevice.deviceType }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ currentDevice.brand }}</el-descriptions-item>
          <el-descriptions-item label="最大载重(kg)">{{ currentDevice.maxLoad }}</el-descriptions-item>
          <el-descriptions-item label="续航时间(分钟)">{{ currentDevice.endurance }}</el-descriptions-item>
          <el-descriptions-item label="购买日期">{{ currentDevice.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="当前位置" :span="2">
          <div class="location-info">
            <span class="location-text">{{ currentDevice.location }}</span>
            <span class="coordinates">{{ currentDevice.longitude }}, {{ currentDevice.latitude }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="飞行小时数">{{ currentDevice.flightHours }} 小时</el-descriptions-item>
        <el-descriptions-item label="总起降次数">{{ currentDevice.flightCount }} 次</el-descriptions-item>
        <el-descriptions-item label="最后在线时间" :span="2">
          <div class="online-time-info">
            <span class="online-time-text">{{ currentDevice.lastOnlineTime }}</span>
            <span class="online-time-relative">{{ getRelativeTime(currentDevice.lastOnlineTime) }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentDevice.remark || '无备注信息' }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 维护记录表格 -->
      <div class="maintain-records-section">
        <h3 class="section-title">维护记录</h3>
        <el-table 
          v-loading="maintainRecordsLoading" 
          :data="maintainRecords" 
          stripe 
          style="width: 100%"
          :empty-text="'暂无维护记录'"
        >
          <el-table-column prop="recordId" label="记录ID" width="100" />
          <el-table-column prop="maintainType" label="维护类型" width="120">
            <template #default="scope">
              {{ getMaintainTypeText(scope.row.maintainType) }}
            </template>
          </el-table-column>
          <el-table-column prop="faultDescription" label="故障描述" min-width="150">
            <template #default="scope">
              {{ scope.row.faultDescription || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="maintainContent" label="维护内容" min-width="200" />
          <el-table-column prop="replaceParts" label="更换配件" min-width="150">
            <template #default="scope">
              {{ scope.row.replaceParts || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="cost" label="维护费用(元)" width="120">
            <template #default="scope">
              {{ scope.row.cost || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="maintainTime" label="维护时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              {{ getMaintainStatusText(scope.row.status) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="updateTime" label="更新时间" width="180" />
          <el-table-column prop="operatorId" label="操作人ID" width="120" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Picture } from '@element-plus/icons-vue'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'Devices',
  components: {
    Plus,
    Search,
    Picture
  },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const deviceDetailDialogVisible = ref(false)
    const currentDevice = ref({})
    const hoveredRow = ref(null)
    const maintainRecords = ref([])
    const maintainRecordsLoading = ref(false)
    
    const searchForm = reactive({
      deviceName: '',
      status: ''
    })
    
    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      total: 0
    })
    
    // 设备列表数据
    const deviceList = ref([])
    const showDebugInfo = ref(false)
    const firstDeviceData = ref(null)
    const deviceKeys = ref([])
    const deviceInfoKeys = ref([])
    const foundEnduranceValue = ref(null)
    console.log('直接初始化的deviceList:', deviceList.value)
    
    // 计算属性：状态统计摘要
    const statusSummary = computed(() => {
      const statusMap = {
        1: { text: '正常', type: 'success', count: 0 },
        2: { text: '维护中', type: 'warning', count: 0 },
        3: { text: '故障', type: 'danger', count: 0 },
        4: { text: '停用', type: 'info', count: 0 }
      }
      
      deviceList.value.forEach(device => {
        if (statusMap[device.status]) {
          statusMap[device.status].count++
        }
      })
      
      return Object.entries(statusMap).map(([value, info]) => ({
        value,
        ...info
      })).filter(item => item.count > 0)
    })
    
    // 计算属性：空数据提示文本
    const emptyText = computed(() => {
      if (searchForm.deviceName || searchForm.status) {
        return '没有找到符合条件的设备'
      }
      return '暂无设备数据'
    })
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
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
        1: 'success',
        2: 'warning',
        3: 'danger',
        4: 'info'
      }
      return typeMap[status] || 'info'
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      try {
        const date = new Date(dateString)
        if (isNaN(date.getTime())) return dateString
        return date.toLocaleDateString('zh-CN')
      } catch (error) {
        return dateString
      }
    }
    
    // 获取相对时间
    const getRelativeTime = (timeString) => {
      const now = new Date()
      const time = new Date(timeString)
      const diffMinutes = Math.floor((now - time) / (1000 * 60))
      
      if (diffMinutes < 1) return '刚刚'
      if (diffMinutes < 60) return `${diffMinutes}分钟前`
      
      const diffHours = Math.floor(diffMinutes / 60)
      if (diffHours < 24) return `${diffHours}小时前`
      
      const diffDays = Math.floor(diffHours / 24)
      if (diffDays < 30) return `${diffDays}天前`
      
      return timeString
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
    
    // 行悬停处理
    const handleRowHover = (row, column, event) => {
      hoveredRow.value = row
    }
    
    // 行点击处理
    const handleRowClick = (row, column, event) => {
      // 如果点击的是操作按钮，不触发行点击
      if (event.target.closest('.operation-buttons')) {
        return
      }
      // 可以在这里添加行点击的逻辑
    }
    
    // 搜索设备
    const handleSearch = () => {
      pagination.currentPage = 1
      fetchDevices()
    }
    
    // 重置搜索
    const handleReset = () => {
      searchForm.deviceName = ''
      searchForm.status = ''
      pagination.currentPage = 1
      fetchDevices()
    }
    
    // 添加设备
    const handleAddDevice = () => {
      router.push('/device/add')
    }
    
    // 编辑设备
    const handleEditDevice = (deviceId) => {
      router.push(`/device/${deviceId}/edit`)
    }
    
    // 查看设备详情
    const handleViewDetails = (device) => {
      router.push(`/detail/${device.deviceId}`)
    }
    
    // 获取设备维护记录
    const fetchDeviceMaintainRecords = async (deviceId) => {
      if (!deviceId) return
      
      maintainRecordsLoading.value = true
      try {
        const response = await axios.get(`/device/maintain/list/${deviceId}`)
        if (response.data && response.data.code === 200 && response.data.data) {
          maintainRecords.value = response.data.data.records || []
        }
      } catch (error) {
        console.error('获取设备维护记录失败:', error)
        ElMessage.error('获取设备维护记录失败')
        maintainRecords.value = []
      } finally {
        maintainRecordsLoading.value = false
      }
    }
    
    // 获取维护类型文本
    const getMaintainTypeText = (type) => {
      const typeMap = {
        1: '常规保养',
        2: '故障维修'
      }
      return typeMap[type] || '未知'
    }
    
    // 获取维护状态文本
    const getMaintainStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '已确认'
      }
      return statusMap[status] || '未知'
    }
    
    // 删除设备
    const handleDeleteDevice = async (deviceId) => {
      try {
        // 调用后端删除API
        console.log('准备删除设备ID:', deviceId)
        const response = await axios.delete(`/device/delete/${deviceId}`)
        
        // 详细打印响应信息以便调试
        console.log('删除API响应:', response)
        console.log('响应类型:', typeof response)
        
        // 处理可能的不同响应结构
        let responseData = response;
        let success = false;
        let message = '';
        
        // 检查响应结构并提取正确的数据
        if (response && typeof response === 'object') {
          if (response.code === 200) {
            // 情况1: response直接是后端返回的数据对象
            success = true;
            message = response.message || '设备删除成功';
            console.log('识别为情况1: response直接是后端返回的数据')
          } else if (response.data && response.data.code === 200) {
            // 情况2: 标准axios响应结构
            success = true;
            message = response.data.message || '设备删除成功';
            console.log('识别为情况2: 标准axios响应结构')
          }
        }
        
        if (success) {
          console.log('后端返回删除成功:', message)
          
          // 从列表中移除设备
          const index = deviceList.value.findIndex(item => item.deviceId === deviceId)
          console.log('在列表中查找设备索引结果:', index)
          
          if (index !== -1) {
            deviceList.value.splice(index, 1)
            console.log('设备已从列表中移除')
          }
          
          // 更新总数
          pagination.total -= 1
          console.log('更新后总数:', pagination.total)
          
          ElMessage.success({
            message: message,
            duration: 2000,
            showClose: true
          })
        } else {
          // 尝试获取错误信息
          const errorMsg = response?.message || response?.data?.message || '未知错误';
          const errorCode = response?.code || response?.data?.code || 'undefined';
          console.log('后端返回非成功状态:', errorCode, errorMsg)
          ElMessage.error({
            message: '删除设备失败：' + errorMsg + ' (响应码: ' + errorCode + ')',
            duration: 2000,
            showClose: true
          })
        }
      } catch (error) {
        console.error('删除设备异常:', error)
        // 处理异常情况，特别是设备被租借中的情况
        const errorMsg = error.response?.data?.message || error.message || '删除设备失败，请重试';
        console.error('错误详情:', error.response || error.message)
        ElMessage.error({
          message: errorMsg,
          duration: 2000,
          showClose: true
        })
      }
    }
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pagination.pageSize = size
      fetchDevices()
    }
    
    // 当前页码变化
    const handleCurrentChange = (current) => {
      pagination.currentPage = current
      fetchDevices()
    }
    
    // 获取设备列表
    const fetchDevices = async () => {
      console.log('获取设备列表')
      loading.value = true
      
      try {
        // 直接调用真实API获取设备数据
        const response = await axios.get('/device/list', {
          params: {
            pageNum: pagination.currentPage,
            pageSize: pagination.pageSize
          }
        })
        
        // 添加详细的API响应日志
        console.log('API完整响应原始对象:', response);
        console.log('API完整响应JSON字符串:', JSON.stringify(response));
        console.log('响应数据结构:', Object.keys(response.data));
        console.log('响应数据JSON字符串:', JSON.stringify(response.data));
        if (response.data.data) {
          console.log('data对象结构:', Object.keys(response.data.data));
          console.log('data对象JSON字符串:', JSON.stringify(response.data.data));
          // 如果有records，记录第一条设备的完整结构
          if (response.data.data.records && response.data.data.records.length > 0) {
            console.log('第一条设备记录完整数据:', response.data.data.records[0]);
            console.log('第一条设备记录JSON字符串:', JSON.stringify(response.data.data.records[0]));
            // 检查设备记录中的endurance相关字段
            const firstRecord = response.data.data.records[0];
            console.log('设备记录中所有字段:', Object.keys(firstRecord));
            if (firstRecord.deviceInfo) {
              console.log('deviceInfo中所有字段:', Object.keys(firstRecord.deviceInfo));
              console.log('deviceInfo.endurance值:', firstRecord.deviceInfo.endurance);
            }
          }
        }
        
        // 处理响应数据
          let devices = []
          
          // 检查不同的响应格式
          // 1. 直接返回records和total的格式
          if (response.data && Array.isArray(response.data.records)) {
            devices = response.data.records
            pagination.total = response.data.total || 0
          }
          // 2. 标准响应格式（code + data）
          else if (response.data && response.data.code === 200 && response.data.data) {
            if (Array.isArray(response.data.data.records)) {
              devices = response.data.data.records
              pagination.total = response.data.data.total || 0
            } else if (Array.isArray(response.data.data)) {
              devices = response.data.data
              pagination.total = devices.length
            }
          }
          // 3. 响应直接是数组
          else if (Array.isArray(response.data)) {
            devices = response.data
            pagination.total = devices.length
          }
          else {
            console.warn('未知的数据格式:', response.data);
          }
          
          // 格式化设备数据 - 处理嵌套的deviceInfo结构
          const formattedDevices = devices.map(device => {
            const deviceInfo = device.deviceInfo || {};
            
            // 调试日志：记录device和deviceInfo中的endurance相关字段
            console.log('设备原始数据:', device);
            console.log('设备endurance值:', device.endurance);
            console.log('设备deviceInfo.endurance值:', deviceInfo.endurance);
            
            // 全面检查endurance字段，根据实际数据结构调整
      let enduranceValue = 0;
      // 首先检查deviceInfo中的endurance字段
      if (deviceInfo && deviceInfo.endurance !== undefined && deviceInfo.endurance !== null) {
        enduranceValue = deviceInfo.endurance;
        console.log('找到endurance值:', enduranceValue);
      } else {
        // 检查device对象直接包含的endurance字段
        if (device && device.endurance !== undefined && device.endurance !== null) {
          enduranceValue = device.endurance;
          console.log('从device对象找到endurance值:', enduranceValue);
        } else {
          // 检查其他可能的字段名
          const possibleFields = ['enduranceTime', 'endurancetime', 'endurance_time'];
          for (const field of possibleFields) {
            if (deviceInfo && deviceInfo[field] !== undefined && deviceInfo[field] !== null) {
              enduranceValue = deviceInfo[field];
              console.log(`找到${field}值作为endurance:`, enduranceValue);
              break;
            }
            if (device && device[field] !== undefined && device[field] !== null) {
              enduranceValue = device[field];
              console.log(`从device对象找到${field}值作为endurance:`, enduranceValue);
              break;
            }
          }
        }
      }
      console.log('最终使用的endurance值:', enduranceValue);
            
            // 获取设备图片
            let pictureValue = '';
            // 检查deviceInfo中的picture字段
            if (deviceInfo && deviceInfo.picture !== undefined && deviceInfo.picture !== null) {
              pictureValue = deviceInfo.picture;
              console.log('找到picture值:', pictureValue);
            } else {
              // 检查device对象直接包含的picture字段
              if (device && device.picture !== undefined && device.picture !== null) {
                pictureValue = device.picture;
                console.log('从device对象找到picture值:', pictureValue);
              }
            }
            
            return {
              deviceId: (deviceInfo.deviceId || device.deviceId || deviceInfo.device_id || device.device_id)?.toString() || '未知ID',
              deviceName: deviceInfo.deviceName || device.deviceName || deviceInfo.device_name || device.device_name || '未知设备',
              deviceModel: deviceInfo.model || device.model || '未知型号',
              deviceType: deviceInfo.deviceType || device.deviceType || deviceInfo.device_type || device.device_type || '未知类型',
              purchaseDate: (deviceInfo.purchaseTime || device.purchaseTime || deviceInfo.purchase_time || device.purchase_time) ? 
                           (deviceInfo.purchaseTime || device.purchaseTime || deviceInfo.purchase_time || device.purchase_time).split(' ')[0] : '',
              status: deviceInfo.status || device.status || 0,
              location: '',
              lastOnlineTime: deviceInfo.lastMaintainTime || device.lastMaintainTime || deviceInfo.last_maintain_time || device.last_maintain_time || '',
              serialNumber: deviceInfo.serialNumber || device.serialNumber || deviceInfo.serialnumber || device.serialnumber || '未知序列号',
              longitude: '',
              latitude: '',
              flightHours: '',
              flightCount: '',
              remark: '',
              brand: deviceInfo.deviceType || device.deviceType || deviceInfo.device_type || device.device_type || '',
              maxLoad: deviceInfo.maxLoad || device.maxLoad || deviceInfo.max_load || device.max_load || 0,
              endurance: enduranceValue,
              manufacturer: deviceInfo.manufacturer || device.manufacturer || '未知厂商',
              picture: pictureValue // 添加设备图片
            };
          });
          
          deviceList.value = formattedDevices
        // 显示数据加载状态提示
        ElMessage.success(`成功加载 ${formattedDevices.length} 台设备`)
        
        loading.value = false
      } catch (error) {
        console.error('获取设备列表失败:', error)
        ElMessage.error('获取设备列表失败，请稍后重试')
        deviceList.value = []
        pagination.total = 0
        loading.value = false
      }
    }
    
    // 现在直接从API获取真实数据
    
    onMounted(() => {
      console.log('设备管理页面已挂载')
      console.log('初始deviceList状态:', deviceList.value)
      console.log('初始pagination状态:', pagination)
      
      // 检查是否已登录（有token）
      const token = localStorage.getItem('token')
      console.log('当前token状态:', token ? '存在' : '不存在')
      
      if (!token) {
        console.error('未登录，没有token')
        ElMessage.warning({
          message: '您当前未登录，无法获取数据',
          duration: 3000,
          showClose: true
        })
        // 不加载模拟数据
      } else {
        console.log('已登录，开始获取设备列表')
        // 初始加载设备列表
        fetchDevices()
      }
    })
    
    return {
      searchForm,
      pagination,
      deviceList,
      loading,
      deviceDetailDialogVisible,
      currentDevice,
      hoveredRow,
      statusSummary,
      emptyText,
      getStatusText,
      getStatusTagType,
      getRelativeTime,
      formatDate,
      getDeviceImage,
      handleRowHover,
      handleRowClick,
      handleSearch,
      handleReset,
      handleAddDevice,
      handleEditDevice,
      handleViewDetails,
      handleDeleteDevice,
      handleSizeChange,
      handleCurrentChange,
      maintainRecords,
      maintainRecordsLoading,
      getMaintainTypeText,
      getMaintainStatusText
    }
  }
}
</script>

<style scoped>
/* 全局样式重置 */
* {
  box-sizing: border-box;
}

/* 页面容器 */
.devices {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e6eaf0 100%);
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
  position: relative;
}

/* 维护记录部分 */
.maintain-records-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
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

.page-description {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

/* 设备卡片 */
.device-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: #ffffff;
}

.device-card:hover {
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

.header-title-section {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.total-devices {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stats-number {
  font-size: 24px;
  font-weight: 700;
  color: #409EFF;
}

.stats-label {
  font-size: 14px;
  color: #606266;
}

.status-summary {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-dot.status-success {
  background-color: #67c23a;
}

.status-dot.status-warning {
  background-color: #e6a23c;
}

.status-dot.status-danger {
  background-color: #f56c6c;
}

.status-dot.status-info {
  background-color: #909399;
}

/* 添加设备按钮 */
.add-device-btn {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.add-device-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66B1FF, #409EFF);
}


/* 搜索筛选区域 */
.search-filter-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  gap: 16px;
  flex-wrap: wrap;
}

.search-section {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 320px;
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.search-input:focus-within {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.status-select {
  width: 180px;
  border-radius: 10px;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

.status-select:focus-within {
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.search-btn,
.reset-btn {
  border-radius: 10px;
  padding: 10px 20px;
  transition: all 0.3s ease;
}

.search-btn {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #66B1FF, #409EFF);
}

.reset-btn {
  border: 1px solid #dcdfe6;
  background: #ffffff;
}

.reset-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
  background: #ecf5ff;
}

/* 表格区域 */
.table-section {
  padding: 24px;
}

.device-table {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

/* 表格头部样式 */
.device-table :deep(.el-table__header th) {
  background: linear-gradient(90deg, #f8f9ff, #f0f2f5);
  border-bottom: 2px solid #e0e0e0;
  color: #303133;
  font-weight: 600;
  font-size: 14px;
  padding: 16px 12px;
  text-align: left;
  transition: background-color 0.3s ease;
}

.device-table :deep(.el-table__header th:hover) {
  background: linear-gradient(90deg, #f0f2ff, #e6e8f0);
}

/* 表格行样式 */
.device-table :deep(.el-table__body tr) {
  transition: all 0.3s ease;
}

.device-table :deep(.el-table__body tr:hover > td) {
  background-color: #f5f7fa !important;
}

.device-table :deep(.el-table__body tr:nth-child(even)) {
  background-color: #fafafa;
}

/* 表格单元格样式 */
.device-table :deep(.el-table__body td) {
  border-bottom: 1px solid #f0f0f0;
  color: #606266;
  font-size: 14px;
  padding: 14px 12px;
  transition: all 0.3s ease;
}

/* 设备名称单元格 */
.device-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.device-model {
  font-size: 12px;
  color: #909399;
}

/* 状态标签样式 */
.status-container {
  display: flex;
  align-items: center;
}

.status-tag {
  border-radius: 12px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 500;
}

/* 在线时间样式 */
.online-time {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-text {
  font-size: 14px;
  color: #606266;
}

.time-relative {
  font-size: 12px;
  color: #909399;
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.edit-btn,
.detail-btn,
.delete-btn {
  border-radius: 8px;
  padding: 4px 12px;
  font-size: 13px;
  transition: all 0.3s ease;
}

.edit-btn {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border: none;
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  background: linear-gradient(135deg, #66B1FF, #409EFF);
}

.detail-btn {
  border: 1px solid #dcdfe6;
  background: #ffffff;
}

.detail-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
  background: #ecf5ff;
}

.delete-btn {
  border: 1px solid #f56c6c;
  background: #ffffff;
  color: #f56c6c;
}

.delete-btn:hover {
  background: #fef0f0;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.2);
}

/* 分页区域 */
.pagination-container {
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  gap: 16px;
  flex-wrap: wrap;
}

.pagination-info {
  font-size: 13px;
  color: #606266;
}

.pagination-control :deep(.el-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-control :deep(.el-pagination button),
.pagination-control :deep(.el-pagination .el-pager li) {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.pagination-control :deep(.el-pagination .el-pager li) {
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  margin: 0 2px;
}

.pagination-control :deep(.el-pagination .el-pager li:hover:not(.disabled)) {
  color: #409EFF;
  background-color: #ecf5ff;
  transform: translateY(-1px);
}

.pagination-control :deep(.el-pagination .el-pager li.active) {
  color: #ffffff;
  background-color: #409EFF;
  font-weight: 600;
  transform: translateY(-1px);
}

/* 详情对话框样式 */
.device-detail-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.modern-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}

.dialog-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.device-main-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dialog-device-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.dialog-device-model {
  font-size: 14px;
  color: #606266;
}

.status-badge {
  border-radius: 12px;
  padding: 6px 14px;
  font-size: 14px;
  font-weight: 500;
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

.location-text {
  font-weight: 500;
  color: #303133;
}

.coordinates {
  font-size: 13px;
  color: #909399;
}

.online-time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.online-time-text {
  font-weight: 500;
  color: #303133;
}

.online-time-relative {
  font-size: 13px;
  color: #909399;
}

/* 图片样式 */
.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 12px;
}

/* 动画效果 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.device-card {
  animation: fadeInUp 0.6s ease-out;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-input {
    width: 280px;
  }
  
  .header-stats {
    gap: 16px;
  }
  
  .status-summary {
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .devices {
    padding: 12px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-description {
    font-size: 13px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px 20px;
  }
  
  .header-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-filter-container {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    padding: 16px 20px;
  }
  
  .search-section {
    flex-direction: column;
    gap: 12px;
  }
  
  .search-input,
  .status-select {
    width: 100%;
  }
  
  .action-buttons {
    justify-content: stretch;
  }
  
  .search-btn,
  .reset-btn {
    flex: 1;
  }
  
  .table-section {
    padding: 16px;
  }
  
  .device-table :deep(.el-table__header th),
  .device-table :deep(.el-table__body td) {
    padding: 12px 8px;
    font-size: 13px;
  }
  
  .operation-buttons {
    flex-wrap: wrap;
  }
  
  .pagination-container {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 16px 20px;
  }
  
  .pagination-control :deep(.el-pagination) {
    justify-content: center;
  }
  
  .dialog-header-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
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
}

@media (max-width: 480px) {
  .devices {
    padding: 8px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-description {
    font-size: 12px;
  }
  
  .card-header {
    padding: 12px 16px;
  }
  
  .header-title {
    font-size: 18px;
  }
  
  .search-filter-container {
    padding: 12px 16px;
  }
  
  .table-section {
    padding: 12px;
  }
  
  .device-table :deep(.el-table__header th),
  .device-table :deep(.el-table__body td) {
    padding: 10px 6px;
    font-size: 12px;
  }
  
  .edit-btn,
  .detail-btn,
  .delete-btn {
    padding: 3px 8px;
    font-size: 12px;
  }
  
  .pagination-container {
    padding: 12px 16px;
  }
  
  .dialog-device-name {
    font-size: 18px;
  }
  
  .dialog-device-model {
    font-size: 13px;
  }
}
</style>