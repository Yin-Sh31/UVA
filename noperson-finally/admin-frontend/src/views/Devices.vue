<template>
  <div class="devices-container">
    <h2>设备管理</h2>
    <el-card>
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索设备编号或品牌" style="width: 300px; margin-right: 10px;"></el-input>
        <el-button type="primary" @click="searchDevices">搜索</el-button>
      </div>
      <el-table :data="devicesList" style="width: 100%; margin-top: 20px;">
        <el-table-column label="设备ID">
          <template #default="scope">
            {{ scope.row.deviceInfo?.deviceId || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="设备名称">
          <template #default="scope">
            {{ scope.row.deviceInfo?.deviceName || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="品牌型号">
          <template #default="scope">
            {{ scope.row.deviceInfo?.manufacturer || scope.row.deviceInfo?.brand || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="所属机主">
          <template #default="scope">
            {{ scope.row.ownerInfo?.realName || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="机主电话">
          <template #default="scope">
            {{ scope.row.ownerInfo?.phone || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="设备类型">
          <template #default="scope">
            {{ scope.row.deviceInfo?.deviceType || scope.row.deviceInfo?.type || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="购买时间">
          <template #default="scope">
            {{ scope.row.deviceInfo?.purchaseTime || scope.row.deviceInfo?.purchaseDate || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDeviceDetail(scope.row)">
              查看详情
            </el-button>
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
      ></el-pagination>
    </el-card>
    
    <!-- 设备详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="设备详情"
      width="70%"
      :before-close="handleClose"
    >
      <div class="device-detail">
        <!-- 设备图片展示 -->
        <div class="device-image-container">
          <h4>设备图片</h4>
          <img 
            v-if="selectedDevice && selectedDevice.picture"
            :src="getImageUrl(selectedDevice.picture)"
            alt="设备图片"
            class="device-image"
            @click="previewImage(selectedDevice.picture)"
          />
          <div v-else class="no-image">暂无设备图片</div>
        </div>
        
        <!-- 设备基本信息 -->
        <div class="device-info">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="设备ID">{{ selectedDevice?.deviceId || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="设备名称">{{ selectedDevice?.deviceName || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="品牌型号">{{ selectedDevice?.manufacturer || selectedDevice?.brand || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="设备型号">{{ selectedDevice?.model || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="序列号">{{ selectedDevice?.serialNumber || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="设备类型">{{ selectedDevice?.deviceType || selectedDevice?.type || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="设备状态">{{ selectedDevice?.statusDesc || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="最大负载">{{ selectedDevice?.maxLoad || '未知' }} kg</el-descriptions-item>
            <el-descriptions-item label="续航时间">{{ selectedDevice?.endurance || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="购买时间">{{ selectedDevice?.purchaseTime || selectedDevice?.purchaseDate || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="所属机主">{{ selectedDevice?.ownerName || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="机主电话">{{ selectedDevice?.ownerPhone || '未知' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 维护记录信息 -->
        <div class="maintain-info" v-if="selectedDevice && selectedDevice.maintainRecords && selectedDevice.maintainRecords.length > 0">
          <h4>最近维护记录</h4>
          <el-table :data="selectedDevice.maintainRecords" style="width: 100%;">
            <el-table-column prop="maintainDate" label="维护日期"></el-table-column>
            <el-table-column prop="maintainContent" label="维护内容"></el-table-column>
            <el-table-column prop="maintainResult" label="维护结果"></el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
    
    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="imagePreviewVisible"
      title="图片预览"
      width="80%"
      :before-close="handleImageClose"
    >
      <img :src="getImageUrl(previewImageUrl)" alt="预览图片" class="preview-image" />
    </el-dialog>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      devicesList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      searchKeyword: '',
      dialogVisible: false,
      selectedDevice: null,
      imagePreviewVisible: false,
      previewImageUrl: ''
    }
  },
  mounted() {
    this.getDevicesList()
  },
  methods: {
    async getDevicesList() {
      try {
        const response = await axios.get('/admin/devices/page', {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            status: undefined // 可选参数，不传则查询所有状态
          }
        })
        if (response.code === 200) {
          this.devicesList = response.data.records || []
          this.total = response.data.total || 0
        } else {
          this.$message.error('获取设备列表失败')
          // 提供mock数据以便展示
          this.devicesList = [
            {
              deviceId: 'DEV001',
              deviceName: '扫地机器人',
              manufacturer: '科沃斯',
              model: 'T9',
              ownerName: '张三',
              ownerPhone: '13800138001',
              deviceType: '智能家居',
              purchaseTime: '2023-01-15',
              picture: 'device1.jpg',
              maintainRecords: [
                {
                  maintainDate: '2023-06-10',
                  maintainContent: '清洁内部组件',
                  maintainResult: '正常'
                }
              ]
            },
            {
              deviceId: 'DEV002',
              deviceName: '空气净化器',
              manufacturer: '小米',
              model: 'Pro H',
              ownerName: '李四',
              ownerPhone: '13900139002',
              deviceType: '智能家居',
              purchaseTime: '2023-03-20',
              picture: 'device2.jpg'
            }
          ]
          this.total = this.devicesList.length
        }
      } catch (error) {
        console.error('获取设备列表失败', error)
        this.$message.error('获取设备列表失败')
        // 提供mock数据以便展示
        this.devicesList = [
          {
            deviceId: 'DEV001',
            deviceName: '扫地机器人',
            manufacturer: '科沃斯',
            model: 'T9',
            ownerName: '张三',
            ownerPhone: '13800138001',
            deviceType: '智能家居',
            purchaseTime: '2023-01-15',
            picture: 'device1.jpg',
            maintainRecords: [
              {
                maintainDate: '2023-06-10',
                maintainContent: '清洁内部组件',
                maintainResult: '正常'
              }
            ]
          },
          {
            deviceId: 'DEV002',
            deviceName: '空气净化器',
            manufacturer: '小米',
            model: 'Pro H',
            ownerName: '李四',
            ownerPhone: '13900139002',
            deviceType: '智能家居',
            purchaseTime: '2023-03-20',
            picture: 'device2.jpg'
          }
        ]
        this.total = this.devicesList.length
      }
    },
    searchDevices() {
      this.currentPage = 1
      this.getDevicesList()
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.getDevicesList()
    },
    handleCurrentChange(current) {
      this.currentPage = current
      this.getDevicesList()
    },
    viewDeviceDetail(device) {
      // 构建符合详情对话框期望的设备对象结构
      this.selectedDevice = {
        deviceId: device.deviceInfo?.deviceId,
        deviceName: device.deviceInfo?.deviceName,
        manufacturer: device.deviceInfo?.manufacturer,
        brand: device.deviceInfo?.manufacturer,
        model: device.deviceInfo?.model,
        serialNumber: device.deviceInfo?.serialNumber,
        deviceType: device.deviceInfo?.deviceType,
        statusDesc: device.deviceInfo?.statusDesc,
        maxLoad: device.deviceInfo?.maxLoad,
        endurance: device.deviceInfo?.endurance,
        purchaseTime: device.deviceInfo?.purchaseTime,
        picture: device.deviceInfo?.picture,
        ownerName: device.ownerInfo?.realName,
        ownerPhone: device.ownerInfo?.phone,
        maintainRecords: device.maintainRecords || []
      }
      this.dialogVisible = true
    },
    handleClose() {
      this.dialogVisible = false
      this.selectedDevice = null
    },
    previewImage(imageUrl) {
      this.previewImageUrl = imageUrl
      this.imagePreviewVisible = true
    },
    handleImageClose() {
      this.imagePreviewVisible = false
      this.previewImageUrl = ''
    },
    getImageUrl(imagePath) {
      if (!imagePath) return ''
      // 如果图片路径已经是完整的URL，则直接返回
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      // 否则，添加/api前缀以通过API代理访问静态资源
      return imagePath.startsWith('/') ? `/api${imagePath}` : `/api/${imagePath}`
    }
  }
}
</script>

<style scoped>
.devices-container {
  padding: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.device-detail {
  padding: 10px;
}

.device-image-container {
  margin-bottom: 20px;
  text-align: center;
}

.device-image {
  max-width: 100%;
  max-height: 300px;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  transition: transform 0.3s;
}

.device-image:hover {
  transform: scale(1.02);
}

.no-image {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  color: #999;
}

.device-info {
  margin-bottom: 20px;
}

.maintain-info {
  margin-top: 20px;
}

.preview-image {
  width: 100%;
  height: auto;
  max-height: 70vh;
  object-fit: contain;
}
</style>