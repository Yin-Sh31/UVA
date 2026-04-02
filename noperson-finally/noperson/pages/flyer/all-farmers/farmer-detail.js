// flyer/all-farmers/farmer-detail.js

const { request } = require('../../../utils/request');

Page({
  data: {
    farmerId: '',
    farmerInfo: null,
    demandList: [], // 农户需求列表
    loading: true,
    error: false,
    loadingDemands: false // 加载需求的状态
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ farmerId: options.id });
      this.loadFarmerDetail();
    } else {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      wx.navigateBack();
    }
  },

  // 加载农户详情
  loadFarmerDetail() {
    const { farmerId } = this.data;
    
    this.setData({ loading: true, error: false });
    
    request('/farmer/flyer/info', 'GET', {
      farmerId: farmerId
    })
      .then(res => {
        if (res.code === 200 && res.data) {
          // 根据API响应，农户信息在res.data.farmerInfo中
          const farmerInfo = res.data.farmerInfo || res.data;
          this.setData({
            farmerInfo: farmerInfo,
            loading: false
          });
          // 如果API响应中已经包含需求列表，直接使用
          if (res.data.demands && Array.isArray(res.data.demands)) {
            this.setData({
              demandList: res.data.demands,
              loadingDemands: false
            });
          } else {
            // 否则再单独请求需求列表
            this.loadFarmerDemands();
          }
        } else {
          wx.showToast({
            title: res.msg || '获取农户信息失败',
            icon: 'none'
          });
          this.setData({ loading: false, error: true });
        }
      })
      .catch(err => {
        console.error('获取农户详情失败:', err);
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
        this.setData({ loading: false, error: true });
      });
  },
  
  // 加载农户需求列表
  loadFarmerDemands() {
    const { farmerId } = this.data;
    
    // 如果已经在加载中，则不再请求
    if (this.data.loadingDemands) {
      return;
    }
    
    this.setData({ loadingDemands: true });
    
    // 使用获取农户需求的API，不使用分页参数以获取所有需求
    request('/demand/spray/flyer/all-demands', 'GET', {
      farmerId: farmerId // 仅添加农户ID筛选条件
    })
      .then(res => {
        let records = [];
        
        // 处理不同格式的响应数据
        if (res && res.code === 200 && res.data) {
          // 优先处理API返回的demands字段
          if (res.data.demands && Array.isArray(res.data.demands)) {
            records = res.data.demands;
          } else if (res.data.records && Array.isArray(res.data.records)) {
            records = res.data.records;
          } else if (Array.isArray(res.data)) {
            records = res.data;
          }
        } else if (res && res.records && Array.isArray(res.records)) {
          records = res.records;
        } else if (res && Array.isArray(res)) {
          records = res;
        }
        
        // 更新需求列表
        this.setData({
          demandList: records,
          loadingDemands: false
        });
      })
      .catch(err => {
        console.error('获取农户需求列表失败:', err);
        this.setData({ loadingDemands: false });
      });
  },
  


  // 刷新农户信息
  refreshFarmerInfo() {
    this.setData({
      demandList: []
    });
    this.loadFarmerDetail();
  },

  // 拨打电话
  makePhoneCall(e) {
    const phoneNumber = e.currentTarget.dataset.phone;
    if (phoneNumber) {
      wx.makePhoneCall({
        phoneNumber: phoneNumber,
        fail: () => {
          console.log('拨打电话失败');
        }
      });
    } else {
      wx.showToast({
        title: '手机号未设置',
        icon: 'none'
      });
    }
  },

  // 复制手机号
  copyPhoneNumber(e) {
    const phoneNumber = e.currentTarget.dataset.phone;
    if (phoneNumber) {
      wx.setClipboardData({
        data: phoneNumber,
        success: () => {
          wx.showToast({
            title: '复制成功',
            icon: 'success'
          });
        },
        fail: () => {
          wx.showToast({
            title: '复制失败',
            icon: 'none'
          });
        }
      });
    } else {
      wx.showToast({
        title: '手机号未设置',
        icon: 'none'
      });
    }
  },
  
  // 与农户发起聊天
  startChatWithFarmer() {
    const { farmerInfo } = this.data;
    if (!farmerInfo) return;
    
    // 检查必要字段
    if (!farmerInfo.userId) {
      console.error('农户ID为空');
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      return;
    }
    
    // 确保userId是字符串格式
    const userId = String(farmerInfo.userId);
    const userName = encodeURIComponent(farmerInfo.username || '农户');
    
    console.log('用户信息:', { userId, userName });
    
    // 跳转到现有的聊天详情页面
    const url = `/pages/flyer/chat/detail?otherUserId=${userId}&otherUserName=${userName}&otherUserType=1`;
    console.log('跳转URL:', url);
    
    wx.navigateTo({
      url: url,
      success: () => {
        console.log('跳转到聊天页面成功');
      },
      fail: (err) => {
        console.error('跳转到聊天页面失败:', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  },
  
  // 查看需求详情
  viewDemandDetail(e) {
    const demandId = e.currentTarget.dataset.demandId;
    if (!demandId) return;
    
    // 跳转到订单详情页面
    wx.navigateTo({
      url: `/pages/flyer/order-detail/index?orderId=${demandId}`,
      success: () => {
        console.log('跳转到需求详情页面成功');
      },
      fail: (err) => {
        console.error('跳转到需求详情页面失败:', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  }
});