// flyer/device-rent/index.js
const { api } = require('../../../utils/request');

Page({
  data: {
    ownerId: '',
    deviceList: [],
    loading: false,
    error: '',
    refreshing: false
  },

  onLoad(options) {
    // 从页面参数中获取ownerId
    if (options.ownerId) {
      this.setData({
        ownerId: options.ownerId
      });
      // 加载设备列表
      this.loadDeviceList();
    } else {
      this.setData({
        error: '机主ID参数错误',
        loading: false
      });
    }
  },

  // 加载设备列表
  loadDeviceList() {
    const { ownerId } = this.data;
    
    if (!ownerId) {
      this.setData({
        error: '机主ID为空',
        loading: false,
        refreshing: false
      });
      return;
    }
    
    this.setData({
      loading: !this.data.refreshing, // 如果是下拉刷新，不设置loading为true
      error: ''
    });
    
    // 这里暂时使用模拟数据，实际应该调用获取机主设备列表的接口
    // 由于没有专门的接口，这里模拟一些可租借设备数据
    setTimeout(() => {
      // 模拟设备数据
      const mockDevices = [
        {
          id: 'D2023001',
          name: '大疆植保无人机',
          type: 'T40',
          status: 'available',
          flightHours: 120,
          lastMaintenanceDate: '2024-01-15',
          rentPrice: 500,
          description: '全新设备，适合大面积作业'
        },
        {
          id: 'D2023002',
          name: '极飞农业无人机',
          type: 'XP 2022',
          status: 'available',
          flightHours: 80,
          lastMaintenanceDate: '2024-01-10',
          rentPrice: 450,
          description: '续航时间长，操作简单'
        },
        {
          id: 'D2023003',
          name: '大疆巡检无人机',
          type: 'M300 RTK',
          status: 'unavailable',
          flightHours: 200,
          lastMaintenanceDate: '2024-01-05',
          rentPrice: 600,
          description: '高精度RTK定位，适合复杂环境'
        }
      ];
      
      this.setData({
        deviceList: mockDevices,
        loading: false,
        refreshing: false
      });
    }, 1000);
    
    // 实际应该调用类似这样的接口
    /*
    api.getOwnerDevices(ownerId)
      .then(res => {
        const deviceData = res.data || [];
        this.setData({
          deviceList: deviceData,
          loading: false,
          refreshing: false
        });
      })
      .catch(error => {
        console.error('获取设备列表失败:', error);
        this.setData({
          error: error.message || '获取设备列表失败',
          loading: false,
          refreshing: false
        });
      });
    */
  },
  
  // 下拉刷新
  onRefresh() {
    this.setData({
      refreshing: true
    });
    this.loadDeviceList();
  },
  
  // 获取设备状态文本
  getStatusText(status) {
    const statusMap = {
      'available': '可租借',
      'unavailable': '已租借',
      'maintain': '维护中',
      'fault': '故障'
    };
    return statusMap[status] || '未知状态';
  },
  
  // 租借设备
  rentDevice(e) {
    const deviceId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认租借',
      content: '确定要租借此设备吗？',
      success: (res) => {
        if (res.confirm) {
          this.submitRentDevice(deviceId);
        }
      }
    });
  },
  
  // 提交租借请求
  submitRentDevice(deviceId) {
    wx.showLoading({
      title: '处理中...'
    });
    
    api.rentDeviceByFlyer(deviceId)
      .then(res => {
        wx.hideLoading();
        
        // 注意：需要从data字段获取实际数据
        const responseData = res.data || {};
        
        wx.showToast({
          title: '租借成功',
          icon: 'success',
          duration: 2000,
          success: () => {
            // 更新设备状态为已租借
            const { deviceList } = this.data;
            const updatedList = deviceList.map(device => {
              if (device.id === deviceId) {
                return { ...device, status: 'unavailable' };
              }
              return device;
            });
            this.setData({
              deviceList: updatedList
            });
          }
        });
      })
      .catch(error => {
        wx.hideLoading();
        console.error('租借设备失败:', error);
        wx.showToast({
          title: error.message || '租借失败',
          icon: 'none'
        });
      });
  }
});