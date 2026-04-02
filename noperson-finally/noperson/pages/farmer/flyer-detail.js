const { api } = require('../../utils/request');

Page({
  data: {
    flyerDetail: null,
    evaluations: [],
    loading: false,
    evaluationsLoading: false,
    error: false,
    errorMsg: '',
    flyerId: null
  },
  
  onLoad: function(options) {
    console.log('飞手详情页面加载，接收到的参数:', options);
    const flyerId = options.flyerId;
    console.log('接收到的flyerId:', flyerId);
    
    if (!flyerId) {
      console.error('参数错误，flyerId为空');
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      wx.navigateBack();
      return;
    }
    
    this.setData({
      flyerId: flyerId
    });
    
    this.loadFlyerDetail();
  },
  
  // 加载飞手详情
  loadFlyerDetail: function() {
    console.log('开始加载飞手详情，flyerId:', this.data.flyerId);
    this.setData({
      loading: true,
      error: false,
      errorMsg: ''
    });
    
    api.getFlyerInfo(this.data.flyerId).then(function(response) {
      console.log('飞手详情请求成功，响应数据:', response);
      if (response && response.code === 200 && response.data) {
        const flyer = response.data;
        console.log('处理飞手数据:', flyer);
        
        // 添加格式化的评分
        if (flyer.totalScore !== undefined) {
          flyer.formattedRating = flyer.totalScore.toFixed(1);
        } else if (flyer.reputation !== undefined) {
          flyer.formattedRating = flyer.reputation.toFixed(1);
        }
        
        this.setData({
          flyerDetail: flyer,
          loading: false
        });
        
        // 加载评价列表
        this.loadEvaluations();
        console.log('成功设置飞手详情数据');
      } else {
        console.error('获取飞手信息失败，响应数据:', response);
        this.setData({
          error: true,
          errorMsg: '获取飞手信息失败',
          loading: false
        });
      }
    }.bind(this)).catch(function(error) {
      console.error('加载飞手详情失败', error);
      this.setData({
        error: true,
        errorMsg: '加载失败，请稍后重试',
        loading: false
      });
    }.bind(this));
  },
  
  // 联系飞手
  contactFlyer: function() {
    if (!this.data.flyerDetail) return;
    
    const flyerId = this.data.flyerDetail.userId;
    const flyerName = this.data.flyerDetail.userName;
    const flyerPhone = this.data.flyerDetail.phone || '';
    
    if (!flyerId) {
      wx.showToast({
        title: '无法获取飞手信息',
        icon: 'none'
      });
      return;
    }
    
    wx.showActionSheet({
      itemList: ['在线联系', '拨打电话'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 在线联系
          wx.navigateTo({
            url: `/pages/chat/chat?targetUserId=${flyerId}&targetUserName=${encodeURIComponent(flyerName)}`,
            success: function(res) {
              console.log('跳转成功:', res);
            },
            fail: function(err) {
              console.error('跳转失败:', err);
              wx.showToast({
                title: '跳转失败，请重试',
                icon: 'none'
              });
            }
          });
        } else if (res.tapIndex === 1) {
          // 拨打电话
          if (flyerPhone) {
            wx.makePhoneCall({
              phoneNumber: flyerPhone,
              fail: () => {
                wx.showToast({
                  title: '拨打电话失败',
                  icon: 'none'
                });
              }
            });
          } else {
            wx.showToast({
              title: '飞手未设置电话号码',
              icon: 'none'
            });
          }
        }
      },
      fail: (err) => {
        console.log('取消选择联系方式', err);
      }
    });
  },

  // 加载评价列表
  loadEvaluations: function() {
    this.setData({
      evaluationsLoading: true
    });
    
    api.getFlyerEvaluationList(this.data.flyerId).then(function(response) {
      if (response && response.code === 200 && response.data) {
        // 格式化评价数据，计算平均评分
        const formattedEvaluations = response.data.map(item => {
          const averageScore = (item.scoreQuality + item.scorePunctuality + item.scoreAttitude + item.scoreEfficiency) / 4;
          return {
            ...item,
            averageScore: averageScore.toFixed(1)
          };
        });
        
        this.setData({
          evaluations: formattedEvaluations,
          evaluationsLoading: false
        });
      } else {
        console.error('获取评价列表失败', response);
        this.setData({
          evaluationsLoading: false
        });
      }
    }.bind(this)).catch(function(error) {
      console.error('加载评价列表失败', error);
      this.setData({
        evaluationsLoading: false
      });
    }.bind(this));
  },

  // 邀请飞手接单
  inviteFlyer: function() {
    if (!this.data.flyerDetail) return;
    
    const flyerId = this.data.flyerDetail.userId;
    const flyerName = this.data.flyerDetail.userName;
    
    if (!flyerId) {
      wx.showToast({
        title: '无法获取飞手信息',
        icon: 'none'
      });
      return;
    }
    
    wx.showActionSheet({
      itemList: ['邀请巡检', '邀请喷洒'],
      success: (res) => {
        if (res.tapIndex === 0) {
          wx.navigateTo({
            url: `/pages/farmer/create-inspection-order?flyerId=${flyerId}&flyerName=${flyerName}`
          });
        } else if (res.tapIndex === 1) {
          wx.navigateTo({
            url: `/pages/farmer/publish-spray-demand?flyerId=${flyerId}&flyerName=${flyerName}`
          });
        }
      },
      fail: (err) => {
        console.log('取消操作', err);
      }
    });
  }
});