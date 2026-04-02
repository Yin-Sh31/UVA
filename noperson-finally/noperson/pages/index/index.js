// index.js
const roleConfig = require('../../utils/role-config');
const globalStyles = require('../../utils/global-styles');
const sprayDemandAPI = require('../../utils/api/spray-demand-api');

Page({
  data: {
    userRole: '',
    pageTitle: '',
    roleConfig: {},
    farmerOrders: [], // 农户的需求列表
    flyerRequests: [], // 飞手的需求列表

    selectedTab: '全部需求', // 飞手筛选标签
    pageNum: 1,
    pageSize: 10,
    loading: false,
    hasMore: true,
    statusStr: '' // 默认查询待接取状态
  },
  
  onLoad: async function() {
    // 获取全局用户信息
    const app = getApp();
    const userRole = app.globalData.userRole || 'farmer'; // 默认农户角色
    const pageTitle = roleConfig.getTitle(userRole);
    
    this.setData({
      userRole: userRole,
      pageTitle: pageTitle,
      roleConfig: globalStyles
    });
    
    // 根据角色初始化页面数据
    await this.initPageData();
  },
  
  onShow: async function() {
    // 页面显示时重新加载数据，确保数据最新
    if (this.data.userRole === 'flyer') {
      this.setData({
        pageNum: 1,
        hasMore: true,
        flyerRequests: []
      });
      // 同时重新加载农户数据和需求数据
      await this.initFarmerData();
      await this.initFlyerData();
    }
  },
  
  // 根据角色初始化页面数据
  initPageData: async function() {
    const userRole = this.data.userRole;
    
    if (userRole === 'farmer') {
      await this.initFarmerData();
    } else if (userRole === 'flyer') {
      // 飞手需要同时获取农户数据和需求数据
      await this.initFarmerData();
      await this.initFlyerData();
    }
  },
  
  // 初始化农户页面数据（飞手端使用）
  initFarmerData: async function() {
    try {
      console.log('开始调用农户列表API...');
      // 调用API获取真实的农户数据
      const response = await sprayDemandAPI.getFarmersList({
        pageNum: 1,
        pageSize: 20
      });
      
      console.log('农户列表API返回数据:', response);
      
      // 处理不同的返回格式
      let farmersList = [];
      if (response && response.records && Array.isArray(response.records)) {
        // 如果返回的是分页对象，取records数组
        farmersList = response.records;
      } else if (response && Array.isArray(response)) {
        // 如果直接返回数组
        farmersList = response;
      }
      
      console.log('处理后的农户列表:', farmersList);
      
      if (farmersList && farmersList.length > 0) {
        // 随机选择4个农户数据
        const shuffledFarmers = [...farmersList].sort(() => Math.random() - 0.5);
        const selectedFarmers = shuffledFarmers.slice(0, 4);
        
        console.log('随机选择的4个农户:', selectedFarmers);
        
        // 转换数据格式以匹配WXML的需求，不包含隐私信息
        const farmersInfo = selectedFarmers.map((farmer, index) => {
          // 生成随机的需求数量（1-10之间）
          const demandCount = Math.floor(Math.random() * 10) + 1;
          // 生成随机的位置信息
          const locations = ['东村农场', '西村果园', '南村菜地', '北村农田', '中心农场', '河畔果园'];
          const location = locations[Math.floor(Math.random() * locations.length)];
          
          return {
            id: farmer.userId || index + 1,
            farmerName: farmer.realName || farmer.username || '农户用户',
            avatar: farmer.avatar,
            demandCount: demandCount,
            location: location,
            joinTime: farmer.createTime ? this.formatDate(farmer.createTime) : '未知时间'
          };
        });
        
        console.log('最终显示的农户信息:', farmersInfo);
        
        this.setData({
          farmerOrders: farmersInfo
        });
      } else {
        console.error('获取农户列表失败：返回数据为空');
        wx.showToast({
          title: '暂无农户数据',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('获取农户列表失败:', error);
      wx.showToast({
        title: '获取农户数据失败',
        icon: 'none'
      });
    }
  },
  
  // 格式化日期
  formatDate: function(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' });
  },
  
  // 初始化飞手页面数据
  initFlyerData: async function() {
    if (this.data.loading || !this.data.hasMore) {
      return;
    }
    
    this.setData({ loading: true });
    
    try {
      const { pageNum, pageSize, statusStr } = this.data;
      
      // 调用接口获取所有用户发布的需求
      // 注意：根据接口文档，这里使用新的接口路径
      const response = await sprayDemandAPI.getAllFarmersDemandsForFlyer({
        pageNum,
        pageSize,
        statusStr
      });
      
      console.log('需求列表数据:', response);
      
      if (response && response.records && Array.isArray(response.records)) {
        const newRequests = response.records.map(item => {
          // 转换数据格式以匹配WXML的需求
          return {
            id: item.id,
            type: item.type === 'SPRAY' ? '喷洒' : '巡检',
            address: item.location || '未知地址',
            expectedTime: item.expectedTime || '',
            budget: item.budget || 0,
            status: this.getStatusText(item.status)
          };
        });
        
        const updatedRequests = pageNum === 1 ? newRequests : [...this.data.flyerRequests, ...newRequests];
        
        this.setData({
          flyerRequests: updatedRequests,
          hasMore: updatedRequests.length < response.total,
          pageNum: pageNum + 1
        });
      } else {
        console.error('获取需求列表失败：返回数据格式不正确');
        wx.showToast({
          title: '获取需求列表失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('获取需求列表失败:', error);
      wx.showToast({
        title: '网络异常，请稍后再试',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },
  
  // 根据状态码获取状态文本
  getStatusText: function(status) {
    const statusMap = {
      0: '待接取',
      1: '待接单（已支付）',
      2: '已接单（已支付）',
      3: '作业中（已支付）',
      4: '已支付（已退款）',
      5: '已支付（已完成）',
      6: '已取消'
    };
    return statusMap[status] || '未知状态';
  },
  

  
  // 切换飞手筛选标签
  onTabChange: function(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      selectedTab: tab,
      pageNum: 1,
      hasMore: true,
      flyerRequests: []
    });
    
    // 根据选中的标签设置状态筛选
    if (tab === '全部需求') {
      this.setData({ statusStr: '' });
    } else if (tab === '巡检需求') {
      this.setData({ statusStr: '0' }); // 待接取状态
    } else if (tab === '喷洒需求') {
      this.setData({ statusStr: '1' }); // 待接单（已支付）状态
    }
    
    // 重新加载需求列表
    this.initFlyerData();
  },
  
  // 切换距离筛选
  onDistanceChange: function(e) {
    const distance = e.detail.value;
    this.setData({
      pageNum: 1,
      hasMore: true,
      flyerRequests: []
    });
    
    // 根据选择的距离重新加载数据
    // 注意：这里需要根据实际接口参数进行调整
    this.initFlyerData();
  },
  
  // 上拉加载更多
  onReachBottom: function() {
    if (this.data.userRole === 'flyer') {
      this.initFlyerData();
    }
  },
  
  // 查看需求详情
  viewRequestDetail: function(e) {
    const requestId = e.currentTarget.dataset.id;
    
    // 由于accept-order是tabBar页面，需要使用wx.switchTab跳转
    // 先将id参数存储到本地存储中
    wx.setStorageSync('acceptOrderId', requestId);
    wx.setStorageSync('acceptOrderType', 'all'); // 默认显示全部类型
    
    wx.switchTab({
      url: '/pages/flyer/accept-order/index',
      success: function(res) {
        console.log('页面跳转成功', res);
      },
      fail: function(err) {
        console.error('页面跳转失败', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  },
  
  // 一键接单
  acceptOrder: async function(e) {
    const requestId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认接单',
      content: '确定要接此订单吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            // 调用接单接口
            await sprayDemandAPI.acceptSprayDemand({
              demandId: requestId,
              deviceId: 1 // 这里假设设备ID为1，实际应该从用户选择的设备中获取
            });
            
            wx.showToast({
              title: '接单成功',
              icon: 'success'
            });
            
            // 重新加载需求列表
            this.setData({
              pageNum: 1,
              hasMore: true,
              flyerRequests: []
            });
            this.initFlyerData();
          } catch (error) {
            console.error('接单失败:', error);
            wx.showToast({
              title: '接单失败，请稍后再试',
              icon: 'none'
            });
          }
        }
      }
    });
  },
  
  // 发布巡检需求
  publishInspection: function() {
    wx.navigateTo({
      url: '/pages/farmer/create-inspection-order'
    });
  },
  
  // 发布喷洒需求
  publishSpray: function() {
    wx.navigateTo({
      url: '/pages/farmer/publish-spray-demand'
    });
  },
  
  // 查看巡检报告
  viewReports: function() {
    wx.navigateTo({
      url: '/pages/farmer/view-report'
    });
  },
  
  // 联系客服
  contactService: function() {
    wx.showModal({
      title: '联系客服',
      content: '客服电话：400-123-4567',
      showCancel: false
    });
  },
  
  // 跳转到我的设备页面
  navigateToDevices: function() {
    wx.navigateTo({
      
    });
  },
  
  // 跳转到设备需求页面
  navigateToRequests: function() {
    wx.navigateTo({
      
    });
  },
  
  // 一键响应需求
  respondToRequest: function(e) {
    const requestId = e.currentTarget.dataset.id;
    wx.navigateTo({
      
    });
  },
  
  // 消息图标点击事件
  onMessageTap: function() {
    wx.showToast({
      title: '消息功能待开发',
      icon: 'none'
    });
  }
})
