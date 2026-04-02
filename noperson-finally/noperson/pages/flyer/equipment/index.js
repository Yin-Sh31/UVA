// flyer/equipment/index.js
const { api } = require('../../../utils/request');

Page({
  data: {
    equipmentList: [],
    loading: false,
    refreshing: false
  },
  
  onLoad: function() {
    this.loadAvailableDevices();
  },
  
  onShow: function() {
    // 每次页面显示时刷新数据
    this.loadAvailableDevices();
  },
  
  // 加载可用设备列表
  loadAvailableDevices: async function() {
    this.setData({
      loading: true
    });
    
    try {
      // 调用API获取所有可用设备
      const response = await api.getAvailableDevices();
      // 从response.data获取设备列表数组（可能是直接数组或嵌套在某个字段中）
      let rawList = [];
      
      // 处理不同的数据结构情况
      if (Array.isArray(response.data)) {
        // 如果data直接是数组
        rawList = response.data;
      } else if (response.data && Array.isArray(response.data.deviceList)) {
        // 如果data包含deviceList数组
        rawList = response.data.deviceList;
      } else {
        // 默认空数组
        rawList = [];
      }
      
      // 字段映射，将API返回的字段名映射到WXML中使用的字段名
      const equipmentList = rawList.map(device => ({
        id: device.deviceId,
        name: device.deviceName || '无人机',
        type: device.deviceType || '未知型号',
        // 设备状态处理
        available: device.status === 1,
        // 价格处理
        rentPrice: device.rentPrice || 0,
        // 设备参数
        manufacturer: device.manufacturer || '--',
        maxLoad: device.maxLoad || 0,
        maxFlightTime: device.endurance || 0,
        flightHours: device.flightHours || 0,
        // 机主信息
        ownerAvatar: '/assets/images/user-avatar.png',
        ownerName: '未知机主',
        picture: device.picture || '',
        // 添加状态描述
        statusDesc: device.statusDesc || (device.status === 1 ? '正常' : '已借出'),
        // 保留原始数据
        ...device
      }));
      
      this.setData({
        equipmentList: equipmentList
      });
    } catch (error) {
      console.error('加载设备列表失败', error);
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
    this.loadAvailableDevices();
  },
  
  // 查看设备详情
  viewDeviceDetail: function(e) {
    const deviceId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/flyer/equipment/device-detail?deviceId=${deviceId}`
    });
  }
});