<template>
  <div class="edit-device-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">编辑设备信息</h1>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item><a href="/dashboard">首页</a></el-breadcrumb-item>
        <el-breadcrumb-item><a href="/devices">设备管理</a></el-breadcrumb-item>
        <el-breadcrumb-item>编辑设备</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 主内容卡片 -->
    <el-card class="device-form-card">
      <el-form 
        ref="deviceFormRef" 
        :model="deviceForm" 
        :rules="rules" 
        label-width="120px"
        class="device-form"
        v-loading="loading"
      >
        <!-- 设备名称 -->
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="deviceForm.deviceName" placeholder="请输入设备名称"></el-input>
        </el-form-item>
        
        <!-- 设备型号 -->
        <el-form-item label="设备型号" prop="model">
          <el-input v-model="deviceForm.model" placeholder="请输入设备型号"></el-input>
        </el-form-item>
        
        <!-- 设备类型 -->
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="deviceForm.deviceType" placeholder="请选择设备类型">
            <el-option label="航拍无人机" value="航拍无人机"></el-option>
            <el-option label="测绘无人机" value="测绘无人机"></el-option>
            <el-option label="喷洒无人机" value="喷洒无人机"></el-option>
            <el-option label="巡检无人机" value="巡检无人机"></el-option>
            <el-option label="其他类型" value="其他类型"></el-option>
          </el-select>
        </el-form-item>
        
        <!-- 序列号 -->
        <el-form-item label="序列号" prop="serialNumber">
          <el-input v-model="deviceForm.serialNumber" placeholder="请输入设备序列号"></el-input>
        </el-form-item>
        
        <!-- 购买日期 -->
        <el-form-item label="购买日期" prop="purchaseDate">
          <el-date-picker
            v-model="deviceForm.purchaseDate"
            type="date"
            placeholder="选择购买日期"
            style="width: 100%;"
          ></el-date-picker>
        </el-form-item>
        
        <!-- 设备状态 -->
        <el-form-item label="设备状态" prop="status">
          <el-radio-group v-model="deviceForm.status">
            <el-radio label="1">正常</el-radio>
            <el-radio label="2">维护中</el-radio>
            <el-radio label="3">故障</el-radio>
            <el-radio label="4">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 设备图片 -->
        <el-form-item label="设备图片">
          <el-upload
            class="avatar-uploader"
            :action="''"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleImageChange"
            accept="image/*"
          >
            <template #default>
              <img v-if="imageUrl" :src="imageUrl" class="avatar" />
              <div v-else class="avatar-uploader-icon">
                <el-icon><Plus /></el-icon>
                <div>上传图片</div>
              </div>
            </template>
          </el-upload>
          <div class="image-hint">支持JPG、PNG格式，建议尺寸不超过1024x1024，文件大小不超过5MB</div>
        </el-form-item>
        
        <!-- 备注 -->
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="deviceForm.remark" 
            type="textarea" 
            placeholder="请输入备注信息"
            :rows="3"
          ></el-input>
        </el-form-item>
        
        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { Plus } from '@element-plus/icons-vue'
import axios from '../utils/axios'

