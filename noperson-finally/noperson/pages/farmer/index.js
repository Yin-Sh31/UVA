// farmer/index.js
const { api } = require('../../utils/request');
const { getBanners } = require('../../utils/services/banner');

Page({
  data: {
    userInfo: {},
    // 轮播图数据 - 使用默认数据
    bannerImages: [
      { id: '1', title: '轮播图1', imageUrl: '/assets/images/1.jpg', targetUrl: '', url: '/assets/images/1.jpg', link: '' },
      { id: '2', title: '轮播图2', imageUrl: '/assets/images/2.jpg', targetUrl: '', url: '/assets/images/2.jpg', link: '' }
    ],
    // 临时存储默认轮播图，仅在API调用失败时使用
    defaultBanners: [
      { id: '1', title: '轮播图1', imageUrl: '/assets/images/1.jpg', targetUrl: '', url: '/assets/images/1.jpg', link: '' },
      { id: '2', title: '轮播图2', imageUrl: '/assets/images/2.jpg', targetUrl: '', url: '/assets/images/2.jpg', link: '' }
    ],
    loading: false, // 轮播图加载状态
    bannerLoadTime: 0, // 轮播图加载时间
    recentDemands: [
      // 模拟数据，实际应从API获取
      {
        id: '1',
        type: 'inspection',
        location: '东湾村3组农田',
        status: '进行中'
      },
      {
        id: '2', 
        type: 'spray',
        location: '西湖村5组果园',
        status: '待接单'
      },
      {
        id: '3',
        type: 'inspection',
        location: '北岗村2组茶园',
        status: '已完成'
      }
    ],
    flyerList: [], // 初始为空数组，通过API获取实际数据
    allFlyers: [], // 存储全部飞手数据
    pageNum: 1,    // 分页参数
    pageSize: 100,  // 设置较大的值获取全部飞手
    // 病虫害识别对话框相关
    diseaseDialogVisible: false,
    selectedImage: '',
    detecting: false,
    detectionResult: null,
    // 作物类型数据
    cropTypes: ['水稻', '小麦', '玉米', '大豆', '棉花', '蔬菜', '水果', '茶叶', '烟草', '其他'],
    cropTypeIndex: 0
  },
  
  onLoad: function() {
    // 显示全局加载状态
    wx.showLoading({ title: '加载中...' });
    // 并行加载所有数据
    Promise.all([
      this.fetchBanners(),
      this.loadUserInfo(),
      this.loadRecentDemands(),
      this.loadFlyerList()
    ]).finally(() => {
      wx.hideLoading();
    });
  },
  
  onPullDownRefresh: function() {
    // 并行刷新所有数据
    Promise.all([
      this.fetchBanners(),
      this.loadUserInfo(),
      this.loadRecentDemands(),
      this.loadFlyerList()
    ]).finally(() => {
      // 停止下拉刷新动画
      wx.stopPullDownRefresh();
    });
  },
  
  /**
   * 获取农户端轮播图
   */
  fetchBanners: async function() {
    const startTime = Date.now();
    
    try {
      // 设置加载状态
      this.setData({ loading: true });
      
      // 引入banner服务
      const { getBanners } = require('../../utils/services/banner');
      
      // 调用getBanners服务
      let banners = await getBanners('farmer');
      
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      // 安全处理：确保banners是有效数据
      let bannerList = [];
      if (Array.isArray(banners)) {
        bannerList = banners;
      } else if (banners && banners.data && Array.isArray(banners.data)) {
        bannerList = banners.data;
      } else if (banners && banners.records && Array.isArray(banners.records)) {
        bannerList = banners.records;
      }
      
      // 安全过滤：确保只处理有效的轮播图对象
      const safeBanners = bannerList.filter(item => item && typeof item === 'object' && item.imageUrl);
      
      if (safeBanners.length > 0) {
        // 处理轮播图数据
        const processedBanners = safeBanners.map((banner, index) => {
          return {
            id: banner.id || `api_banner_${index}`,
            title: banner.title || '轮播图' + (index + 1),
            // 确保图片URL正确拼接后端服务器地址
            imageUrl: banner.imageUrl ? 
              (banner.imageUrl.startsWith('http') ? banner.imageUrl : 'http://localhost:8082' + banner.imageUrl) : 
              '',
            // 兼容现有代码使用的字段
            url: banner.imageUrl ? 
              (banner.imageUrl.startsWith('http') ? banner.imageUrl : 'http://localhost:8082' + banner.imageUrl) : 
              '',
            link: banner.targetUrl || banner.link || '',
            targetUrl: banner.targetUrl || banner.link || '',
            // 添加标记避免undefined问题
            __valid: true
          };
        });
        
        this.setData({ 
          bannerImages: processedBanners,
          loading: false,
          bannerLoadTime: loadTime
        });
        
        return true;
      } else {
        // 确保默认数据安全
        const safeDefaultBanners = this.getSafeBanners(this.data.defaultBanners);
        this.setData({ 
          bannerImages: safeDefaultBanners,
          loading: false,
          bannerLoadTime: loadTime
        });
        
        return true;
      }
      
    } catch (error) {
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      // 显示错误提示
      wx.showToast({ 
        title: '轮播图加载失败', 
        icon: 'none'
      });
      
      // 异常情况下使用默认轮播图，确保安全
      try {
        const safeDefaultBanners = this.getSafeBanners(this.data.defaultBanners);
        this.setData({ 
          bannerImages: safeDefaultBanners,
          loading: false,
          bannerLoadTime: loadTime
        });
      } catch (setDataError) {
        // 最后防线：使用空数组防止渲染错误
        this.setData({ 
          bannerImages: [],
          loading: false,
          bannerLoadTime: loadTime
        });
      }
      
      return false;
    } finally {
      // 停止下拉刷新动画，添加错误处理
      try {
        wx.stopPullDownRefresh();
      } catch (e) {
        // 忽略错误
      }
    }
  },
  
  // 安全处理轮播图数据的辅助方法
  getSafeBanners: function(banners) {
    if (!Array.isArray(banners)) {
      console.error('【农户端】错误: banners参数不是数组');
      return [];
    }
    return banners
      .filter(item => item && typeof item === 'object' && item.imageUrl)
      .map((banner, index) => ({
        id: banner.id || `default_${index}`,
        title: banner.title || '轮播图' + (index + 1),
        // 确保图片URL正确拼接后端服务器地址
        imageUrl: banner.imageUrl ? 
          (banner.imageUrl.startsWith('http') ? banner.imageUrl : 'http://localhost:8082' + banner.imageUrl) : 
          '',
        // 兼容现有代码使用的字段
        url: banner.imageUrl ? 
          (banner.imageUrl.startsWith('http') ? banner.imageUrl : 'http://localhost:8082' + banner.imageUrl) : 
          '',
        targetUrl: banner.targetUrl || banner.url || banner.link || '',
        link: banner.targetUrl || banner.url || banner.link || '',
        __valid: true
      }));
  },

  /**
   * 统一的轮播图点击事件处理
   */
  onBannerClick: function(e) {
    const { index } = e.currentTarget.dataset;
    const banner = this.data.bannerImages[index];
    
    if (!banner) return;
    
    const link = banner.targetUrl || banner.link;
    if (link) {
      try {
        // 根据链接类型进行不同处理
        if (link.startsWith('/pages/')) {
          wx.navigateTo({ url: link });
        } else if (link.startsWith('http')) {
          // 检查是否有web-view页面
          wx.navigateTo({
            url: `/pages/web-view/index?url=${encodeURIComponent(link)}`,
            fail: () => {
              // 如果没有web-view页面，显示提示
              wx.showModal({
                title: '提示',
                content: '请在浏览器中打开链接',
                showCancel: false
              });
            }
          });
        }
      } catch (error) {
        console.error('轮播图跳转失败:', error);
        wx.showToast({ title: '跳转失败', icon: 'none' });
      }
    }
  },
  
  onShow: function() {
    // 每次页面显示时刷新数据
    this.loadUserInfo();
    this.loadRecentDemands();
    this.loadFlyerList();
  },
  
  // 加载用户信息
  loadUserInfo: function() {
    return new Promise((resolve) => {
      try {
        const app = getApp();
        if (app.globalData.userInfo) {
          this.setData({
            userInfo: app.globalData.userInfo
          });
        } else {
          // 如果全局没有用户信息，尝试从本地存储获取
          const storedUserInfo = wx.getStorageSync('userInfo');
          if (storedUserInfo) {
            this.setData({
              userInfo: storedUserInfo
            });
          }
        }
      } catch (error) {
        console.error('加载用户信息失败:', error);
      } finally {
        resolve();
      }
    });
  },
  
  // 加载最近需求数据
  loadRecentDemands: function() {
    return new Promise((resolve) => {
      // 使用新的统一接口获取所有需求数据
      api.getFarmerAllDemandsList(1, 5).then(function(results) {
        var recentDemands = [];
        
        // 处理统一返回的SprayDemandVO对象列表
        if (results) {
          var dataList = [];
          if (results.data && results.data.records) {
            dataList = results.data.records;
          } else if (results.data && Array.isArray(results.data)) {
            dataList = results.data;
          } else if (results.records) {
            dataList = results.records;
          } else if (Array.isArray(results)) {
            dataList = results;
          } else {
            dataList = [results];
          }
          
          recentDemands = dataList.map(function(demand) {
          // 确保对象不为空
          if (!demand) return null;
          
          // 检查所有可能的字段名
          const actualStatus = demand.status || demand.demandStatus || 0;
          const actualLocation = demand.fieldLocation || demand.location || demand.landLocation || '';
          const actualOrderType = demand.orderType || demand.demandType || 1;
          
          // 只显示未完成的需求（状态为已发布或已接取）
          // 需求状态：0-待接取(已发布), 1-处理中(已接取), 2-作业中, 3-待确认, 4-已完成, 5-已取消
          if (actualStatus === 4 || actualStatus === 5) {
            return null; // 过滤掉已完成和已取消的需求
          }
          
          const statusText = actualStatus === 0 ? '已发布' : (actualStatus === 1 ? '已接取' : (actualStatus === 2 ? '作业中' : (actualStatus === 3 ? '待确认' : '') ));
          const type = actualOrderType === 2 ? 'inspection' : 'spray';
          const typeDesc = demand.orderTypeDesc || (type === 'inspection' ? '巡检' : '喷洒');
          
          return {
            id: demand.id || demand.demandId || '',
            type: type, // 根据orderType区分类型
            typeDesc: typeDesc,
            location: actualLocation,
            status: statusText,
            createTime: demand.createTime || demand.create_time || null
          };
        }).filter(item => item !== null); // 过滤掉null值
        }
        
        // 按时间排序（如果有时间字段的话）
        if (recentDemands.length > 0) {
          if (recentDemands[0].createTime) {
            recentDemands.sort(function(a, b) {
              return new Date(b.createTime) - new Date(a.createTime);
            });
          }
          
          // 取最近的几条数据
          recentDemands = recentDemands.slice(0, 5);
        }
        
        // 无论是否有数据，都更新页面
        this.setData({
          recentDemands: recentDemands
        });
      }.bind(this)).catch(function(err) {
        console.error('获取需求列表失败:', err);
        // 发生错误时保持原有数据不变，避免页面闪烁
      }).finally(() => {
        resolve();
      });
    });
  },
  
  // 发布巡检需求
  createInspectionDemand: function() {
    wx.navigateTo({
      url: '/pages/farmer/create-inspection-order'
    });
  },
  
  // 发布喷洒需求
  createSprayDemand: function() {
    wx.navigateTo({
      url: '/pages/farmer/publish-spray-demand'
    });
  },
  
  // 查看所有需求
  viewAllDemands: function() {
    wx.navigateTo({
      url: '/pages/farmer/my-demand-list'
    });
  },
  
  // 查看需求详情
  viewDemandDetail: function(e) {
    const demandId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/farmer/demand-detail?id=${demandId}`
    });
  },
  
  // 查看巡检报告
  viewInspectionReports: function() {
    wx.navigateTo({
      url: '/pages/farmer/inspection-reports'
    });
  },
  
  // 加载飞手列表
  loadFlyerList: function() {
    return new Promise((resolve) => {
      const { pageNum, pageSize } = this.data;
      
      // 使用真实API获取飞手列表数据，传入分页参数获取全部飞手
      api.getFarmerFlyerList(pageNum, pageSize).then(function(res) {
        console.log('飞手列表API返回数据:', res);
        // 初始化飞手数据数组
        let flyerData = [];
        
        // 检查返回数据结构
        if (res && res.records && Array.isArray(res.records)) {
          flyerData = res.records;
          console.log('飞手列表数据:', flyerData);
        } else if (res && res.data && res.data.records && Array.isArray(res.data.records)) {
          flyerData = res.data.records;
          console.log('飞手列表数据(res.data.records):', flyerData);
        }
        
        // 转换数据格式以匹配WXML的需求
        const formattedFlyers = flyerData.map(function(flyer) {
          console.log('原始飞手数据:', flyer);
          // 确保对象不为空
          if (!flyer) return null;
          
          // 处理isFree字段：接口返回1表示空闲，0表示忙碌
          const isFreeStatus = flyer.isFree === 1;
          
          const result = {
            id: flyer.userId || flyer.id || '',
            username: flyer.userName || '未知飞手',
            avatar: flyer.avatar || '/assets/images/user-avatar.png',
            licenseType: flyer.licenseType || '',
            rating: flyer.reputation || 0,
            creditScore: flyer.creditScore || 0,
            completedOrders: flyer.completedOrders || 0,
            location: flyer.location || '未知位置',
            distance: flyer.distance ? 
              (typeof flyer.distance === 'string' ? flyer.distance : flyer.distance + 'km') : 
              '未知距离',
            skillLevel: flyer.skillLevel || '通用作业',
            pricePerAcre: flyer.pricePerAcre || '未设置',
            isFree: isFreeStatus,
            introduction: flyer.introduction || '',
            experience: flyer.experience || ''
          };
          console.log('格式化后的飞手数据:', result);
          return result;
        }).filter(function(flyer) { return flyer !== null; });
        
        // 存储全部飞手数据
        this.setData({
          allFlyers: formattedFlyers
        });
        
        // 随机选择4个飞手显示
        const randomFlyers = this.getRandomFlyers(formattedFlyers, 4);
        
        // 设置最终飞手列表数据
        this.setData({
          flyerList: randomFlyers
        });
      }.bind(this)).catch(function(err) {
        console.error('获取飞手列表失败:', err);
        // 错误时保持原有数据，避免页面闪烁
      }).finally(() => {
        resolve();
      });
    });
  },
  
  // 随机选择指定数量的飞手
  getRandomFlyers: function(flyers, count) {
    if (!flyers || flyers.length === 0) {
      return [];
    }
    
    // 如果飞手数量小于等于需要的数量，直接返回全部
    if (flyers.length <= count) {
      return flyers;
    }
    
    // 随机选择飞手
    const shuffled = [...flyers].sort(() => 0.5 - Math.random());
    return shuffled.slice(0, count);
  },
  
  // 查看飞手详情
  viewFlyerDetail: function(e) {
    console.log('点击飞手卡片，事件对象:', e);
    const flyerId = e.currentTarget.dataset.flyerId;
    console.log('获取到的flyerId:', flyerId);
    wx.navigateTo({
      url: `/pages/farmer/flyer-detail?flyerId=${flyerId}`,
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
  },
  
  // 查看全部飞手
  viewAllFlyers: function() {
    wx.navigateTo({
      url: '/pages/farmer/flyer-list'
    });
  },
  
  // 联系客服
  contactCustomerService: function() {
    wx.showActionSheet({
      itemList: ['拨打客服电话', '查看工作时间'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 拨打电话
          wx.makePhoneCall({
            phoneNumber: '4001234567',
            fail: () => {
              wx.showToast({ title: '拨打电话失败', icon: 'none' });
            }
          });
        } else if (res.tapIndex === 1) {
          // 查看工作时间
          wx.showModal({
            title: '工作时间',
            content: '客服热线：400-123-4567\n工作时间：9:00-18:00',
            showCancel: false
          });
        }
      }
    });
  },
  
  // 导航到首页
  navigateToHome: function() {
    // 已经在首页，不需要跳转
  },
  
  // 导航到我的需求
  navigateToMyDemands: function() {
    wx.redirectTo({
      url: '/pages/farmer/my-demand-list'
    });
  },

  // 导航到消息中心
  navigateToMessages: function() {
    wx.redirectTo({
      url: '/pages/farmer/chat/list'
    });
  },

  // 导航到我的页面
  navigateToProfile: function() {
    wx.redirectTo({
      url: '/pages/farmer/profile'
    });
  },

  // 显示病虫害识别对话框
  showDiseaseDetectionDialog: function() {
    this.setData({
      diseaseDialogVisible: true,
      selectedImage: '',
      detectionResult: null
    });
  },

  // 隐藏病虫害识别对话框
  hideDiseaseDetectionDialog: function() {
    this.setData({
      diseaseDialogVisible: false,
      selectedImage: '',
      detectionResult: null,
      detecting: false
    });
  },

  // 阻止对话框内容点击关闭
  preventClose: function() {
    // 阻止事件冒泡
  },

  // 作物类型选择
  onCropTypeChange: function(e) {
    this.setData({
      cropTypeIndex: e.detail.value
    });
  },

  // 选择图片
  chooseImage: function() {
    const that = this;
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        const tempFilePaths = res.tempFilePaths;
        that.setData({
          selectedImage: tempFilePaths[0],
          detectionResult: null
        });
        console.log('选择图片成功:', tempFilePaths[0]);
      },
      fail: function(err) {
        console.error('选择图片失败:', err);
        wx.showToast({
          title: '选择图片失败',
          icon: 'none'
        });
      }
    });
  },

  // 重新上传图片
  reuploadImage: function() {
    this.chooseImage();
  },

  // 开始识别
  startDetection: function() {
    if (!this.data.selectedImage) {
      wx.showToast({
        title: '请先选择图片',
        icon: 'none'
      });
      return;
    }

    this.setData({
      detecting: true,
      detectionResult: null
    });

    wx.showLoading({
      title: 'AI正在分析图片...'
    });

    // 上传图片到阿里云OSS
    this.uploadImageToOSS().then(imageUrl => {
      console.log('图片上传成功:', imageUrl);
      // 调用后端AI诊断接口
      return this.callAIDiagnoseAPI(imageUrl);
    }).then(result => {
      console.log('识别结果:', result);
      this.setData({
        detectionResult: result,
        detecting: false
      });
      wx.hideLoading();
    }).catch(err => {
      console.error('识别失败:', err);
      wx.hideLoading();
      wx.showToast({
        title: '识别失败，请重试',
        icon: 'none'
      });
      this.setData({
        detecting: false
      });
    });
  },

  // 上传图片到阿里云OSS
  uploadImageToOSS: function() {
    return new Promise((resolve, reject) => {
      const filePath = this.data.selectedImage;
      
      wx.uploadFile({
        url: 'http://localhost:8082/upload/image',
        filePath: filePath,
        name: 'file',
        success: function(res) {
          console.log('上传接口返回:', res);
          try {
            const data = JSON.parse(res.data);
            console.log('解析后的数据:', data);
            if (data.code === 200) {
              console.log('上传成功，返回URL:', data.message);
              resolve(data.message);
            } else {
              reject(new Error(data.message || '上传失败'));
            }
          } catch (e) {
            console.error('解析返回数据失败:', e);
            reject(new Error('解析返回数据失败'));
          }
        },
        fail: function(err) {
          reject(err);
        }
      });
    });
  },

  // 调用后端AI诊断接口
  callAIDiagnoseAPI: function(imageUrl) {
    return new Promise((resolve, reject) => {
      const cropType = this.data.cropTypes[this.data.cropTypeIndex];
      
      // 获取用户ID，优先从全局数据获取，然后从本地存储获取
      const app = getApp();
      let userId = app.globalData.userId;
      if (!userId) {
        const userInfo = wx.getStorageSync('userInfo');
        userId = userInfo ? userInfo.userId : null;
      }
      
      wx.request({
        url: 'http://localhost:8082/api/ai/diagnose',
        method: 'POST',
        header: {
          'Content-Type': 'application/json'
        },
        data: {
          imageUrl: imageUrl,
          cropType: cropType,
          userId: userId
        },
        success: function(res) {
          console.log('AI诊断接口返回:', res.data);
          if (res.data && res.data.code === 200) {
            resolve(res.data.data);
          } else {
            reject(new Error(res.data.message || '诊断失败'));
          }
        },
        fail: function(err) {
          reject(err);
        }
      });
    });
  },

  // 跳转到发布需求页面
  navigateToPublishDemand: function() {
    const detectionResult = this.data.detectionResult;
    const cropType = this.data.cropTypes[this.data.cropTypeIndex];
    
    // 构建跳转参数
    const params = {
      cropType: cropType,
      pestType: detectionResult.diseaseName || '',
      specialRequirements: detectionResult.recommendedPesticide ? `推荐药剂：${detectionResult.recommendedPesticide}` : ''
    };
    
    // 将参数转换为URL格式
    const queryString = Object.keys(params)
      .map(key => `${key}=${encodeURIComponent(params[key])}`)
      .join('&');
    
    wx.navigateTo({
      url: `/pages/farmer/publish-spray-demand?${queryString}`
    });
  }
})