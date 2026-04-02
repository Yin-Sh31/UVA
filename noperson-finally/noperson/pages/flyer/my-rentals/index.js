// 飞手我的租借记录页面
const { api } = require('../../../utils/request.js');

Page({
  data: {
    rentalList: [],
    loading: false,
    refreshing: false
  },

  onLoad: function() {
    this.loadRentalHistory();
  },

  onShow: function() {
    // 每次显示页面时刷新数据
    this.loadRentalHistory();
  },

  // 加载租借历史记录
  loadRentalHistory: async function() {
    this.setData({
      loading: true
    });
    
    try {
      // 调用API获取飞手的租借历史记录
      const response = await api.getFlyerRentalHistory();
      
      // 处理响应数据
      let rawList = [];
      if (Array.isArray(response.data)) {
        rawList = response.data;
      } else if (response.data && Array.isArray(response.data.rentalList)) {
        rawList = response.data.rentalList;
      }
      
      // 格式化租借记录
      const rentalList = rawList.map(rental => ({
        id: rental.rentalId,
        deviceId: rental.deviceId,
        deviceName: rental.deviceName || '无人机',
        deviceType: rental.deviceType || '未知型号',
        rentalStartTime: this.formatTime(rental.rentalStartTime),
        rentalEndTime: rental.rentalEndTime ? this.formatTime(rental.rentalEndTime) : '未归还',
        rentalStatus: this.getRentalStatusText(rental.rentalStatus),
        rentalStatusColor: this.getRentalStatusColor(rental.rentalStatus),
        ...rental
      }));
      
      this.setData({
        rentalList: rentalList
      });
    } catch (error) {
      console.error('加载租借历史记录失败', error);
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
    this.loadRentalHistory();
  },

  // 查看租借详情
  viewRentalDetail: function(e) {
    const rentalId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/flyer/my-rentals/detail?rentalId=${rentalId}`
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
  },

  // 获取租借状态文本
  getRentalStatusText: function(status) {
    const statusMap = {
      1: '租借中',
      2: '已归还',
      3: '已取消'
    };
    return statusMap[status] || '未知状态';
  },

  // 获取租借状态颜色
  getRentalStatusColor: function(status) {
    const colorMap = {
      1: '#007AFF',
      2: '#34C759',
      3: '#FF3B30'
    };
    return colorMap[status] || '#8E8E93';
  }
});