export default {
  name: 'EditDevice',
  components: {
    Plus
  },
  mounted() {
    // 设置正确的baseURL以匹配后端实际端口，不包含/api前缀
    axios.defaults.baseURL = 'http://localhost:8082'
    this.fetchDeviceDetail()
  },
  data() {
    return {
      loading: false,
      submitting: false,
      imageUrl: '',
      imageFile: null,
      deviceForm: {
        deviceId: '',
        deviceName: '',
        model: '',
        deviceType: '',
        serialNumber: '',
        purchaseDate: '',
        status: '1',
        remark: '',
        imageUrl: ''
      },
      rules: {
        deviceName: [
          { required: true, message: '请输入设备名称', trigger: 'blur' },
          { min: 1, max: 50, message: '设备名称长度在1-50个字符', trigger: 'blur' }
        ],
        model: [
          { required: true, message: '请输入设备型号', trigger: 'blur' },
          { min: 1, max: 50, message: '设备型号长度在1-50个字符', trigger: 'blur' }
        ],
        deviceType: [
          { required: true, message: '请选择设备类型', trigger: 'change' }
        ],
        serialNumber: [
          { required: true, message: '请输入序列号', trigger: 'blur' }
        ],
        purchaseDate: [
          { required: true, message: '请选择购买日期', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择设备状态', trigger: 'change' }
        ]
      }
    }
  },
  // 组件挂载时的生命周期已移到export default中
  methods: {
    // 获取设备详情
    async fetchDeviceDetail() {
      const deviceId = this.$route.params.id;
      this.loading = true;
      try {
        // 这里应该调用实际的API
        console.log('加载设备数据，ID:', deviceId);
        // 模拟数据
        // 调用设备详情API获取实际数据
         const response = await axios.get(`/device/detail/${deviceId}`)
         
         // 处理API响应数据
         const apiData = response.data || {}
         const deviceInfo = apiData.deviceInfo || apiData
         
         // 映射API返回的数据到表单
         this.deviceForm = {
           deviceId: deviceInfo.device_id || deviceInfo.deviceId || deviceId,
           deviceName: deviceInfo.device_name || deviceInfo.deviceName || '',
           model: deviceInfo.model || '',
           deviceType: deviceInfo.device_type || deviceInfo.deviceType || '',
           serialNumber: deviceInfo.serialNumber || deviceInfo.device_no || '',
           purchaseDate: deviceInfo.purchaseTime ? 
             (typeof deviceInfo.purchaseTime === 'string' ? 
               deviceInfo.purchaseTime.split(' ')[0] : 
               deviceInfo.purchaseTime) : '',
           status: (deviceInfo.status || 1).toString(), // 确保是字符串格式
           remark: deviceInfo.remark || '',
           imageUrl: deviceInfo.picture || ''
         }
         
         // 设置图片预览，使用相对路径
         if (deviceInfo.picture && deviceInfo.picture.trim()) {
           // 如果不是完整URL，直接使用相对路径
           if (deviceInfo.picture.startsWith('http://') || deviceInfo.picture.startsWith('https://')) {
             this.imageUrl = deviceInfo.picture;
           } else {
             this.imageUrl = deviceInfo.picture;
           }
         } else {
           this.imageUrl = '';
         }
      } catch (error) {
        this.$message.error('获取设备详情失败');
        this.$router.push('/devices');
      } finally {
        this.loading = false;
      }
    },
    
    // 处理图片选择
    handleImageChange(file) {
      // 检查文件类型
      const isImage = file.raw.type.startsWith('image/');
      if (!isImage) {
        this.$message.error('请上传图片文件！');
        return false;
      }
      
      // 检查文件大小（5MB）
      const isLt5M = file.size / 1024 / 1024 < 5;
      if (!isLt5M) {
        this.$message.error('图片大小不能超过 5MB！');
        return false;
      }
      
      // 预览图片
      this.imageFile = file.raw;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.imageUrl = e.target.result;
      };
      reader.readAsDataURL(file.raw);
      
      // 将图片URL保存到表单中
      this.deviceForm.imageUrl = this.imageUrl;
    },
    
    // 提交表单
    async handleSubmit() {
      this.$refs.deviceFormRef.validate(async (valid) => {
        if (valid) {
          this.submitting = true;
          try {
            // 准备设备数据（移除remark字段，因为后端实体类中不存在）
            const deviceData = {
              deviceName: this.deviceForm.deviceName,
              model: this.deviceForm.model,
              deviceType: this.deviceForm.deviceType,
              serialNumber: this.deviceForm.serialNumber,
              status: parseInt(this.deviceForm.status),
              purchaseTime: this.deviceForm.purchaseDate ? new Date(this.deviceForm.purchaseDate).toISOString().split('T')[0] : null,
              // 添加picture字段，保留现有的图片路径
              picture: this.deviceForm.imageUrl || ''
            };
            
            // 注意：不再提交remark字段，因为后端DroneDevice实体类中不存在此字段
            
            // 如果有新上传的图片文件，先上传图片
            if (this.imageFile) {
              console.log('需要上传图片:', this.imageFile.name);
              const uploadFormData = new FormData();
              uploadFormData.append('file', this.imageFile);
              const uploadResponse = await axios.post('/device/image/upload', uploadFormData, {
                headers: {'Content-Type': 'multipart/form-data'}
              });
              
              // 更新deviceData中的picture字段为上传后的URL
              if (uploadResponse && uploadResponse.code === 200) {
                // axios拦截器已返回response.data，直接获取图片路径
                deviceData.picture = uploadResponse.data;
                console.log('图片上传成功，路径:', deviceData.picture);
              } else {
                console.error('图片上传返回数据格式不正确:', uploadResponse);
              }
            }
            
            console.log('准备提交设备更新数据，ID:', this.deviceForm.deviceId);
            console.log('设备数据:', deviceData);
            
            // 调用正确的设备更新API
            await axios.post(`/device/edit/${this.deviceForm.deviceId}`, deviceData);
            
            // API调用成功
            console.log('设备更新成功');
            this.$message.success('设备更新成功');
            this.$router.push('/devices');
          } catch (error) {
            this.$message.error('更新失败，请重试');
          } finally {
            this.submitting = false;
          }
        }
      });
    },
    
    // 取消操作
    handleCancel() {
      this.$router.push('/devices');
    }
  }
}
</script>

<style scoped>
.edit-device-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e6eaf0 100%);
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #303133, #606266);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 设备表单卡片 */
.device-form-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  background: #ffffff;
}

.device-form-card:hover {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

/* 表单样式 */
.device-form {
  padding: 24px;
}

.device-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.device-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.device-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.device-form :deep(.el-select__wrapper) {
  border-radius: 8px;
}

.device-form :deep(.el-date-editor) {
   border-radius: 8px;
 }

/* 图片上传样式 */
.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.avatar-uploader-icon {
  width: 120px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background-color: #f7f8fa;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-uploader-icon:hover {
  border-color: #409EFF;
  color: #409EFF;
  background-color: #ecf5ff;
}

.avatar-uploader-icon :deep(.el-icon) {
  font-size: 24px;
  margin-bottom: 8px;
}

.image-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 按钮样式 */
.device-form :deep(.el-button) {
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.device-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF, #66B1FF);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.device-form :deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #66B1FF, #409EFF);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .edit-device-page {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .device-form {
    padding: 16px;
  }
  
  .device-form :deep(.el-form-item__label) {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
   .edit-device-page {
     padding: 8px;
   }
   
   .page-title {
     font-size: 20px;
   }
   
   .device-form {
     padding: 12px;
   }
   
   .device-form :deep(.el-form-item__label) {
     font-size: 13px;
   }
   
   .device-form :deep(.el-button) {
     width: 100%;
     margin-bottom: 8px;
   }
   
   .avatar,
   .avatar-uploader-icon {
     width: 100px;
     height: 100px;
   }
 }
</style>