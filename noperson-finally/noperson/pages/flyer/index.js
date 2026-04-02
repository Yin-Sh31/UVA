// flyer/index.js
const sprayDemandAPI = require('../../utils/api/spray-demand-api');
const { api } = require('../../utils/request');
const { getBanners } = require('../../utils/services/banner');

Page({
  data: {
    // 轮播图数据 - 初始为空，确保完全使用API返回的数据
    bannerImages: [],
    // 临时存储默认轮播图，仅在API调用失败时使用
    defaultBanners: [
      { id: '1', title: '轮播图1', imageUrl: '/assets/images/1.jpg', targetUrl: '' },
      { id: '2', title: '轮播图2', imageUrl: '/assets/images/2.jpg', targetUrl: '' }
    ],
    // 多个信息列表
    featuredList: [],
    // 加载状态
    loading: false,
    // 轮播图加载时间
    bannerLoadTime: 0,
    // 未读消息数
    unreadCount: 0
  },
  
  onLoad() {
    console.log('========== 飞手端页面加载 ==========');
    console.log('页面加载，初始化轮播图');
    // 加载轮播图
    this.fetchBanners();
    // 加载推荐信息
    this.loadFeaturedData();
    // 获取未读消息数
    this.loadUnreadCount();
  },

  onShow() {
    // 页面显示时刷新未读消息数
    this.loadUnreadCount();
  },
  
  onPullDownRefresh() {
    console.log('========== 飞手端下拉刷新 ==========');
    // 重新获取轮播图数据
    this.fetchBanners().then(() => {
      // 停止下拉刷新动画
      wx.stopPullDownRefresh();
      console.log('========== 飞手端刷新完成 ==========');
    });
  },
  
  /**
   * 获取轮播图数据 - 优先使用API返回数据
   */
  async fetchBanners() {
    const startTime = Date.now();
    console.log('【飞手端】1. 开始获取轮播图数据');
    console.log('【飞手端】2. 准备调用banner服务');
    
    try {
      // 引入banner服务
      console.log('【飞手端】3. 引入banner服务模块');
      const { getBanners } = require('../../utils/services/banner');
      
      // 设置加载状态
      this.setData({ loading: true });
      console.log('【飞手端】4. 调用getBanners服务，type=flyer');
      
      // 调用API并记录详细信息
      let banners = await getBanners('flyer');
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      console.log('【飞手端】5. API调用完成，耗时:', loadTime, 'ms');
      console.log('【飞手端】6. API返回原始数据类型:', typeof banners, '数据:', JSON.stringify(banners));
      
      // 安全处理：确保banners是数组
      if (!Array.isArray(banners)) {
        console.error('【飞手端】7. 错误：返回的数据不是数组类型');
        banners = [];
      }
      
      // 安全处理：过滤确保数据有效性
      const safeBanners = banners.filter(item => item && typeof item === 'object' && item.imageUrl);
      console.log(`【飞手端】8. 安全过滤后轮播图数量：${safeBanners.length}`);
      
      // 只有当API返回的数据存在且长度大于0时，才使用API返回的数据
      if (safeBanners.length > 0) {
        console.log(`【飞手端】9. 成功获取到${safeBanners.length}张有效轮播图`);
        
        // 处理轮播图数据，确保字段完整
        const processedBanners = safeBanners.map((banner, index) => {
          // 确保图片URL正确拼接后端服务器地址
          const processedImageUrl = banner.imageUrl ? 
            (banner.imageUrl.startsWith('http') ? banner.imageUrl : 'http://localhost:8082' + banner.imageUrl) : 
            '';
          
          return {
            id: banner.id || `banner_${index}`,
            title: banner.title || `轮播图${index + 1}`,
            targetUrl: banner.targetUrl || banner.link || '',
            // 确保图片URL正确拼接后端服务器地址
            imageUrl: processedImageUrl,
            // 兼容现有代码使用的字段
            url: processedImageUrl,
            link: banner.targetUrl || banner.link || '',
            // 添加标记避免undefined问题
            __valid: true
          };
        });
        
        console.log('【飞手端】10. 处理后的轮播图数据:', JSON.stringify(processedBanners));
        
        this.setData({
          bannerImages: processedBanners,
          loading: false,
          bannerLoadTime: loadTime
        });
      } else {
        // 否则使用默认的轮播图
        console.log('【飞手端】9. API未返回有效轮播图，使用默认轮播图');
        // 确保默认数据也是安全的
        const safeDefaultBanners = this.getSafeBanners(this.data.defaultBanners);
        this.setData({
          bannerImages: safeDefaultBanners,
          loading: false,
          bannerLoadTime: loadTime
        });
      }
      
      console.log('【飞手端】11. 轮播图数据设置完成');
      return true;
    } catch (error) {
      const endTime = Date.now();
      const loadTime = endTime - startTime;
      
      console.error('【飞手端】错误: 获取轮播图失败:', error);
      console.error('【飞手端】错误详情:', error.message || error);
      console.error('【飞手端】错误堆栈:', error.stack);
      
      // 发生错误时使用默认轮播图，并确保数据安全
      try {
        console.log('【飞手端】错误: API调用异常，使用安全处理的默认轮播图');
        const safeDefaultBanners = this.getSafeBanners(this.data.defaultBanners);
        this.setData({
          bannerImages: safeDefaultBanners,
          loading: false,
          bannerLoadTime: loadTime
        });
      } catch (setDataError) {
        console.error('【飞手端】错误: 设置默认数据失败:', setDataError);
        // 最后防线：使用空数组防止渲染错误
        this.setData({
          bannerImages: [],
          loading: false,
          bannerLoadTime: loadTime
        });
      }
      
      // 显示错误提示
      wx.showToast({
        title: '轮播图加载失败',
        icon: 'none',
        duration: 2000
      });
      
      return false;
    } finally {
      // 停止下拉刷新动画，添加错误处理
      try {
        wx.stopPullDownRefresh();
      } catch (e) {
        console.error('【飞手端】停止下拉刷新失败:', e);
      }
    }
  },

  // 安全处理轮播图数据的辅助方法
  getSafeBanners: function(banners) {
    if (!Array.isArray(banners)) {
      console.error('【飞手端】错误: banners参数不是数组');
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
   * 轮播图点击事件处理
   */
  onBannerClick: function(e) {
    const { index } = e.currentTarget.dataset;
    const banner = this.data.bannerImages[index];
    
    // 可以根据banner中的targetUrl或其他信息进行页面跳转
    if (banner.targetUrl || banner.link) {
      wx.navigateTo({
        url: banner.targetUrl || banner.link
      });
    }
  },
  
  // 轮播图点击处理
  bannerItemClick: function(e) {
    const link = e.currentTarget.dataset.link;
    if (link) {
      // 处理轮播图点击事件，可以是跳转页面或其他操作
      if (link.startsWith('/pages/')) {
        wx.navigateTo({ url: link });
      } else if (link.startsWith('http')) {
        wx.navigateTo({ url: `/pages/web-view/index?url=${encodeURIComponent(link)}` });
      }
    }
  },
  
  // 从API加载推荐数据
  loadFeaturedData() {
    console.log('========== 开始加载推荐数据 ==========');
    this.setData({ loading: true });
    
    // 并行获取需求数据和农户数据
    Promise.all([
      // 获取喷洒需求
      sprayDemandAPI.getSprayOnlyDemands({
        pageNum: 1,
        pageSize: 3,
        status: '0' // 待接取状态
      }),
      // 获取巡检需求
      sprayDemandAPI.getInspectionOnlyDemands({
        pageNum: 1,
        pageSize: 2,
        status: '0' // 待接取状态
      }),
      // 获取真实农户数据
      sprayDemandAPI.getFarmersList({
        pageNum: 1,
        pageSize: 20
      })
    ]).then(([sprayResults, inspectionResults, farmerResults]) => {
      console.log('推荐数据加载完成，开始处理数据');
      console.log('喷洒需求数据:', sprayResults);
      console.log('巡检需求数据:', inspectionResults);
      console.log('农户数据:', farmerResults);
      
      const featuredList = [];
      
      // 处理喷洒需求数据
      if (sprayResults && sprayResults.records && sprayResults.records.length > 0) {
        console.log('处理喷洒需求数据，数量:', sprayResults.records.length);
        sprayResults.records.forEach(record => {
          featuredList.push({
            id: record.id,
            type: 'demand',
            subtype: 'spray',
            location: record.location || '未知位置',
            area: record.area || 0,
            price: record.price || 0,
            status: '待接单',
            time: this.formatTime(record.createTime || new Date())
          });
        });
      } else {
        console.log('没有喷洒需求数据');
      }
      
      // 处理巡检需求数据
      if (inspectionResults && inspectionResults.records && inspectionResults.records.length > 0) {
        console.log('处理巡检需求数据，数量:', inspectionResults.records.length);
        inspectionResults.records.forEach(record => {
          featuredList.push({
            id: record.id,
            type: 'demand',
            subtype: 'inspection',
            location: record.location || '未知位置',
            area: record.area || 0,
            price: record.price || 0,
            status: '待接单',
            time: this.formatTime(record.createTime || new Date())
          });
        });
      } else {
        console.log('没有巡检需求数据');
      }
      
      // 处理真实农户数据
      let farmersList = [];
      if (farmerResults && farmerResults.data && Array.isArray(farmerResults.data)) {
        // 如果返回的是Result对象包装的数组
        farmersList = farmerResults.data;
        console.log('农户数据类型：Result对象包装的数组，记录数:', farmersList.length);
      } else if (farmerResults && farmerResults.records && Array.isArray(farmerResults.records)) {
        // 如果返回的是分页对象，取records数组
        farmersList = farmerResults.records;
        console.log('农户数据类型：分页对象，记录数:', farmersList.length);
      } else if (farmerResults && Array.isArray(farmerResults)) {
        // 如果直接返回数组
        farmersList = farmerResults;
        console.log('农户数据类型：直接数组，记录数:', farmersList.length);
      } else {
        console.log('农户数据格式不符合预期:', farmerResults);
      }
      
      // 随机选择4个真实农户数据
      if (farmersList && farmersList.length > 0) {
        console.log('开始处理真实农户数据，总数:', farmersList.length);
        const shuffledFarmers = [...farmersList].sort(() => Math.random() - 0.5);
        const selectedFarmers = shuffledFarmers.slice(0, 4);
        console.log('随机选择的农户数量:', selectedFarmers.length);
        console.log('随机选择的农户数据:', selectedFarmers);
        
        selectedFarmers.forEach((farmer, index) => {
          console.log('处理农户:', farmer);
          // 生成随机的农场类型和种植面积
          const farmTypes = ['粮食种植', '蔬菜种植', '水果种植', '花卉种植', '多种种植'];
          const farmType = farmTypes[Math.floor(Math.random() * farmTypes.length)];
          const farmArea = Math.floor(Math.random() * 200) + 50; // 50-250亩
          const completedOrders = Math.floor(Math.random() * 50) + 5; // 5-55个订单
          
          featuredList.push({
            id: farmer.userId || 'farmer_' + index,
            type: 'farmer',
            name: farmer.realName || farmer.username || '农户用户',
            farmType: farmType,
            location: farmer.location || '本地区域',
            farmArea: farmArea,
            completedOrders: completedOrders,
            avatar: farmer.avatar
          });
        });
      } else {
        console.log('没有获取到真实农户数据');
      }
      
      console.log('合并后的推荐列表数量:', featuredList.length);
      
      // 如果没有足够的数据，添加模拟的农户信息作为补充
      if (featuredList.length < 4) {
        const neededFarmers = 4 - featuredList.length;
        console.log('需要补充的模拟农户数量:', neededFarmers);
        for (let i = 0; i < neededFarmers; i++) {
          featuredList.push({
            id: 'farmer_' + i,
            type: 'farmer',
            name: '附近农户' + i,
            farmType: '多种种植',
            location: '本地区域',
            farmArea: 100 + i * 50,
            completedOrders: 20 + i * 10,
            avatar: ''
          });
        }
      }
      
      // 随机排序
      console.log('最终推荐列表:', featuredList);
      this.setData({
        featuredList: this.shuffleArray(featuredList),
        loading: false
      });
      console.log('========== 推荐数据加载完成 ==========');
    }).catch(error => {
      console.error('加载推荐数据失败:', error);
      console.error('错误详情:', error.message || error);
      this.setData({ loading: false });
      
      // 错误处理：显示空状态或提示
      wx.showToast({
        title: '数据加载失败',
        icon: 'none'
      });
    });
  },
  
  // 打乱数组顺序
  shuffleArray(array) {
    const newArray = [...array];
    for (let i = newArray.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [newArray[i], newArray[j]] = [newArray[j], newArray[i]];
    }
    return newArray;
  },
  
  // 格式化时间
  formatTime(time) {
    const date = new Date(time);
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    
    return `${month}-${day} ${hours}:${minutes}`;
  },
  
  // 刷新推荐信息
  refreshFeatured() {
    wx.showToast({
      title: '刷新中...',
      icon: 'loading',
      duration: 500
    });
    
    this.loadFeaturedData();
    
    setTimeout(() => {
      wx.showToast({
        title: '刷新成功',
        icon: 'success',
        duration: 1000
      });
    }, 500);
  },
  
  // 查看所有需求
  viewAllDemands() {
    console.log('尝试跳转到需求筛选页面');
    
    // 由于accept-order是tabBar页面，需要使用wx.switchTab跳转
    // 先将type参数存储到本地存储中
    wx.setStorageSync('acceptOrderType', 'all');
    
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

  // 查看巡检需求
  viewInspectionDemands() {
    // 由于accept-order是tabBar页面，需要使用wx.switchTab跳转
    // 先将type参数存储到本地存储中
    wx.setStorageSync('acceptOrderType', 'inspection');
    
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

  // 查看喷洒需求
  viewSprayDemands() {
    // 由于accept-order是tabBar页面，需要使用wx.switchTab跳转
    // 先将type参数存储到本地存储中
    wx.setStorageSync('acceptOrderType', 'spray');
    
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

  // 资质管理
  viewQualification() {
    wx.navigateTo({
      url: '/pages/flyer/qualification/index'
    });
  },

  // 上传巡检报告
  uploadInspection() {
    wx.navigateTo({
      url: '/pages/flyer/upload-inspection/index'
    });
  },

  // 设备归还
  viewDeviceReturn() {
    console.log('点击了设备归还按钮');
    wx.navigateTo({
      url: '/pages/flyer/device-return/index',
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

  // 查看所有农户
  viewAllFarmers() {
    console.log('点击了所有农户按钮');
    wx.navigateTo({
      url: '/pages/flyer/all-farmers/index',
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

  // 查看设备大厅
  viewEquipmentHall() {
    console.log('点击了设备大厅按钮');
    wx.navigateTo({
      url: '/pages/flyer/equipment/index',
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

  // 查看需求详情
  viewDemandDetail(e) {
    const demandId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/flyer/demand-detail?id=${demandId}`,
      fail: function(err) {
        console.error('跳转失败', err);
        wx.showToast({
          title: '页面不存在',
          icon: 'none'
        });
      }
    });
  },

  // 跳转到消息列表
  goToMessageList() {
    wx.navigateTo({
      url: '/pages/flyer/chat/list',
      success: function(res) {
        console.log('跳转到消息列表成功', res);
      },
      fail: function(err) {
        console.error('跳转失败', err);
        wx.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  },

  // 获取未读消息数
  async loadUnreadCount() {
    try {
      const userInfo = wx.getStorageSync('userInfo');
      const userId = userInfo?.id;
      if (!userId) return;
      
      const res = await api.getChatUnreadCount(userId);
      if (res.data && res.data.count !== undefined) {
        this.setData({
          unreadCount: res.data.count
        });
      }
    } catch (error) {
      console.error('获取未读消息数失败:', error);
    }
  }
});
