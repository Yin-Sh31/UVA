// 飞手的个人中心页面
const { api } = require('../../../utils/request.js');

Page({
  data: {
    userInfo: {
      realName: '张飞手',
      avatar: '/assets/images/user-avatar.png',
      balance: '1280.50',
      skillLevel: '高级飞手',
      certification: '已认证'
    },
    menuItems: [
      {
        id: 'my_info',
        name: '我的信息',
        icon: '/assets/images/user-info.png'
      },
      {
        id: 'my_wallet',
        name: '我的钱包',
        icon: '/assets/images/wallet.png',
        url: '/pages/payment/index?type=flyer'
      },
      {
        id: 'my_orders',
        name: '我的接单',
        icon: '/assets/images/reports.png',
        url: '/pages/flyer/my-order-list/index'
        },
        {
        id: 'qualification',
        name: '资质认证',
        icon: '/assets/images/profile.png',
        url: '/pages/flyer/qualification/index'
      },
      {
        id: 'messages',
        name: '消息中心',
        icon: '/assets/images/no-message.svg',
        url: '/pages/flyer/chat/list'
      },
      {
        id: 'payment_history',
        name: '收支明细',
        icon: '/assets/images/payment-icon.svg',
        url: '/pages/transaction/index'
      },
      {
        id: 'my_rentals',
        name: '我的租借',
        icon: '/assets/images/wurenji.jpg',
        url: '/pages/flyer/my-rentals/index'
      },
      {
        id: 'current_rentals',
        name: '当前租借',
        icon: '/assets/images/wurenji.jpg',
        url: '/pages/flyer/current-rentals/index'
      },
      {
        id: 'settings',
        name: '设置',
        icon: '/assets/images/system-icon.svg',
        url: '/pages/flyer/settings/index'
      }
    ]
  },

  onLoad: function() {
    // 这里可以从服务器加载用户信息
    this.loadUserInfo();
  },

  onShow: function() {
    // 每次显示页面时重新加载用户信息，确保数据最新
    this.loadUserInfo();
  },

  // 加载用户信息
  loadUserInfo: function() {
    wx.showLoading({
      title: '加载中...',
    });
    
    // 先获取飞手个人信息
    api.getFlyerInfo()
      .then(userResponse => {
        // API响应格式为 {code: 200, message: "操作成功", data: {...}}，需要访问data
        const userData = userResponse?.data || {};
        console.log('获取飞手信息成功:', userData);
        
        // 然后使用getUserBalance获取余额信息
        return api.getUserBalance()
          .then(balanceResponse => {
            // 同样需要访问data字段
            const balanceData = balanceResponse?.data || {};
            console.log('获取余额信息成功:', balanceData);
            
            // 使用展开运算符确保所有后端返回的字段都被保留
            const userInfo = {
              // 首先展开所有后端返回的字段
              ...userData,
              
              // 然后设置必要的默认值和格式化字段
              avatar: userData.avatar || '/assets/images/user-avatar.png',
              balance: balanceData.balance ? balanceData.balance.toFixed(2) : userData.balance ? userData.balance.toFixed(2) : '0.00',
              skillLevel: this.getSkillLevelText(userData.skillLevel),
              certification: this.getAuditStatusText(userData.auditStatus),
              realName: userData.realName || userData.userName || userData.username || '飞手用户',
              
              // 信用评分相关字段
              totalScore: userData.totalScore || 0,
              starLevel: userData.starLevel || 0,
              evaluationCount: userData.evaluationCount || 0,
              positiveRate: userData.positiveRate || 0,
              formattedScore: (userData.totalScore || 0).toFixed(1),
              
              // 保留完整数据引用
              fullInfo: userData
            };
            
            // 保存到本地存储
            wx.setStorageSync('flyerUserInfo', userInfo);
            
            this.setData({
              userInfo: userInfo
            });
          })
          .catch(balanceError => {
            console.error('获取余额信息失败，使用个人信息中的余额:', balanceError);
            // 余额获取失败时，使用个人信息中的余额
            const userInfo = {
              ...userData,
              avatar: userData.avatar || '/assets/images/user-avatar.png',
              balance: userData.balance ? userData.balance.toFixed(2) : '0.00',
              skillLevel: this.getSkillLevelText(userData.skillLevel),
              certification: this.getAuditStatusText(userData.auditStatus),
              realName: userData.realName || userData.userName || userData.username || '飞手用户',
              
              // 信用评分相关字段
              totalScore: userData.totalScore || 0,
              starLevel: userData.starLevel || 0,
              evaluationCount: userData.evaluationCount || 0,
              positiveRate: userData.positiveRate || 0,
              formattedScore: (userData.totalScore || 0).toFixed(1),
              
              fullInfo: userData
            };
            
            wx.setStorageSync('flyerUserInfo', userInfo);
            this.setData({ userInfo });
          });
      })
      .catch(err => {
        console.error('获取飞手信息失败:', err);
        // 失败时使用本地缓存或默认数据
        const userInfo = wx.getStorageSync('flyerUserInfo') || this.data.userInfo;
        this.setData({
          userInfo: userInfo
        });
      })
      .finally(() => {
        wx.hideLoading();
      });
  },
  
  // 将技能等级转换为文本
  getSkillLevelText: function(skillLevel) {
    const skillLevelMap = {
      '喷洒认证': '喷洒飞手',
      '巡检认证': '巡检飞手',
      '全能认证': '全能飞手'
    };
    return skillLevelMap[skillLevel] || '初级飞手';
  },
  
  // 将审核状态转换为文本
  getAuditStatusText: function(auditStatus) {
    const auditStatusMap = {
      0: '待审核',
      1: '已认证',
      2: '未通过'
    };
    return auditStatusMap[auditStatus] || '未认证';
  },

  // 导航到其他页面
  navigateToPage: function(e) {
    console.log('导航函数被调用:', e);
    
    // 检查事件对象
    if (!e || !e.currentTarget || !e.currentTarget.dataset) {
      console.error('事件对象不完整:', e);
      wx.showToast({
        title: '操作异常',
        icon: 'none'
      });
      return;
    }
    
    const id = e.currentTarget.dataset.id;
    console.log('点击的菜单项ID:', id);
    
    try {
      // 对于"我的信息"菜单项，直接导航到编辑页面
      if (id === 'my_info') {
        console.log('导航到编辑页面');
        
        // 直接导航，不使用setTimeout延迟，避免可能的事件通道问题
        wx.navigateTo({
          url: '/pages/flyer/edit-profile/index',
          success: function(res) {
            console.log('导航成功，准备传递数据');
            // 传递用户信息给编辑页面
            res.eventChannel.emit('userInfoData', {
              userInfo: this.data.userInfo
            });
          }.bind(this),
          fail: function(err) {
            console.error('导航失败:', err);
            wx.showToast({
              title: '导航失败，请重试',
              icon: 'none'
            });
          }
        });
      } else if (id === 'my_wallet') {
        // 专门处理我的钱包菜单项
        console.log('处理我的钱包菜单项点击');
        
        // 使用正确的钱包页面路径
        wx.navigateTo({
          url: '/pages/payment/wallet',
          success: function() {
            console.log('成功导航到钱包页面');
          },
          fail: function(err) {
            console.error('钱包页面导航失败:', err);
            wx.showToast({
              title: '导航失败，请重试',
              icon: 'none'
            });
          }
        });
      } else {
              // 查找对应的菜单项
              const menuItem = this.data.menuItems.find(item => item.id === id);
              
              if (menuItem && menuItem.url) {

                
                console.log('导航到页面:', menuItem.url);
                wx.navigateTo({              url: menuItem.url,
                  fail: function(err) {
                    console.error('导航失败:', err);
                    wx.showToast({
                      title: '导航失败，请重试',
                      icon: 'none'
                    });              }
                });
              } else {
                console.warn('未找到对应的菜单项或URL:', id);
                wx.showToast({
                  title: '功能未实现',              icon: 'none'
                });              }
            }
    } catch (error) {
      console.error('导航函数异常:', error);
      wx.showToast({
        title: '操作异常，请重试',
        icon: 'none'
      });
    }
  },

  // 退出登录
  logout: function() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除本地存储的用户信息和登录状态
          wx.removeStorageSync('flyerUserInfo');
          wx.removeStorageSync('token');
          
          // 跳转到登录页面
          wx.reLaunch({
            url: '/pages/auth/login'
          });
        }
      }
    });
  },

  // 点击余额查看收支明细  viewBalanceDetail: function() {    wx.navigateTo({      url: '/pages/transaction/index',      success: function() {        console.log('成功导航到收支明细页面');      },      fail: function(err) {        console.error('收支明细页面导航失败:', err);        wx.showToast({          title: '导航失败，请重试',          icon: 'none'        });      }    });  }
});