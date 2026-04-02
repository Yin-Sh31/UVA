// 飞手当前租借设备页面
const { api } = require('../../../utils/request.js');

Page({
  data: {
    currentRentals: [],
    loading: false,
    refreshing: false
  },

  onLoad: function() {
    this.loadCurrentRentals();
  },

  onShow: function() {
    // 每次显示页面时刷新数据
    this.loadCurrentRentals();
  },

  // 加载当前租借的设备
  loadCurrentRentals: async function() {
    this.setData({
      loading: true
    });
    
    try {
      // 调用API获取飞手当前租借的设备
      const response = await api.getFlyerCurrentRentals();
      
      // 处理响应数据
      let rawList = [];
      if (Array.isArray(response.data)) {
        rawList = response.data;
      } else if (response.data && Array.isArray(response.data.currentRentals)) {
        rawList = response.data.currentRentals;
      }
      
      // 格式化设备数据
      const currentRentals = rawList.map(device => ({
        id: device.deviceId,
        name: device.deviceName || '无人机',
        type: device.deviceType || '未知型号',
        picture: device.picture || '',
        rentalTime: this.formatTime(device.rentalStartTime),
        rentalStatus: device.rentalStatus || 1,
        ...device
      }));
      
      this.setData({
        currentRentals: currentRentals
      });
    } catch (error) {
      console.error('加载当前租借设备失败', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({
        loading: false,
        refreshing: false
      });
    }
  },

  // 下拉刷新
  onRefresh: function() {
    this.setData({
      refreshing: true
    });
    this.loadCurrentRentals();
  },

  // 释放设备
  releaseDevice: function(e) {
    const deviceId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '释放设备',
      content: '确定要释放该设备吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({
              title: '释放中...',
              mask: true
            });
            
            // 调用API释放设备
            const response = await api.releaseDevice(deviceId);
            
            if (response.code === 200) {
              wx.showToast({
                title: '释放成功',
                icon: 'success'
              });
              // 重新加载数据
              this.loadCurrentRentals();
            } else {
              wx.showToast({
                title: response.message || '释放失败',
                icon: 'none'
              });
            }
          } catch (error) {
            console.error('释放设备失败', error);
            wx.showToast({
              title: '释放失败，请重试',
              icon: 'none'
            });
          } finally {
            wx.hideLoading();
          }
        }
      }
    });
  },

  // 查看设备详情
  viewDeviceDetail: function(e) {
    const deviceId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/flyer/equipment/device-detail?deviceId=${deviceId}`
    });
  },

  // 格式化时间
  formatTime: function(time) {
    if (!time) return '';
    
    const date = new Date(time);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}年${month}月${day}日${hours}:${minutes}:${seconds}`;
  }
});