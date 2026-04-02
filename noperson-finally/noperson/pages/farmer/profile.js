const { api } = require('../../utils/request');

Page({
  data: {
    userInfo: {},
    currentRole: 'farmer',
    menuItems: [
      { id: 'my_info', name: '我的信息', icon: '/assets/images/user-info.png' },
      { id: 'my_demands', name: '我的需求', icon: '/assets/images/user-info.png' },
      { id: 'my_diagnosis', name: '我的诊断', icon: '/assets/images/user-info.png' },
      { id: 'favorite_address', name: '常用地址', icon: '/assets/images/user-info.png' },
      { id: 'my_wallet', name: '我的钱包', icon: '/assets/images/wallet.png' },
      { id: 'my_reports', name: '我的报告', icon: '/assets/images/reports.png' },
      { id: 'role_switch', name: '角色切换', icon: '/assets/images/user-info.png' },
      { id: 'settings', name: '设置', icon: '/assets/images/user-info.png' }
    ]
  },
  
  onLoad: function() {
    this.loadUserInfo();
  },
  
  onShow: function() {
    // 每次页面显示时刷新用户信息
    this.loadUserInfo();
  },
  
  // 加载用户信息和余额
  loadUserInfo: function() {
    wx.showLoading({
      title: '加载中...',
    });
    
    // 同时获取用户信息和余额
    Promise.all([
      api.getUserInfo(),
      api.getUserBalance()
    ]).then(results => {
      // 从响应对象的data字段获取实际数据
      const userResponse = results[0];
      const balanceResponse = results[1];
      
      const userInfo = userResponse.data || {};
      const balanceInfo = balanceResponse.data || {};
      
      console.log('获取用户信息成功:', userInfo);
      console.log('获取用户余额成功:', balanceInfo);
      
      // 将余额信息合并到用户信息中
      // 安全处理余额数据
      let balance = 0.00;
      // 检查balanceResponse响应格式
      if (typeof balanceResponse === 'object' && balanceResponse !== null) {
        // 从data字段获取余额值
        if (balanceResponse.data !== undefined) {
          if (typeof balanceResponse.data === 'object' && balanceResponse.data.balance !== undefined) {
            balance = parseFloat(balanceResponse.data.balance) || 0.00;
          } else if (typeof balanceResponse.data === 'number') {
            balance = balanceResponse.data;
          } else {
            balance = parseFloat(balanceResponse.data) || 0.00;
          }
        } else if (balanceResponse.balance !== undefined) {
          balance = parseFloat(balanceResponse.balance) || 0.00;
        }
      } else if (typeof balanceResponse === 'number') {
        // 如果直接返回了数字
        balance = balanceResponse;
      }
      
      // 将余额格式化为两位小数
      userInfo.balance = parseFloat(balance).toFixed(2);
      
      this.setData({
        userInfo: userInfo
      });
      
      // 更新全局用户信息
      const app = getApp();
      app.globalData.userInfo = userInfo;
    }).catch(error => {
      console.error('获取用户信息或余额失败:', error);
      // 即使获取失败，也尝试只获取用户信息
      this.getOnlyUserInfo().finally(() => {
        wx.hideLoading();
      });
    }).finally(() => {
      wx.hideLoading();
    });
  },
  
  // 仅获取用户信息（备用方案）
  getOnlyUserInfo: function() {
    return api.getUserInfo().then(res => {
      // 从响应对象的data字段获取实际数据
      const userInfo = res.data || {};
      console.log('备用方案：获取用户信息成功:', userInfo);
      this.setData({
        userInfo: userInfo
      });
      
      // 更新全局用户信息
      const app = getApp();
      app.globalData.userInfo = userInfo;
    }).catch(error => {
      console.error('备用方案：获取用户信息失败:', error);
      wx.showToast({
        title: '获取用户信息失败',
        icon: 'none'
      });
    });
  },
  
  // 跳转到对应页面
  navigateToPage: function(e) {
    const id = e.currentTarget.dataset.id;
    
    switch(id) {
      case 'my_info':
        this.viewUserInfo();
        break;
      case 'my_demands':
        wx.navigateTo({ url: '/pages/farmer/my-demand-list' });
        break;
      case 'my_diagnosis':
        wx.navigateTo({ url: '/pages/farmer/diagnosis-history' });
        break;
      case 'favorite_address':
        wx.navigateTo({ url: '/pages/farmer/favorite-address' });
        break;
      case 'my_wallet':
        wx.navigateTo({ url: '/pages/payment/wallet' });
        break;
      case 'my_reports':
        wx.navigateTo({ url: '/pages/farmer/view-report' });
        break;
      case 'role_switch':
        wx.navigateTo({ url: '/pages/role-select/index' });
        break;
      case 'settings':
        this.showSettingsMenu();
        break;
      default:
        break;
    }
  },
  
  // 显示设置菜单
  showSettingsMenu: function() {
    wx.showActionSheet({
      itemList: ['设置常用地址', '账号安全', '关于我们', '帮助与反馈'],
      success: (res) => {
        switch(res.tapIndex) {
          case 0:
            this.setCommonAddress();
            break;
          case 1:
            this.accountSecurity();
            break;
          case 2:
            this.aboutUs();
            break;
          case 3:
            this.helpFeedback();
            break;
        }
      }
    });
  },
  
  // 设置常用地址
  setCommonAddress: function() {
    wx.navigateTo({
      url: '/pages/farmer/favorite-address'
    });
  },
  
  // 账号安全
  accountSecurity: function() {
    wx.showToast({
      title: '账号安全功能待开发',
      icon: 'none'
    });
  },
  
  // 关于我们
  aboutUs: function() {
    wx.showToast({
      title: '关于我们功能待开发',
      icon: 'none'
    });
  },
  
  // 帮助与反馈
  helpFeedback: function() {
    wx.showToast({
      title: '帮助与反馈功能待开发',
      icon: 'none'
    });
  },
  
  // 查看用户信息
  viewUserInfo: function() {
    wx.navigateTo({
      url: '/pages/farmer/user-info'
    });
  },

  // 导航到首页
  navigateToHome: function() {
    wx.redirectTo({
      url: '/pages/farmer/index'
    });
  },

  // 导航到我的需求
  navigateToMyDemands: function() {
    wx.redirectTo({
      url: '/pages/farmer/my-demand-list'
    });
  },

  // 导航到消息页面
  navigateToChat: function() {
    wx.redirectTo({
      url: '/pages/farmer/chat/list'
    });
  },
  
  // 退出登录
  logout: function() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          const app = getApp();
          app.globalData.userInfo = null;
          wx.removeStorageSync('token');
          wx.redirectTo({
            url: '/pages/auth/login'
          });
        }
      }
    });
  }
});
