<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1 class="profile-title">个人资料</h1>
      <el-button type="primary" @click="handleEdit">编辑</el-button>
    </div>
    
    <!-- 调试信息展示 -->
    <div class="debug-info" style="background-color: #f5f5f5; padding: 10px; margin-bottom: 20px; border-radius: 4px;">
      <h4 style="color: #333; margin-top: 0;">系统状态</h4>
      <div class="debug-item">
        <span class="debug-label">登录状态：</span>
        <span :class="['debug-value', userStore.isLoggedIn ? 'success' : 'warning']">
          {{ userStore.isLoggedIn ? '已登录' : '未登录' }}
        </span>
      </div>
      <div class="debug-item">
        <span class="debug-label">Token状态：</span>
        <span :class="['debug-value', userStore.token ? 'success' : 'warning']">
          {{ userStore.token ? '已存在' : '不存在' }}
        </span>
      </div>
      <div class="debug-item">
        <span class="debug-label">用户信息：</span>
        <span class="debug-value">{{ userStore.userInfo ? '已存在' : '不存在' }}</span>
      </div>
      <el-button type="info" size="small" @click="refreshData" style="margin-top: 10px; margin-right: 10px;">
        刷新数据
      </el-button>
      <el-button type="warning" size="small" @click="generateMockToken" style="margin-top: 10px;">
        生成模拟Token
      </el-button>
    </div>
    
    <el-card class="profile-card">
      <h2 class="section-title">基本信息</h2>
      <div class="info-item">
        <span class="info-label">姓名：</span>
        <span class="info-value">{{ userInfo.name || '未设置' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">手机号：</span>
        <span class="info-value">{{ userInfo.phone || '未设置' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">所在位置：</span>
        <span class="info-value">{{ userInfo.address || '未设置' }}</span>
      </div>
    </el-card>
    
    <el-card class="profile-card">
      <h2 class="section-title">机主信息</h2>
      
      <!-- 头像和基本信息 -->
      <div class="owner-basic-info">
        <el-avatar 
          v-if="userInfo.avatar" 
          :src="userInfo.avatar"
          size="large"
          class="owner-avatar"
        ></el-avatar>
        <el-avatar 
          v-else 
          size="large"
          class="owner-avatar"
        >{{ userInfo.name ? userInfo.name.charAt(0) : '机' }}</el-avatar>
        <div class="owner-name">{{ userInfo.name || '未设置姓名' }}</div>
      </div>
      
      <div class="info-item">
        <span class="info-label">常用区域：</span>
        <span class="info-value">{{ ownerInfo.frequentAreas || '未设置' }}</span>
      </div>
      
      <div class="info-item">
        <span class="info-label">运营执照类型：</span>
        <span class="info-value">{{ ownerInfo.licenseTypeDesc || ownerInfo.licenseType || '未设置' }}</span>
      </div>
      
      <div class="info-item">
        <span class="info-label">执照编号：</span>
        <span class="info-value">{{ ownerInfo.licenseNumber || '未设置' }}</span>
      </div>
      
      <div class="info-item">
        <span class="info-label">审核状态：</span>
        <span class="info-value" :class="statusClass">
          {{ ownerInfo.auditStatusDesc || statusText || '未设置' }}
        </span>
      </div>
      
      <!-- 拒绝原因 -->
        <div v-if="ownerInfo.auditStatus === 2 && ownerInfo.rejectReason" class="info-item reject-reason">
          <span class="info-label">拒绝原因：</span>
          <span class="info-value reject-text">{{ ownerInfo.rejectReason }}</span>
        </div>
      
      <div class="info-item">
        <span class="info-label">设备总数：</span>
        <span class="info-value">{{ ownerInfo.deviceTotal }}</span>
      </div>
      
      <div class="info-item">
        <span class="info-label">可用设备数：</span>
        <span class="info-value">{{ ownerInfo.availableDeviceCount }}</span>
      </div>
      
      <div class="info-item">
        <span class="info-label">信用分：</span>
        <span class="info-value credit-score">{{ ownerInfo.creditScore }}</span>
      </div>
      
      <!-- 执照照片展示 -->
      <div v-if="ownerInfo.licenseUrls && ownerInfo.licenseUrls.length > 0" class="info-item">
        <span class="info-label">执照照片：</span>
        <div class="license-photos">
          <div v-for="(url, index) in ownerInfo.licenseUrls" :key="index" class="license-photo-item">
            <el-image :src="url" fit="cover" :preview-src-list="ownerInfo.licenseUrls"></el-image>
          </div>
        </div>
      </div>
    </el-card>
    
    <el-card class="profile-card">
      <h2 class="section-title">修改密码</h2>
      <el-button type="default" @click="handlePasswordChange">修改密码</el-button>
    </el-card>
    
    <!-- 编辑个人资料对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑个人资料"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="ownerFormRef"
        :model="editForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        
        <el-form-item label="所在位置" prop="address">
          <el-input v-model="editForm.address" placeholder="请输入地址" @focus="showAddressPicker"></el-input>
          <el-button type="primary" size="small" @click="showAddressPicker" style="margin-top: 10px;">选择地址</el-button>
        </el-form-item>
        
        <el-form-item label="头像">
          <!-- 统一的上传组件，支持点击头像上传 -->
          <el-upload
            v-model:file-list="avatarFiles"
            class="upload-demo"
            action="http://localhost:8082/api/file/upload"
            :limit="1"
            :on-success="handleAvatarSuccess"
            :before-remove="handleBeforeRemove"
            list-type="picture-card"
            :headers="uploadHeaders"
          >
            <!-- 自定义上传按钮内容 -->
            <template #default>
              <!-- 如果有头像，显示头像 -->
              <div v-if="avatarFiles.length > 0" class="avatar-upload-container">
                <el-image
                  :src="avatarFiles[0].url"
                  fit="cover"
                  class="avatar-upload-preview"
                />
                <div class="avatar-upload-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
              <!-- 如果没有头像，显示加号按钮 -->
              <div v-else class="avatar-upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传头像</span>
              </div>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                点击上传或更换头像
              </div>
            </template>
          </el-upload>
          <el-dialog v-model="imagePreviewVisible">
            <img width="100%" :src="dialogImageUrl" alt="preview" />
          </el-dialog>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交修改</el-button>
      </template>
    </el-dialog>
    
    <!-- 地址选择器对话框 -->
    <el-dialog
      v-model="addressPickerVisible"
      title="选择地址"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="address-picker-container">
        <!-- 腾讯地图API加载失败时的备选方案 -->
        <div v-if="typeof qq === 'undefined'" class="api-fallback">
          <el-alert
            title="地图API加载失败"
            type="warning"
            description="请手动输入地址信息"
            show-icon
          />
          <el-form-item label="详细地址">
            <el-input 
              v-model="manualAddress"
              placeholder="请输入详细地址，如：北京市海淀区中关村南大街5号"
              rows="3"
              type="textarea"
            />
          </el-form-item>
        </div>
        
        <!-- 腾讯地图API加载成功时的正常界面 -->
        <div v-else>
          <div class="address-search">
            <el-input v-model="addressSearchText" placeholder="输入地址关键词" @input="handleAddressSearch" clearable>
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <div v-if="searchResults.length > 0" class="search-results">
              <div 
                v-for="result in searchResults" 
                :key="result.id"
                class="search-result-item"
                @click="selectAddressFromSearch(result)"
              >
                {{ result.title }}
                <span class="result-address">{{ result.address }}</span>
              </div>
            </div>
          </div>
          <div class="map-container" id="mapContainer"></div>
          <div class="selected-address" v-if="selectedAddress">
            <div class="selected-address-info">
              <div class="address-title">{{ selectedAddress.title }}</div>
              <div class="address-detail">{{ selectedAddress.address }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="addressPickerVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmAddress"
          :disabled="(!selectedAddress && typeof qq !== 'undefined') || (!manualAddress && typeof qq === 'undefined')"
        >
          确认选择
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑资质信息对话框 -->
    <el-dialog
      v-model="qualificationDialogVisible"
      title="编辑资质信息"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-form
        ref="qualificationFormRef"
        :model="qualificationForm"
        :rules="qualificationFormRules"
        label-width="120px"
      >
        <el-form-item label="运营执照类型" prop="licenseType">
          <el-select v-model="qualificationForm.licenseType" placeholder="请选择执照类型">
            <el-option label="个人" value="INDIVIDUAL"></el-option>
            <el-option label="企业" value="ENTERPRISE"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="执照编号" prop="licenseNumber">
          <el-input v-model="qualificationForm.licenseNumber" placeholder="请输入执照编号"></el-input>
        </el-form-item>
        
        <el-form-item label="常用作业区域" prop="commonArea">
          <el-input v-model="qualificationForm.commonArea" placeholder="请输入常用作业区域" @focus="showAddressPickerForQualification"></el-input>
          <el-button type="primary" size="small" @click="showAddressPickerForQualification" style="margin-top: 10px;">选择地址</el-button>
        </el-form-item>
        
        <el-form-item label="执照照片">
          <div v-if="qualificationForm.licenseUrls && qualificationForm.licenseUrls.length > 0" class="license-photos">
            <div v-for="(url, index) in qualificationForm.licenseUrls" :key="index" class="license-photo-item">
              <el-image :src="url" fit="cover" :preview-src-list="qualificationForm.licenseUrls"></el-image>
            </div>
          </div>
          <el-upload
            v-model:file-list="licenseFiles"
            class="upload-demo"
            action="http://localhost:8082/api/file/upload"
            :limit="3"
            multiple
            :on-success="handleLicenseFileSuccess"
            :before-remove="handleBeforeRemove"
            list-type="picture-card"
            :on-exceed="handleExceed"
            :headers="uploadHeaders"
          >
            <el-icon><Plus /></el-icon>
            <template #file="{ file }">
              <img :src="file.url" class="el-upload-list__item-thumbnail" />
              <span class="el-upload-list__item-actions">
                <span class="el-upload-list__item-preview" @click="handlePictureCardPreview(file)">
                  <el-icon><ZoomIn /></el-icon>
                </span>
                <span class="el-upload-list__item-delete" @click="handleLicenseFileRemove(file)">
                  <el-icon><Delete /></el-icon>
                </span>
              </span>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                最多上传3张照片，请上传清晰的执照照片
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="qualificationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleQualificationSubmit">提交修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ZoomIn, Delete, Camera } from '@element-plus/icons-vue'
import axios from '../utils/axios'

export default {
  name: 'Profile',
  setup() {
    const userStore = useUserStore()
    
    // 用户基本信息
    const userInfo = ref({
      name: '',
      phone: '',
      address: '',
      avatar: ''
    })
    
    // 机主信息
    const ownerInfo = ref({
      frequentAreas: '',
      licenseType: '',
      licenseNumber: '',
      auditStatus: '',
      licenseUrls: [],
      realLicenseNumber: ''
    })
    
    // 编辑对话框相关
    const dialogVisible = ref(false)
    const imagePreviewVisible = ref(false)
    const dialogImageUrl = ref('')
    const avatarFiles = ref([])
    const ownerFormRef = ref(null)
    const editForm = ref({
      realName: '',
      address: '',
      avatar: ''
    })
    
    // 资质管理对话框相关
    const qualificationDialogVisible = ref(false)
    const licenseFiles = ref([])
    const qualificationFormRef = ref(null)
    const qualificationForm = ref({
      licenseType: '',
      licenseNumber: '',
      commonArea: '',
      licenseUrls: []
    })
    
    // 地址选择器相关
    const addressPickerVisible = ref(false)
    const addressSearchText = ref('')
    const searchResults = ref([])
    const selectedAddress = ref(null)
    const manualAddress = ref('')
    const mapInstance = ref(null)
    const marker = ref(null)
    
    // 上传请求头
    const uploadHeaders = computed(() => {
      const token = localStorage.getItem('token')
      return token ? {
        'Authorization': `Bearer ${token}`
      } : {}
    })
    
    // 表单验证规则
    const formRules = {
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' },
        { max: 50, message: '姓名长度不能超过50个字符', trigger: 'blur' }
      ],
      address: [
        { required: true, message: '请输入地址', trigger: 'blur' }
      ]
    }
    
    // 资质表单验证规则
    const qualificationFormRules = {
      licenseType: [
        { required: true, message: '请选择执照类型', trigger: 'change' }
      ],
      licenseNumber: [
        { required: true, message: '请输入执照编号', trigger: 'blur' },
        { max: 50, message: '执照编号长度不能超过50个字符', trigger: 'blur' }
      ],
      commonArea: [
        { required: true, message: '请输入常用作业区域', trigger: 'blur' }
      ]
    }
    
    // 计算属性 - 状态文本
    const statusText = computed(() => {
      const status = ownerInfo.value.auditStatus
      const statusMap = {
        'pending': '待审核',
        'approved': '审核通过',
        'rejected': '审核拒绝',
        0: '待审核',
        1: '审核通过',
        2: '审核拒绝'
      }
      return statusMap[status] || '未设置'
    })
    
    // 计算属性 - 状态样式类
    const statusClass = computed(() => {
      const status = ownerInfo.value.auditStatus
      return {
        'status-pending': status === 'pending' || status === 0,
        'status-approved': status === 'approved' || status === 1,
        'status-rejected': status === 'rejected' || status === 2
      }
    })
    
    // 刷新数据函数
    const refreshData = async () => {
      console.log('用户手动点击刷新数据')
      await fetchUserProfile()
    }
    
    // 获取用户资料
    const fetchUserProfile = async () => {
      console.log('=====================================')
      console.log('开始获取用户资料')
      console.log('当前userStore状态:', {
        isLoggedIn: userStore.isLoggedIn,
        hasToken: !!userStore.token,
        hasUserInfo: !!userStore.userInfo,
        tokenValue: userStore.token ? userStore.token.substring(0, 10) + '...' : '无token'
      })
      
      // 检查axios实例配置
      console.log('axios实例配置:', {
        baseURL: axios.defaults.baseURL,
        timeout: axios.defaults.timeout
      })
      
      // 直接测试API调用
        try {
          console.log('1. 直接测试获取用户信息API (/user/info)')
          console.log('当前localStorage中的token:', localStorage.getItem('token') ? '存在' : '不存在')
          console.log('当前userStore中的token:', userStore.token ? '存在' : '不存在')
          
          // 直接使用axios实例，让拦截器自动处理Authorization头
          // 使用正确的API路径，移除/api前缀以避免与baseURL重复
          const directUserInfoResponse = await axios.get('/user/info')
        console.log('直接API响应成功:', JSON.stringify(directUserInfoResponse))
        
        // 更新用户信息
        if (directUserInfoResponse && directUserInfoResponse.data) {
            const userData = directUserInfoResponse.data.data || directUserInfoResponse.data
            userInfo.value = {
            name: userData.realName || userData.username || userData.name || '',
            phone: userData.phone || '',
            address: userData.address || '',
            avatar: userData.avatar || ''
          }
          console.log('更新后的用户基本信息:', JSON.stringify(userInfo.value))
        }
      } catch (directError) {
        console.error('直接API调用失败:', directError)
        console.error('错误详情:', directError.response ? JSON.stringify(directError.response) : directError.message)
        console.error('错误状态:', directError.response ? directError.response.status : '未知')
      }
      
      // 然后尝试通过userStore获取（如果直接调用失败的话）
      if (!userInfo.value.name && !userInfo.value.phone) {
        try {
          console.log('2. 尝试通过userStore获取用户信息')
          const userInfoResponse = await userStore.getUserInfo()
          console.log('userStore.getUserInfo()返回:', JSON.stringify(userInfoResponse))
          
          if (userInfoResponse && userInfoResponse.data) {
          userInfo.value = {
            name: userInfoResponse.data.realName || userInfoResponse.data.username || userInfoResponse.data.name || '',
            phone: userInfoResponse.data.phone || '',
            address: userInfoResponse.data.address || '',
            avatar: userInfoResponse.data.avatar || ''
          }
            console.log('通过userStore更新后的用户信息:', JSON.stringify(userInfo.value))
          }
        } catch (storeError) {
          console.error('userStore.getUserInfo()失败:', storeError)
          console.error('错误详情:', storeError.response ? JSON.stringify(storeError.response) : storeError.message)
        }
      }
      
      // 尝试获取机主信息
      try {
        console.log('3. 尝试获取机主信息(/owner/detail)')
        console.log('当前localStorage中的token:', localStorage.getItem('token') ? '存在' : '不存在')
        console.log('当前userStore中的token:', userStore.token ? '存在' : '不存在')
        
        // 直接使用axios实例，让拦截器自动处理Authorization头
        const ownerResponse = await axios.get('/owner/detail')
        console.log('机主信息API响应:', JSON.stringify(ownerResponse.data))
        
        if (ownerResponse.data) {
          const ownerData = ownerResponse.data
          ownerInfo.value = {
            frequentAreas: ownerData.commonArea || '',
            licenseType: ownerData.licenseType || '',
            licenseTypeDesc: ownerData.licenseTypeDesc || '',
            licenseNumber: ownerData.licenseNumber || '',
            auditStatus: ownerData.auditStatus || 0,
            auditStatusDesc: ownerData.auditStatusDesc || '',
            licenseUrls: ownerData.licenseUrls || [],
            realLicenseNumber: ownerData.licenseNumber || '',
            deviceTotal: ownerData.deviceTotal || 0,
            availableDeviceCount: ownerData.availableDeviceCount || 0,
            creditScore: ownerData.creditScore || 0,
            rejectReason: ownerData.rejectReason || '',
            realName: ownerData.realName || '',
            phone: ownerData.phone || '',
            avatar: ownerData.avatar || ''
          }
          console.log('更新后的机主信息:', JSON.stringify(ownerInfo.value))
          
          // 调用设备统计API获取实时设备数量
          try {
            console.log('4. 尝试获取设备统计信息(/owner/device/stat)')
            const deviceStatResponse = await axios.get('/owner/device/stat')
            console.log('设备统计API响应:', JSON.stringify(deviceStatResponse.data))
            
            if (deviceStatResponse.data && deviceStatResponse.data.total !== undefined) {
              const deviceStatData = deviceStatResponse.data
              // 更新设备总数和可用设备数
              ownerInfo.value.deviceTotal = deviceStatData.total || 0
              ownerInfo.value.availableDeviceCount = deviceStatData.available || 0
              console.log('更新后的设备统计信息:', {
                deviceTotal: ownerInfo.value.deviceTotal,
                availableDeviceCount: ownerInfo.value.availableDeviceCount
              })
            }
          } catch (deviceStatError) {
            console.error('获取设备统计信息失败:', deviceStatError)
            console.error('错误详情:', deviceStatError.response ? JSON.stringify(deviceStatError.response) : deviceStatError.message)
            // 保留原有机主信息中的设备数量作为后备
            console.log('使用机主信息中的设备数量作为后备')
          }
        }
      } catch (ownerError) {
        console.error('获取机主信息失败:', ownerError)
        console.error('错误详情:', ownerError.response ? JSON.stringify(ownerError.response) : ownerError.message)
      }
      
      // 尝试一个简单的GET请求，看看网络是否正常
      try {
        console.log('4. 测试网络连接是否正常')
        const testResponse = await fetch('/')
        console.log('网络测试状态:', testResponse.status)
      } catch (networkError) {
        console.error('网络连接异常:', networkError.message)
      }
      
      console.log('用户资料获取完成')
      console.log('最终用户信息:', JSON.stringify(userInfo.value))
      console.log('最终机主信息:', JSON.stringify(ownerInfo.value))
      console.log('=====================================')
    }
    
    // 编辑个人资料
    const handleEdit = () => {
      // 填充编辑表单
      editForm.value = {
        realName: userInfo.value.name || '',
        address: userInfo.value.address || '',
        avatar: userInfo.value.avatar || ''
      }
      
      // 重置文件列表
      avatarFiles.value = userInfo.value.avatar ? [{
        name: userInfo.value.avatar.split('/').pop(),
        url: userInfo.value.avatar
      }] : []
      
      // 打开对话框
      dialogVisible.value = true
      // 延迟重置表单验证状态
      setTimeout(() => {
        if (ownerFormRef.value) {
          ownerFormRef.value.clearValidate()
        }
      }, 100)
    }
    
    // 编辑资质信息
    const showQualificationEdit = () => {
      // 填充编辑表单
      qualificationForm.value = {
        licenseType: ownerInfo.value.licenseType || '',
        licenseNumber: ownerInfo.value.licenseNumber || ownerInfo.value.realLicenseNumber || '',
        commonArea: ownerInfo.value.frequentAreas || '',
        licenseUrls: [...(ownerInfo.value.licenseUrls || [])]
      }
      
      // 重置文件列表
      licenseFiles.value = (ownerInfo.value.licenseUrls || []).map(url => ({
        name: url.split('/').pop(),
        url: url
      }))
      
      // 打开对话框
      qualificationDialogVisible.value = true
      // 延迟重置表单验证状态
      setTimeout(() => {
        if (qualificationFormRef.value) {
          qualificationFormRef.value.clearValidate()
        }
      }, 100)
    }
    
    // 处理文件选择
    const handleFileChange = (file, fileList) => {
      // 这里可以处理文件上传逻辑
      // 目前只是简单地更新表单中的文件列表
      console.log('选择的文件:', fileList)
    }
    
    const handleAvatarSuccess = (response, uploadFile) => {
      if (response.code === 200) {
        editForm.value.avatar = response.data
        // 更新文件列表，替换旧头像
        avatarFiles.value = [{
          name: uploadFile.name,
          url: response.data
        }]
        ElMessage.success('上传成功')
      } else {
        ElMessage.error('上传失败，请重试')
      }
    }
    
    // 处理执照文件上传成功
    const handleLicenseFileSuccess = (response, uploadFile) => {
      if (response.code === 200) {
        qualificationForm.value.licenseUrls.push(response.data)
        ElMessage.success('上传成功')
      } else {
        ElMessage.error('上传失败，请重试')
      }
    }
    
    // 处理执照文件删除
    const handleLicenseFileRemove = (file) => {
      const index = qualificationForm.value.licenseUrls.indexOf(file.url)
      if (index !== -1) {
        qualificationForm.value.licenseUrls.splice(index, 1)
      }
    }
    
    // 为资质信息打开地址选择器
    const showAddressPickerForQualification = () => {
      addressPickerVisible.value = true
      // 延迟初始化地图，确保对话框已渲染
      setTimeout(() => {
        if (typeof qq !== 'undefined') {
          try {
            initMap()
          } catch (error) {
            console.error('地图初始化失败:', error)
          }
        }
      }, 100)
    }
    
    // 提交资质信息
    const handleQualificationSubmit = async () => {
      // 表单验证
      if (!qualificationFormRef.value) return
      
      try {
        await qualificationFormRef.value.validate()
        
        // 准备提交数据
        const submitData = {
          licenseType: qualificationForm.value.licenseType,
          licenseNumber: qualificationForm.value.licenseNumber,
          licenseUrls: qualificationForm.value.licenseUrls,
          commonArea: qualificationForm.value.commonArea
        }
        
        console.log('提交的资质信息:', submitData)
        
        // 调用API提交数据
        const response = await axios.post('/owner/qualification/submit', submitData)
        console.log('提交响应:', response)
        
        if (response && response.code === 200) {
          ElMessage.success(response.message || '资质资料提交成功，请等待审核')
          qualificationDialogVisible.value = false
          // 刷新数据
          await fetchUserProfile()
        } else {
          ElMessage.error(response?.message || '提交失败，请稍后重试')
        }
      } catch (error) {
        console.error('提交资质信息失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
      }
    }
    

    
    const handleBeforeRemove = (file, fileList) => {
      return ElMessageBox.confirm(`确定移除 ${file.name}？`)
    }
    
    const handleExceed = (files, fileList) => {
      ElMessage.warning(`当前限制选择 ${fileList.length} 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`)
    }
    
    const handlePictureCardPreview = (file) => {
      dialogImageUrl.value = file.url
      imagePreviewVisible.value = true
    }
    
    const handleRemove = (file) => {
      // 清除头像
      editForm.value.avatar = ''
      // 清空文件列表
      avatarFiles.value = []
    }
    

    
    // 提交编辑表单
    const handleSubmit = async () => {
      // 表单验证
      if (!ownerFormRef.value) return
      
      try {
        await ownerFormRef.value.validate()
        
        // 准备提交数据
        const submitData = {
          realName: editForm.value.realName,
          address: editForm.value.address,
          avatar: editForm.value.avatar
        }
        
        console.log('提交的个人信息:', submitData)
        
        // 调用API提交数据
        const response = await axios.put('/user/info', submitData)
        console.log('提交响应:', response)
        
        if (response && response.code === 200) {
          ElMessage.success(response.message || '个人信息更新成功')
          dialogVisible.value = false
          // 刷新数据
          await fetchUserProfile()
        } else {
          ElMessage.error(response?.message || '提交失败，请稍后重试')
        }
      } catch (error) {
        console.error('提交机主信息失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
      }
    }
    
    // 修改密码
    const handlePasswordChange = () => {
      // 这里可以跳转到修改密码页面或打开对话框
      console.log('修改密码')
      ElMessage.info('修改密码功能待实现')
    }
    
    // 生成模拟Token（用于测试）
    const generateMockToken = () => {
      console.log('生成模拟Token...')
      // 生成一个随机的模拟token
      const mockToken = 'mock-token-' + Math.random().toString(36).substring(2, 15)
      
      // 保存到localStorage
      localStorage.setItem('token', mockToken)
      
      // 更新userStore
      userStore.token = mockToken
      userStore.isLoggedIn = true
      
      // 设置模拟用户信息
      const mockUserInfo = {
        userId: '1',
        name: '测试用户',
        phone: '13800138000'
      }
      localStorage.setItem('userInfo', JSON.stringify(mockUserInfo))
      userStore.userInfo = mockUserInfo
      
      ElMessage.success('模拟Token已生成并保存')
      console.log('生成的模拟Token:', mockToken)
      console.log('更新后的登录状态:', userStore.isLoggedIn)
      
      // 刷新页面数据
      fetchUserProfile()
    }
    
    // 显示地址选择器
    const showAddressPicker = () => {
      // 重置状态
      selectedAddress.value = null
      manualAddress.value = ''
      searchResults.value = []
      
      addressPickerVisible.value = true
      
      // 延迟初始化地图，确保对话框已渲染
      setTimeout(() => {
        if (typeof qq !== 'undefined') {
          try {
            initMap()
          } catch (error) {
            console.error('地图初始化失败:', error)
          }
        }
      }, 100)
    }
    
    // 初始化地图
    const initMap = () => {
      const mapContainer = document.getElementById('mapContainer')
      if (!mapContainer) return
      
      try {
        // 清除旧地图实例
        if (mapInstance.value) {
          mapInstance.value.destroy()
        }
        
        // 创建新地图实例
        mapInstance.value = new qq.maps.Map(mapContainer, {
          center: new qq.maps.LatLng(39.916527, 116.397128), // 默认北京
          zoom: 13,
          mapTypeId: qq.maps.MapTypeId.ROADMAP
        })
        
        // 添加点击事件，用于选择地图上的点
        qq.maps.event.addListener(mapInstance.value, 'click', (event) => {
          const latLng = event.latLng
          // 根据坐标获取地址信息
          getAddressFromLatLng(latLng)
        })
      } catch (error) {
        console.error('地图初始化失败:', error)
        ElMessage.error('地图初始化失败，请重试')
      }
    }
    
    // 根据坐标获取地址信息
    const getAddressFromLatLng = (latLng) => {
      if (typeof qq === 'undefined') {
        ElMessage.error('地图API加载失败，请刷新页面重试')
        return
      }
      
      try {
        const geocoder = new qq.maps.Geocoder({
          complete: (result) => {
            const addressComponent = result.detail.addressComponent
            const address = result.detail.address
            const title = addressComponent.district + addressComponent.street + addressComponent.streetNumber
            
            selectedAddress.value = {
              title: title,
              address: address,
              lat: latLng.getLat(),
              lng: latLng.getLng()
            }
            
            // 更新地图标记
            updateMarker(latLng)
          },
          error: (error) => {
            console.error('获取地址失败:', error)
            ElMessage.error('获取地址信息失败，请重试')
          }
        })
        
        geocoder.getAddress(latLng)
      } catch (error) {
        console.error('获取地址失败:', error)
        ElMessage.error('获取地址信息失败，请重试')
      }
    }
    
    // 更新地图标记
    const updateMarker = (latLng) => {
      if (typeof qq === 'undefined' || !mapInstance.value) {
        return
      }
      
      try {
        if (marker.value) {
          marker.value.setPosition(latLng)
        } else {
          marker.value = new qq.maps.Marker({
            position: latLng,
            map: mapInstance.value
          })
        }
        mapInstance.value.setCenter(latLng)
      } catch (error) {
        console.error('更新标记失败:', error)
      }
    }
    
    // 处理地址搜索
    const handleAddressSearch = () => {
      if (!addressSearchText.value.trim()) {
        searchResults.value = []
        return
      }
      
      if (typeof qq === 'undefined') {
        ElMessage.error('地图API加载失败，请刷新页面重试')
        return
      }
      
      try {
        const searchService = new qq.maps.SearchService({
          complete: (result) => {
            searchResults.value = result.detail.pois.map(item => ({
              id: item.id,
              title: item.title,
              address: item.address,
              lat: item.location.lat,
              lng: item.location.lng
            }))
          },
          error: (error) => {
            console.error('搜索失败:', error)
            ElMessage.error('搜索失败，请重试')
          }
        })
        
        searchService.search(addressSearchText.value)
      } catch (error) {
        console.error('搜索失败:', error)
        ElMessage.error('搜索失败，请重试')
      }
    }
    
    // 从搜索结果中选择地址
    const selectAddressFromSearch = (result) => {
      selectedAddress.value = result
      // 更新地图位置
      if (typeof qq !== 'undefined') {
        try {
          const latLng = new qq.maps.LatLng(result.lat, result.lng)
          updateMarker(latLng)
        } catch (error) {
          console.error('更新地图位置失败:', error)
        }
      }
      searchResults.value = []
    }
    
    // 确认选择地址
    const confirmAddress = () => {
      let addressValue = ''
      
      if (typeof qq !== 'undefined') {
        if (!selectedAddress.value) return
        addressValue = selectedAddress.value.address
      } else {
        if (!manualAddress.value) return
        addressValue = manualAddress.value
      }
      
      // 根据当前打开的对话框更新对应的表单字段
      if (qualificationDialogVisible.value) {
        qualificationForm.value.commonArea = addressValue
      } else {
        editForm.value.address = addressValue
      }
      
      addressPickerVisible.value = false
      
      // 清除搜索结果和选中地址
      searchResults.value = []
      selectedAddress.value = null
      manualAddress.value = ''
    }
    
    // 组件挂载时获取数据
    onMounted(() => {
      fetchUserProfile()
    })
    
    // 组件卸载时清理
    onUnmounted(() => {
      // 销毁地图实例
      if (mapInstance.value) {
        try {
          mapInstance.value.destroy()
        } catch (error) {
          console.error('销毁地图实例失败:', error)
        }
      }
    })
    
    return {
      userInfo,
      ownerInfo,
      statusText,
      statusClass,
      handleEdit,
      handlePasswordChange,
      refreshData,
      generateMockToken,
      userStore,
      dialogVisible,
      imagePreviewVisible,
      ownerFormRef,
      editForm,
      formRules,
      handleFileChange,
      handleSubmit,
      uploadHeaders,
      avatarFiles,
      handleAvatarSuccess,
      handlePictureCardPreview,
      handleRemove,
      handleBeforeRemove,
      handleExceed,
      // 资质管理相关
      qualificationDialogVisible,
      licenseFiles,
      qualificationFormRef,
      qualificationForm,
      qualificationFormRules,
      showQualificationEdit,
      handleLicenseFileSuccess,
      handleLicenseFileRemove,
      handleQualificationSubmit,
      showAddressPickerForQualification,
      // 地址选择器相关
      addressPickerVisible,
      addressSearchText,
      searchResults,
      selectedAddress,
      manualAddress,
      showAddressPicker,
      handleAddressSearch,
      selectAddressFromSearch,
      confirmAddress,
      Search
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.profile-title {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.profile-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.info-item {
  margin-bottom: 16px;
  font-size: 14px;
}

.info-label {
  display: inline-block;
  width: 100px;
  color: #909399;
}

.info-value {
  color: #303133;
}

.status-pending {
  color: #e6a23c;
}

.status-approved {
  color: #67c23a;
}

.status-rejected {
  color: #f56c6c;
}

.debug-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.debug-label {
  font-weight: bold;
  margin-right: 10px;
  min-width: 80px;
}

.debug-value {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.debug-value.success {
  background-color: #f0f9ff;
  color: #0984e3;
}

.debug-value.warning {
  background-color: #fff3cd;
  color: #856404;
}
</style>

<style scoped>
.license-photos {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.license-photo-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #e0e0e0;
}

.license-photo-item :deep(.el-image) {
  width: 100%;
  height: 100%;
}

/* 机主基本信息样式 */
.owner-basic-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.owner-avatar {
  margin-bottom: 10px;
}

.owner-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

/* 信用分样式 */
.credit-score {
  font-weight: bold;
  color: #ff6b6b;
}

/* 拒绝原因样式 */
.reject-reason {
  background-color: #fef0f0;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
}

.reject-text {
  color: #f56c6c;
  font-weight: 500;
}

/* 头像上传样式 */
.avatar-upload-container {
  position: relative;
  width: 148px;
  height: 148px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.avatar-upload-preview {
  width: 100%;
  height: 100%;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-upload-container:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-upload-overlay .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.avatar-upload-placeholder {
  width: 148px;
  height: 148px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #999;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-upload-placeholder:hover {
  border-color: #409eff;
  color: #409eff;
}

.avatar-upload-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 地址选择器样式 */
.address-picker-container {
  width: 100%;
}

.api-fallback {
  margin-bottom: 20px;
}

.api-fallback .el-alert {
  margin-bottom: 20px;
}

.address-search {
  margin-bottom: 20px;
  position: relative;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  padding: 10px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.result-address {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.map-container {
  width: 100%;
  height: 400px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 20px;
}

.selected-address {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.selected-address-info {
  margin-top: 10px;
}

.address-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.address-detail {
  font-size: 14px;
  color: #606266;
}
</style>