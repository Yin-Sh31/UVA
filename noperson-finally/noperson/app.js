// app.js
App({
  onLaunch: function() {
    // 初始化云开发环境（如果需要）
    // wx.cloud.init({
    //   env: 'your-env-id',
    //   traceUser: true,
    // })
    
    // 检查用户是否登录
    this.checkLoginStatus();
    
    // 获取系统信息
    this.getSystemInfo();
  },
  
  // 检查登录状态
  checkLoginStatus: function() {
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    const userRole = wx.getStorageSync('userRole');
    
    if (token && userInfo && userRole) {
      // 确保token没有多余的空格或换行符，并处理可能为undefined的情况
      const cleanToken = token ? token.trim() : '';
      
      this.globalData.isLoggedIn = true;
      this.globalData.userInfo = userInfo;
      this.globalData.userRole = userRole;
      this.globalData.token = cleanToken;
      
      // 更新本地存储中的token为清理后的版本
      wx.setStorageSync('token', cleanToken);
    }
  },
  
  // 获取系统信息
  getSystemInfo: function() {
    try {
      const systemInfo = wx.getSystemInfoSync();
      this.globalData.systemInfo = systemInfo;
      this.globalData.windowHeight = systemInfo.windowHeight;
      this.globalData.windowWidth = systemInfo.windowWidth;
    } catch (e) {
      console.error('获取系统信息失败', e);
    }
  },
  
  // 登录成功后保存用户信息
  setUserInfo: function(token, userInfo, role) {
    // 确保token没有多余的空格或换行符，并处理可能为undefined的情况
    const cleanToken = token ? token.trim() : '';
    
    this.globalData.isLoggedIn = true;
    this.globalData.userInfo = userInfo;
    this.globalData.userRole = role;
    this.globalData.token = cleanToken;
    
    // 存储到本地
    wx.setStorageSync('token', cleanToken);
    wx.setStorageSync('userInfo', userInfo);
    wx.setStorageSync('userRole', role);
  },
  
  // 退出登录
  logout: function() {
    this.globalData.isLoggedIn = false;
    this.globalData.userInfo = null;
    this.globalData.userRole = null;
    this.globalData.token = '';
    
    // 清除本地存储
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('userRole');
    
    // 跳转到登录页
    wx.redirectTo({
      url: '/pages/auth/login',
    });
  },
  
  // 不同角色的tabBar配置
  tabBarConfigs: {
    farmer: {
      color: '#999999',
      selectedColor: '#1AAD19',
      backgroundColor: '#ffffff',
      list: [
        {
          pagePath: 'pages/farmer/index',
          text: '首页',
          iconPath: 'images/home-icon.png',
          selectedIconPath: 'images/home-icon.png'
        },
        {
          pagePath: 'pages/farmer/my-demand-list',
          text: '我的需求',
          iconPath: 'images/demand-icon.png',
          selectedIconPath: 'images/demand-icon.png'
        },
        {
          pagePath: 'pages/farmer/message-center',
          text: '消息',
          iconPath: 'images/publish-icon.png',
          selectedIconPath: 'images/publish-icon.png'
        },
        {
          pagePath: 'pages/farmer/profile',
          text: '我的',
          iconPath: 'images/profile-icon.png',
          selectedIconPath: 'images/profile-icon.png'
        }
      ]
    },
    flyer: {
      color: '#999999',
      selectedColor: '#1AAD19',
      backgroundColor: '#ffffff',
      list: [
        {
          pagePath: 'pages/flyer/index',
          text: '飞手首页',
          iconPath: 'images/home-icon.png',
          selectedIconPath: 'images/home-icon.png'
        },
        {
          pagePath: 'pages/flyer/accept-order/index',
          text: '需求大厅',
          iconPath: 'images/demand-icon.png',
          selectedIconPath: 'images/demand-icon.png'
        },
        {
          pagePath: 'pages/flyer/message-center/index',
          text: '消息',
          iconPath: 'images/publish-icon.png',
          selectedIconPath: 'images/publish-icon.png'
        },
        {
          pagePath: 'pages/flyer/profile/index',
          text: '我的',
          iconPath: 'images/profile-icon.png',
          selectedIconPath: 'images/profile-icon.png'
        }
      ]
    }
  },

  // 全局数据
  globalData: {
    isLoggedIn: false,
    userInfo: null,
    userRole: null, // farmer, flyer, owner, admin
    token: '',
    systemInfo: {},
    windowHeight: 0,
    windowWidth: 0,
    // 其他全局配置
    baseUrl: 'https://localhost:8082/api', // 后端API地址
    appName: '智慧农业服务平台',
    // 临时存储用于页面间传递的数据
    tempDemandId: null
  },

  // 根据角色动态设置tabBar
  setTabBarByRole: function(role) {
    // 微信小程序不支持直接修改全局tabBar配置
    // 我们在app.json中保持当前配置，并提供导航辅助方法
    console.log('根据角色设置导航:', role);
    this.globalData.userRole = role;
  },
  
  // 获取当前角色的首页路径
  getHomePathByRole: function(role) {
    switch(role) {
      case 'farmer':
        return '/pages/farmer/index';
      case 'flyer':
        return '/pages/flyer/index';

      case 'admin':
        return '/pages/admin/index';
      default:
        return '/pages/index/index';
    }
  },
  
  // 获取导航跳转方式
  getNavigateMethodByRole: function(role) {
    // 根据角色和app.json中的tabBar配置决定使用哪种跳转方式
    // 当前app.json中配置的是飞手tabBar
    if (role === 'flyer') {
      return 'switchTab';
    } else {
      // 对于农户和其他角色，使用reLaunch跳转到首页
      return 'reLaunch';
    }
  }
})
