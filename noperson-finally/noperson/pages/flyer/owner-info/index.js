// flyer/owner-info/index.js
const { api } = require('../../../utils/request');

Page({
  data: {
    ownerId: '',
    ownerInfo: null,
    loading: false,
    error: ''
  },

  onLoad(options) {
    // 从页面参数中获取ownerId
    if (options.ownerId) {
      this.setData({
        ownerId: options.ownerId
      });
      // 加载机主信息
      this.loadOwnerInfo();
    } else {
      this.setData({
        error: '机主ID参数错误',
        loading: false
      });
    }
  },

  // 加载机主信息
  loadOwnerInfo() {
    const { ownerId } = this.data;
    
    if (!ownerId) {
      this.setData({
        error: '机主ID为空',
        loading: false
      });
      return;
    }
    
    this.setData({
      loading: true,
      error: ''
    });
    
    api.getOwnerInfoByFlyer(ownerId)
      .then(res => {
        // 注意：需要从data字段获取实际数据
        const ownerData = res.data || {};
        
        // 对身份证号进行脱敏处理
        if (ownerData.idCardNumber) {
          ownerData.idCardNumber = this.maskIdCard(ownerData.idCardNumber);
        }
        
        this.setData({
          ownerInfo: ownerData,
          loading: false
        });
      })
      .catch(error => {
        console.error('获取机主信息失败:', error);
        this.setData({
          error: error.message || '获取机主信息失败',
          loading: false
        });
      });
  },
  
  // 身份证号脱敏处理
  maskIdCard(idCard) {
    if (!idCard || idCard.length < 10) return idCard;
    const start = idCard.substring(0, 6);
    const end = idCard.substring(idCard.length - 4);
    const middle = '*'.repeat(idCard.length - 10);
    return start + middle + end;
  },
  
  // 拨打电话
  makePhoneCall() {
    const { ownerInfo } = this.data;
    if (ownerInfo && ownerInfo.phoneNumber) {
      wx.makePhoneCall({
        phoneNumber: ownerInfo.phoneNumber,
        fail: (err) => {
          console.error('拨打电话失败:', err);
          wx.showToast({
            title: '拨打电话失败',
            icon: 'none'
          });
        }
      });
    }
  },
  
  // 跳转到设备租借页面
  navigateToRentDevice() {
    const { ownerId } = this.data;
    wx.navigateTo({
      url: `/pages/flyer/device-rent/index?ownerId=${ownerId}`,
      fail: (err) => {
        console.error('页面跳转失败:', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  }
});