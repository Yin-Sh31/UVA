const { api } = require('../../utils/request');

Page({
  data: {
    flyerList: [],
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,
    refreshing: false,
    error: false,
    errorMsg: '',
    lastUpdateTime: null
  },
  
  onLoad: function() {
    // 尝试加载缓存数据
    this.loadCachedData();
    // 然后请求最新数据
    this.loadFlyerList();
  },
  
  // 加载缓存数据
  loadCachedData: function() {
    try {
      const cachedData = wx.getStorageSync('flyerListData');
      if (cachedData && cachedData.expireTime > Date.now()) {
        console.log('使用缓存的飞手列表数据');
        this.setData({
          flyerList: cachedData.data.flyerList || [],
          pageNum: cachedData.data.pageNum || 1,
          hasMore: cachedData.data.hasMore || true,
          lastUpdateTime: cachedData.updateTime
        });
      }
    } catch (error) {
      console.error('加载缓存数据失败', error);
    }
  },
  
  // 保存数据到缓存
  saveToCache: function() {
    try {
      const cacheData = {
        data: {
          flyerList: this.data.flyerList,
          pageNum: this.data.pageNum,
          hasMore: this.data.hasMore
        },
        expireTime: Date.now() + 5 * 60 * 1000, // 5分钟过期
        updateTime: new Date().toLocaleString()
      };
      wx.setStorageSync('flyerListData', cacheData);
    } catch (error) {
      console.error('保存缓存数据失败', error);
    }
  },
  
  // 加载飞手列表
  loadFlyerList: async function() {
    if (!this.data.hasMore || this.data.loading) {
      return;
    }
    
    this.setData({
      loading: true,
      error: false,
      errorMsg: ''
    });
    
    try {
      // 使用真实API获取飞手列表数据，添加超时处理
      const { pageNum, pageSize } = this.data;
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('请求超时')), 10000);
      });
      
      const apiPromise = api.getFarmerFlyerList(pageNum, pageSize);
      const response = await Promise.race([apiPromise, timeoutPromise]);
      
      if (!response || !response.data) {
        throw new Error('无效的返回数据');
      }
      
      // console.log('飞手列表API调用成功，返回数据:', response);
      
      // 检查返回数据结构
      let flyersList = [];
      if (response.data.records && Array.isArray(response.data.records)) {
        // 转换数据格式以匹配WXML的需求
        flyersList = response.data.records.map(function(flyer) {
          if (!flyer) return null;
          
          return {
            id: flyer.flyerId || flyer.userId || '',
            name: flyer.userName || '未知飞手',
            avatar: flyer.avatar || '/assets/images/user-avatar.png',
            rating: flyer.reputation || 0,
            totalScore: flyer.totalScore || 0,
            starLevel: flyer.starLevel || 0,
            evaluationCount: flyer.evaluationCount || 0,
            positiveRate: flyer.positiveRate || 0,
            completedOrders: flyer.completedOrders || 0,
            introduction: flyer.introduction || '',
            licenseType: flyer.licenseType || '',
            creditScore: flyer.creditScore || 0,
            location: flyer.location || '未知位置',
            distance: flyer.distance ? (typeof flyer.distance === 'string' ? flyer.distance : flyer.distance + 'km') : '未知距离',
            skillLevel: flyer.skillLevel || '通用作业',
            pricePerAcre: flyer.pricePerAcre || '未设置',
            isFree: flyer.isFree === 1,
            experience: flyer.experience || '',
            // 添加格式化的评分显示
            formattedRating: (flyer.totalScore || 0).toFixed(1)
          };
        }).filter(function(flyer) { return flyer !== null; });
      }
      
      // 获取总记录数和总页数
      const total = response.data?.total || 0;
      const totalPages = Math.ceil(total / this.data.pageSize);
      
      this.setData({
        flyerList: this.data.pageNum === 1 ? flyersList : [...this.data.flyerList, ...flyersList],
        hasMore: this.data.pageNum < totalPages,
        pageNum: this.data.pageNum + 1,
        lastUpdateTime: new Date().toLocaleString()
      });
      
      // 保存数据到缓存
      this.saveToCache();
      console.log('飞手列表数据已更新并保存到缓存');
    } catch (error) {
      console.error('加载飞手列表失败', error);
      this.setData({
        error: true,
        errorMsg: error.message || '加载失败，请稍后重试'
      });
      
      // 如果是首次加载失败且没有缓存数据，显示错误提示
      if (this.data.pageNum === 1 && this.data.flyerList.length === 0) {
        wx.showToast({
          title: error.message || '加载失败，请稍后重试',
          icon: 'none',
          duration: 2000
        });
      }
    } finally {
      this.setData({
        loading: false,
        refreshing: false
      });
    }
  },
  
  // 刷新页面
  onRefresh: function() {
    this.setData({
      refreshing: true,
      pageNum: 1,
      hasMore: true,
      error: false,
      errorMsg: ''
    });
    this.loadFlyerList();
  },
  
  // 重新加载
  retryLoad: function() {
    this.setData({
      pageNum: 1,
      hasMore: true,
      error: false,
      errorMsg: ''
    });
    this.loadFlyerList();
  },
  
  // 上拉加载更多
  onLoadMore: function() {
    this.loadFlyerList();
  },
  
  // 查看飞手详情
  viewFlyerDetail: function(e) {
    const flyerId = e.currentTarget.dataset.id;
    const flyerName = e.currentTarget.dataset.name;
    
    wx.navigateTo({
      url: `/pages/farmer/flyer-detail?flyerId=${flyerId}`
    });
  },
  
  // 联系飞手
  contactFlyer: function(e) {
    const flyerId = e.currentTarget.dataset.id;
    const flyerName = e.currentTarget.dataset.name;
    const flyerPhone = e.currentTarget.dataset.phone || '';
    
    if (!flyerId) {
      wx.showToast({
        title: '无法获取飞手信息',
        icon: 'none'
      });
      return;
    }
    
    // 显示联系方式选择弹窗
    wx.showActionSheet({
      itemList: ['在线联系', '拨打电话'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 在线联系 - 直接跳转到聊天详情页面，创建会话
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

  // 邀请飞手
  inviteFlyer: function(e) {
    const flyerId = e.currentTarget.dataset.id;
    const flyerName = e.currentTarget.dataset.name;
    const isFree = e.currentTarget.dataset.free;
    
    // 检查飞手是否空闲
    if (!isFree) {
      wx.showToast({
        title: '该飞手当前忙，请选择其他飞手',
        icon: 'none',
        duration: 2000
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
  },
  
  // 页面卸载时清理
  onUnload: function() {
    // 可以在这里清理一些资源或定时器
  },
  
  // 下拉刷新触发（系统自带）
  onPullDownRefresh: function() {
    this.onRefresh();
  },
  
  // 上拉触底触发（系统自带）
  onReachBottom: function() {
    this.onLoadMore();
  }
});