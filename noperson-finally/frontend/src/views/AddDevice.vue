<template>
  <div class="add-device">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>添加设备</span>
        </div>
      </template>
      
      <el-form 
        :model="deviceForm" 
        :rules="rules" 
        ref="deviceFormRef"
        label-width="120px"
        class="device-form"
      >
        <el-form-item label="设备名称" prop="deviceName">
          <el-input 
            v-model="deviceForm.deviceName" 
            placeholder="请输入设备名称"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="设备型号" prop="deviceModel">
          <el-input 
            v-model="deviceForm.deviceModel" 
            placeholder="请输入设备型号"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="设备制造商" prop="manufacturer">
          <el-input 
            v-model="deviceForm.manufacturer" 
            placeholder="请输入设备制造商"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="设备类型" prop="deviceType">
          <el-select 
            v-model="deviceForm.deviceType" 
            placeholder="请选择设备类型"
          >
            <el-option label="航拍无人机" value="航拍无人机" />
            <el-option label="测绘无人机" value="测绘无人机" />
            <el-option label="喷洒无人机" value="喷洒无人机" />
            <el-option label="巡检无人机" value="巡检无人机" />
            <el-option label="其他类型" value="其他类型" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="序列号" prop="serialNumber">
          <el-input 
            v-model="deviceForm.serialNumber" 
            placeholder="请输入设备序列号"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="购买日期" prop="purchaseDate">
          <el-date-picker
            v-model="deviceForm.purchaseDate"
            type="date"
            placeholder="选择购买日期"
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="设备状态" prop="status">
          <el-radio-group v-model="deviceForm.status">
            <el-radio label="1">正常</el-radio>
            <el-radio label="2">维护中</el-radio>
            <el-radio label="4">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="品牌" prop="brand">
          <el-select 
            v-model="deviceForm.brand" 
            placeholder="请选择品牌"
          >
            <el-option label="DJI大疆" value="DJI大疆" />
            <el-option label="Parrot派诺特" value="Parrot派诺特" />
            <el-option label="Yuneec昊翔" value="Yuneec昊翔" />
            <el-option label="Autel道通" value="Autel道通" />
            <el-option label="Skydio" value="Skydio" />
            <el-option label="极飞XAG" value="极飞XAG" />
            <el-option label="小米" value="小米" />
            <el-option label="哈博森" value="哈博森" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="最大载重(kg)" prop="maxLoad">
          <el-input 
            v-model.number="deviceForm.maxLoad" 
            type="number"
            placeholder="请输入最大载重"
            :min="0"
            step="0.1"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="续航时间(分钟)" prop="endurance">
          <el-input 
            v-model.number="deviceForm.endurance" 
            type="number"
            placeholder="请输入续航时间"
            :min="0"
            step="1"
          ></el-input>
        </el-form-item>
        
        <!-- 设备图片上传 -->
        <el-form-item label="设备图片" prop="picture">
          <el-upload
            class="avatar-uploader"
            :action="''"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleImageChange"
            accept="image/*"
          >
            <template #default>
              <img v-if="deviceForm.picture" :src="deviceForm.picture" class="avatar" />
              <div v-else class="avatar-placeholder">
                <el-icon><Plus /></el-icon>
                <div class="avatar-placeholder-text">上传设备图片</div>
              </div>
            </template>
          </el-upload>
          <div class="upload-tips">支持 JPG、PNG 格式，大小不超过 5MB</div>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSubmit"
            :loading="loading"
          >
            提交
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

