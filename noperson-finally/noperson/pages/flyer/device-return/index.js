// flyer/device-return/index.js
const { api } = require('../../../utils/request');

Page({
  data: {
    rentedDevices: [],
    loading: false,
    error: '',
    refreshing: false,
    currentReturnDevice: '', // 当前正在归还的设备ID
    returnLocation: '',
    deviceCondition: 'good',
    returnRemark: ''
  },

  onLoad() {
    // 加载已租借设备列表
    this.loadRentedDevices();
  },

  // 加载已租借设备列表
  loadRentedDevices() {
    this.setData({
      loading: !this.data.refreshing,
      error: ''
    });
    
    // 这里暂时使用模拟数据，实际应该调用获取飞手已租借设备列表的接口
    setTimeout(() => {
      // 模拟已租借设备数据
      const mockRentedDevices = [
        {
          id: 'D2023004',
          name: '大疆农业无人机',
          type: 'T30',
          rentTime: '2024-01-20 10:00',
          ownerName: '张三',
          ownerId: '1001',
          rentPrice: 480,
          deviceStatus: 'normal',
          currentLocation: '北京市朝阳区'
        },
        {
          id: 'D2023005',
          name: '极飞植保无人机',
          type: 'P30',
          rentTime: '2024-01-19 15:30',
          ownerName: '李四',
          ownerId: '1002',
          rentPrice: 420,
          deviceStatus: 'normal',
          currentLocation: '北京市海淀区'
        }
      ];
      
      this.setData({
        rentedDevices: mockRentedDevices,
        loading: false,
        refreshing: false
      });
    }, 1000);
    
    // 实际应该调用类似这样的接口
    /*
    api.getRentedDevicesByFlyer()
      .then(res => {
        const deviceData = res.data || [];
        this.setData({
          rentedDevices: deviceData,
          loading: false,
          refreshing: false
        });
      })
      .catch(error => {
        console.error('获取已租借设备列表失败:', error);
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
    this.loadRentedDevices();
  },
  
  // 获取设备状态文本
  getStatusText(status) {
    const statusMap = {
      'normal': '正常',
      'warning': '警告',
      'fault': '故障'
    };
    return statusMap[status] || '未知状态';
  },
  
  // 计算预计费用（简单计算：每天价格 * 天数）
  calculateEstimatedFee(device) {
    if (!device.rentTime || !device.rentPrice) return 0;
    
    const rentDate = new Date(device.rentTime);
    const now = new Date();
    const days = Math.ceil((now - rentDate) / (1000 * 60 * 60 * 24));
    return Math.max(days * device.rentPrice, device.rentPrice); // 至少一天费用
  },
  
  // 显示归还表单
  showReturnForm(e) {
    const deviceId = e.currentTarget.dataset.id;
    this.setData({
      currentReturnDevice: deviceId,
      returnLocation: '',
      deviceCondition: 'good',
      returnRemark: ''
    });
  },
  
  // 取消归还
  cancelReturn() {
    this.setData({
      currentReturnDevice: '',
      returnLocation: '',
      deviceCondition: 'good',
      returnRemark: ''
    });
  },
  
  // 输入归还地点
  onLocationInput(e) {
    this.setData({
      returnLocation: e.detail.value
    });
  },
  
  // 选择设备状况
  onConditionChange(e) {
    this.setData({
      deviceCondition: e.detail.value
    });
  },
  
  // 输入备注
  onRemarkInput(e) {
    this.setData({
      returnRemark: e.detail.value
    });
  },
  
  // 确认归还
  confirmReturn(e) {
    const deviceId = e.currentTarget.dataset.id;
    const { returnLocation, deviceCondition, returnRemark } = this.data;
    
    // 验证表单
    if (!returnLocation) {
      wx.showToast({
        title: '请输入归还地点',
        icon: 'none'
      });
      return;
    }
    
    wx.showModal({
      title: '确认归还',
      content: '确定要归还此设备吗？',
      success: (res) => {
        if (res.confirm) {
          this.submitReturnDevice(deviceId, {
            location: returnLocation,
            condition: deviceCondition,
            remark: returnRemark
          });
        }
      }
    });
  },
  
  // 提交归还请求
  submitReturnDevice(deviceId, returnInfo) {
    wx.showLoading({
      title: '处理中...'
    });
    
    api.returnDeviceByFlyer(deviceId)
      .then(res => {
        wx.hideLoading();
        
        // 注意：需要从data字段获取实际数据
        const responseData = res.data || {};
        
        wx.showToast({
          title: '归还成功',
          icon: 'success',
          duration: 2000,
          success: () => {
            // 从列表中移除已归还的设备
            const { rentedDevices } = this.data;
            const updatedList = rentedDevices.filter(device => device.id !== deviceId);
            this.setData({
              rentedDevices: updatedList,
              currentReturnDevice: ''
            });
          }
        });
      })
      .catch(error => {
        wx.hideLoading();
        console.error('归还设备失败:', error);
        wx.showToast({
          title: error.message || '归还失败',
          icon: 'none'
        });
      });
  }
});