export default {
  name: 'AddDevice',
  setup() {
    const router = useRouter()
    const deviceFormRef = ref(null)
    const loading = ref(false)
    
    const deviceForm = reactive({
      deviceName: '', // 设备名称
      deviceModel: '',
      deviceType: '',
      serialNumber: '',
      purchaseDate: '',
      status: '1',
      manufacturer: '', // 设备制造商
      brand: '', // 品牌
      maxLoad: '', // 最大载重(kg)
      endurance: '', // 续航时间(分钟)
      picture: '' // 设备图片URL
    })
    
    const imageUrl = ref('')
    const imageFile = ref(null)
    
    const rules = {
      deviceName: [
        { required: true, message: '请输入设备名称', trigger: 'blur' },
        { min: 1, max: 100, message: '设备名称长度在1-100个字符', trigger: 'blur' }
      ],
      deviceModel: [
        { required: true, message: '请输入设备型号', trigger: 'blur' },
        { min: 1, max: 50, message: '设备型号长度在1-50个字符', trigger: 'blur' }
      ],
      manufacturer: [
        { required: true, message: '请输入设备制造商', trigger: 'blur' },
        { min: 1, max: 50, message: '制造商名称长度在1-50个字符', trigger: 'blur' }
      ],
      deviceType: [
        { required: true, message: '请选择设备类型', trigger: 'change' }
      ],
      serialNumber: [
        { required: true, message: '请输入设备序列号', trigger: 'blur' },
        { min: 5, max: 50, message: '序列号长度在5-50个字符', trigger: 'blur' }
      ],
      purchaseDate: [
        { required: true, message: '请选择购买日期', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择设备状态', trigger: 'change' }
      ],
      brand: [
        { required: true, message: '请选择品牌', trigger: 'change' }
      ],
      maxLoad: [
        { required: true, message: '请输入最大载重', trigger: 'blur' },
        { type: 'number', min: 0, message: '最大载重必须大于等于0', trigger: 'blur' }
      ],
      endurance: [
        { required: true, message: '请输入续航时间', trigger: 'blur' },
        { type: 'number', min: 0, message: '续航时间必须大于等于0', trigger: 'blur' }
      ],
      picture: [
        { required: true, message: '请上传设备图片', trigger: 'change' }
      ]
    }
    
    // 处理图片选择
    const handleImageChange = (file) => {
      // 检查文件类型
      const isImage = file.raw.type.startsWith('image/')
      if (!isImage) {
        ElMessage.error('请上传图片文件！')
        // 清空之前的选择，避免表单验证通过
        deviceForm.picture = ''
        imageFile.value = null
        return
      }
      
      // 检查文件大小（5MB）
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB！')
        // 清空之前的选择，避免表单验证通过
        deviceForm.picture = ''
        imageFile.value = null
        return
      }
      
      // 预览图片
      imageFile.value = file.raw
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const result = e.target.result
          imageUrl.value = result
          // 直接将结果保存到表单中，确保验证能正确识别
          deviceForm.picture = result
        } catch (error) {
          console.error('图片预览失败:', error)
          ElMessage.error('图片预览失败，请重试')
          deviceForm.picture = ''
          imageFile.value = null
        }
      }
      reader.onerror = (error) => {
        console.error('文件读取失败:', error)
        ElMessage.error('文件读取失败，请重试')
        deviceForm.picture = ''
        imageFile.value = null
      }
      reader.readAsDataURL(file.raw)
    }
    
    // 提交表单
    const handleSubmit = async () => {
      try {
        // 表单验证
        await deviceFormRef.value.validate()
        
        loading.value = true
        
        // 准备设备数据
        const submitData = {
          deviceName: deviceForm.deviceName, // 设备名称
          deviceModel: deviceForm.deviceModel, // 设备型号
          serialNumber: deviceForm.serialNumber, // 设备序列号
          deviceType: deviceForm.deviceType, // 设备类型
          manufacturer: deviceForm.manufacturer, // 制造商
          status: deviceForm.status, // 设备状态
          purchaseDate: deviceForm.purchaseDate ? new Date(deviceForm.purchaseDate).toISOString().split('T')[0] : null,
          brand: deviceForm.brand, // 品牌
          maxLoad: deviceForm.maxLoad, // 最大载重(kg)
          endurance: deviceForm.endurance, // 续航时间(分钟)
          picture: deviceForm.picture // 设备图片URL
        }
        
        // 如果有新上传的图片文件，先上传图片
        if (imageFile.value) {
          console.log('需要上传图片:', imageFile.value.name)
          const uploadFormData = new FormData()
          uploadFormData.append('file', imageFile.value)
          const uploadResponse = await axios.post('/device/image/upload', uploadFormData, {
            headers: {'Content-Type': 'multipart/form-data'}
          })
          
          // 更新submitData中的picture字段为上传后的URL
          if (uploadResponse && uploadResponse.code === 200) {
            // axios拦截器已返回response.data，直接获取图片路径
            submitData.picture = uploadResponse.data
            console.log('图片上传成功，路径:', submitData.picture)
          } else {
            console.error('图片上传返回数据格式不正确:', uploadResponse)
            throw new Error('图片上传失败：' + (uploadResponse?.message || '服务器错误'))
          }
        }
        
        console.log('提交的数据:', submitData)
        console.log('准备发送请求到:', '/device/add')
        
        // 发送请求到后端API
        const response = await axios.post('/device/add', submitData)
        console.log('API响应成功:', response)
        
        if (response && response.code === 200) {
          ElMessage.success('设备添加成功！')
          router.push('/devices')
        } else {
          throw new Error('设备添加失败：' + (response?.message || '服务器错误'))
        }
      } catch (error) {
        console.error('添加设备失败:', error)
        // 处理表单验证错误
        if (error.name === 'ValidationError') {
          ElMessage.error('表单验证失败，请检查输入')
        } else {
          ElMessage.error(error.message || '添加设备失败，请重试')
        }
      } finally {
        loading.value = false
      }
    }
    
    // 取消操作
    const handleCancel = () => {
      router.push('/devices')
    }
    
    return {
      deviceForm,
      rules,
      deviceFormRef,
      loading,
      imageUrl,
      handleSubmit,
      handleCancel,
      handleImageChange,
      Plus
    }
  }
}
</script>

<style scoped>
.add-device {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-form {
  margin-top: 20px;
}

/* 图片上传样式 */
.avatar-uploader .el-upload {
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar {
  width: 200px;
  height: 150px;
  display: block;
  object-fit: cover;
}

.avatar-placeholder {
  width: 200px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.avatar-placeholder-icon {
  font-size: 28px;
  margin-bottom: 10px;
}

.avatar-placeholder-text {
  font-size: 14px;
}

.upload-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>