// farmer/demand-detail.js
const { api } = require('../../utils/request');

Page({
  data: {
    demandDetail: null,
    loading: true,
    error: false,
    errorMsg: '', // 错误信息
    refreshing: false, // 下拉刷新状态
    lastUpdateTime: 0, // 上次更新时间，用于缓存控制
    cachedDetail: null, // 缓存的详情数据
    // 反馈表单相关
    showFeedbackForm: false,
    feedbackContent: '',
    feedbackImages: [],
    maxImages: 5,
    // 评价表单相关
    showEvaluationForm: false,
    evaluation: {
      scoreQuality: 5,
      scorePunctuality: 5,
      scoreAttitude: 5,
      scoreEfficiency: 5,
      comment: ''
    },
    hasEvaluated: false
  },

  onLoad: function(options) {
    // 简化参数获取，更高效地获取需求ID
    const demandId = options.id || options['id'];
    
    if (!demandId) {
      // 尝试从全局获取临时保存的ID
      const app = getApp();
      const globalTempId = app.globalData?.tempDemandId;
      
      if (!globalTempId) {
        wx.showToast({
          title: '需求ID不存在',
          icon: 'none'
        });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
        return;
      }
      this.demandId = globalTempId;
    } else {
      // 保存ID到实例和全局（作为备选）
      this.demandId = demandId;
      const app = getApp();
      if (!app.globalData) app.globalData = {};
      app.globalData.tempDemandId = demandId;
    }
    
    // 先尝试加载缓存数据，提升用户体验
    this.loadCachedData();
    // 然后加载最新数据
    this.loadDemandDetail();
  },

  // 加载缓存数据
  loadCachedData: function() {
    try {
      const cacheKey = `demand_detail_${this.demandId}`;
      const cachedData = wx.getStorageSync(cacheKey);
      const cacheTime = wx.getStorageSync(`${cacheKey}_time`);
      
      // 如果缓存存在且未过期（5分钟内）
      if (cachedData && cacheTime && (Date.now() - cacheTime < 5 * 60 * 1000)) {
        this.setData({
          cachedDetail: cachedData,
          loading: false // 显示缓存数据时不显示加载状态
        });
      }
    } catch (error) {
      console.error('加载缓存数据失败', error);
    }
  },
  
  // 保存数据到缓存
  saveToCache: function(data) {
    try {
      const cacheKey = `demand_detail_${this.demandId}`;
      wx.setStorageSync(cacheKey, data);
      wx.setStorageSync(`${cacheKey}_time`, Date.now());
    } catch (error) {
      console.error('保存数据到缓存失败', error);
    }
  },
  
  // 加载需求详情
  loadDemandDetail: async function() {
    this.setData({
      loading: !this.data.cachedDetail, // 如果有缓存，不显示加载状态
      error: false,
      errorMsg: ''
    });
    
    try {
      if (!this.demandId) {
        throw new Error('需求ID不存在');
      }
      
      // 设置请求超时
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('请求超时，请检查网络连接')), 10000);
      });
      
      // 竞争请求和超时
      const demandDetailData = await Promise.race([
        api.getDemandDetail(this.demandId),
        timeoutPromise
      ]);
      
      if (!demandDetailData) {
        throw new Error('暂无需求数据');
      }
      
      // 从响应对象中获取data字段
      const formattedData = this.formatDemandDetail(demandDetailData.data);
      
      this.setData({
        demandDetail: formattedData,
        loading: false,
        cachedDetail: null, // 加载到新数据后不再显示缓存
        lastUpdateTime: Date.now()
      });
      
      // 保存到缓存
      this.saveToCache(formattedData);
      
      // 检查是否已评价
      this.checkEvaluationStatus();
    } catch (error) {
      console.error('加载需求详情失败', error);
      const errorMsg = error.message || '加载失败，请重试';
      
      this.setData({
        error: true,
        loading: false,
        errorMsg: errorMsg
      });
      
      // 如果没有缓存数据，才显示错误提示
      if (!this.data.cachedDetail) {
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        });
      }
    }
  },

  // 格式化需求详情数据 - 处理符合数据库表结构的数据
  formatDemandDetail: function(data) {
    // 根据orderType区分订单类型
    const orderType = data.orderType || 1; // 默认喷洒订单
    const isInspection = orderType === 2;
    
    // 确保返回的数据结构完整，与数据库字段保持一致
    return {
      id: data.demand_id || data.id || '', // 对应数据库的demand_id
      orderType: orderType,
      type: isInspection ? '巡检订单' : '喷洒需求',
      typeDesc: data.orderTypeDesc || (isInspection ? '巡检' : '喷洒'),
      status: data.statusDesc || this.getStatusText(data.status),
      statusCode: data.status || 0, // 保存状态码用于逻辑判断
      statusClass: `status-${data.status}`,
      createTime: this.formatDateTime(data.create_time || data.createTime) || '', // 对应数据库的create_time
      fieldLocation: data.land_location || data.landName || '未知地点', // 对应数据库的land_location
      landName: data.land_name || data.landName || '未知地块', // 对应数据库的land_name
      area: data.land_area || data.landArea || 0, // 对应数据库的land_area
      budget: data.budget || 0,
      expectedTime: this.formatDateTime(data.expected_time || data.expectedTime) || '', // 对应数据库的expected_time
      cropType: data.crop_type || data.cropType || '', // 对应数据库的crop_type
      pestType: data.pest_type || data.pestType || '', // 对应数据库的pest_type
      inspectionPurpose: data.inspection_purpose || data.inspectionPurpose || '', // 对应数据库的inspection_purpose
      expectedResolution: data.expected_resolution || data.expectedResolution || '', // 对应数据库的expected_resolution
      acceptTime: this.formatDateTime(data.accept_time || data.acceptTime) || '', // 对应数据库的accept_time
      notes: data.specialRequirements || '无备注信息',
      // 飞手信息（如果已接单）
      flyer_id: data.flyer_id || data.flyerId, // 保留飞手ID用于评价
      flyerInfo: data.flyer_id ? {
        name: data.flyerName || '未知飞手',
        avatar: '/assets/images/user-avatar.png',
        phone: '',
        creditScore: 0,
        skillLevel: '初级'
      } : null,
      // 设备信息
      deviceInfo: data.device_id ? {} : null, // 对应数据库的device_id
      // 支付状态
      paymentStatus: this.getPaymentStatusText(data.payment_status || data.paymentStatus || 0), // 对应数据库的payment_status
      paymentStatusValue: data.payment_status || data.paymentStatus || 0,
      paymentAmount: data.payment_amount || 0, // 对应数据库的payment_amount
      completionTime: this.formatDateTime(data.complete_time || data.completionTime) || '', // 对应数据库的complete_time
      // 巡检报告相关
      reportId: data.report_id || data.reportId || null, // 对应数据库的report_id
      inspectionReport: data.report_id ? {
        id: data.report_id || '',
        content: '',
        images: []
      } : null
    };
  },

  // 格式化日期时间
  formatDateTime: function(dateTime) {
    if (!dateTime) return '';
    
    // 如果已经是格式化好的字符串，直接返回
    if (typeof dateTime === 'string' && dateTime.includes('-')) {
      return dateTime;
    }
    
    try {
      const date = new Date(dateTime);
      if (isNaN(date.getTime())) return '';
      
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    } catch (error) {
      console.error('日期格式化失败', error);
      return dateTime.toString();
    }
  },

  // 获取状态文本
  getStatusText: function(status) {
    const statusMap = {
      0: '待接取',
      1: '处理中',
      2: '作业中',
      3: '待确认',
      4: '已完成',
      5: '已取消'
    };
    return statusMap[status] || '待接取'; // 默认为待接取状态，而不是未知
  },

  // 获取支付状态文本
  getPaymentStatusText: function(status) {
    const statusMap = {
      0: '未支付',
      1: '已支付',
      2: '已退款'
    };
    return statusMap[status] || '未知';
  },

  // 返回上一页
  onBackPress: function() {
    wx.navigateBack();
  },

  // 刷新页面
  refreshPage: function() {
    // 清除缓存，强制获取最新数据
    try {
      const cacheKey = `demand_detail_${this.demandId}`;
      wx.removeStorageSync(cacheKey);
      wx.removeStorageSync(`${cacheKey}_time`);
      this.setData({ cachedDetail: null });
    } catch (error) {
      console.error('清除缓存失败', error);
    }
    
    this.loadDemandDetail();
  },

  // 取消需求
  cancelDemand: function() {
    wx.showModal({
      title: '取消需求',
      content: '确定要取消该需求吗？取消后将无法恢复。',
      confirmColor: '#ff4d4f',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '处理中...',
          });
          
          try {
            await api.cancelDemand(this.demandId);
            
            wx.hideLoading();
            wx.showToast({
              title: '取消成功',
              icon: 'success',
              duration: 2000
            });
            
            // 先更新UI，提升用户体验
            const updatedDetail = { ...this.data.demandDetail, status: '已取消', statusCode: 5 };
            this.setData({ demandDetail: updatedDetail });
            
            // 重新加载需求详情，确保数据准确性
            setTimeout(() => {
              this.loadDemandDetail();
            }, 1000);
            
            // 清除缓存
            this.saveToCache(updatedDetail);
          } catch (error) {
            wx.hideLoading();
            console.error('取消需求失败', error);
            wx.showToast({
              title: error.message || '取消失败，请重试',
              icon: 'none'
            });
          }
        }
      }
});
  },

  // 联系飞手
  contactFlyer: function() {
    const flyerPhone = this.data.demandDetail?.flyerInfo?.phone;
    
    if (flyerPhone) {
      wx.showActionSheet({
        itemList: ['拨打电话', '复制号码'],
        success: (res) => {
          if (res.tapIndex === 0) {
            // 拨打电话
            wx.makePhoneCall({
              phoneNumber: flyerPhone,
              fail: (err) => {
                console.error('拨打电话失败', err);
                wx.showToast({
                  title: '拨打电话失败',
                  icon: 'none'
                });
              }
            });
          } else if (res.tapIndex === 1) {
            // 复制号码
            wx.setClipboardData({
              data: flyerPhone,
              success: () => {
                wx.showToast({
                  title: '号码已复制',
                  icon: 'success',
                  duration: 1500
                });
              }
            });
          }
        }
      });
    } else {
      wx.showToast({
        title: '暂无飞手联系方式',
        icon: 'none'
      });
    }
  },

  // 查看报告
  viewReport: function() {
    if (this.data.demandDetail?.reportId) {
      wx.navigateTo({
        url: `/pages/farmer/view-report?id=${this.data.demandDetail.reportId}`
      });
    } else {
      wx.showToast({
        title: '暂无报告',
        icon: 'none'
      });
    }
  },

  // 支付需求费用
  payDemand: function() {
    // 获取需求费用、ID和需求状态
    const demandDetail = this.data.demandDetail || this.data.cachedDetail;
    const demandCost = demandDetail?.budget || 0;
    const demandId = demandDetail?.id || this.demandId;
    const demandStatus = demandDetail?.status || '';
    const demandStatusCode = demandDetail?.statusCode || null;
    
    if (!demandId) {
      wx.showToast({
        title: '无法获取需求ID',
        icon: 'none'
      });
      return;
    }
    
    // 检查需求状态，只有待接取状态(0)才能发起支付
    if (demandStatusCode !== 0 && demandStatus !== '待接取') {
      // 根据不同的需求状态显示不同的提示信息
      let statusText = '';
      switch(demandStatusCode) {
        case 1:
          statusText = '处理中（已支付）';
          break;
        case 2:
          statusText = '作业中（已支付）';
          break;
        case 3:
          statusText = '待确认（已支付）';
          break;
        case 4:
          statusText = '已完成（已支付）';
          break;
        case 5:
          statusText = '已取消（已支付）';
          break;
        default:
          statusText = demandStatus || '非待接取状态';
      }
      wx.showToast({
        title: `需求已${statusText}，无需支付`,
        icon: 'none'
      });
      return;
    }
    
    // 先显示确认框
    wx.showModal({
      title: '支付需求费用',
      content: `确定要支付该需求的费用吗？金额为 ¥${demandCost}`,
      confirmColor: '#1890ff',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '处理中...',
          });
          
          try {
            // 设置支付请求超时
            const timeoutPromise = new Promise((_, reject) => {
              setTimeout(() => reject(new Error('支付处理超时，请稍后重试')), 15000);
            });
            
            // 竞争请求和超时
            await Promise.race([
              api.payDemandFee(demandId),
              timeoutPromise
            ]);
            
            wx.hideLoading();
            
            // 支付成功
            wx.showToast({
              title: '支付成功',
              icon: 'success',
              duration: 2000
            });
            
            // 先更新UI，提升用户体验
            const updatedDetail = { ...demandDetail, paymentStatus: '已支付', paymentStatusValue: 1 };
            this.setData({ 
              demandDetail: updatedDetail,
              cachedDetail: null 
            });
            
            // 重新加载需求详情，确保数据准确性
            setTimeout(() => {
              this.loadDemandDetail();
              
              // 触发余额更新
              if (typeof this.onBalanceUpdate === 'function') {
                this.onBalanceUpdate();
              }
            }, 1000);
            
            // 更新缓存
            this.saveToCache(updatedDetail);
          } catch (error) {
            wx.hideLoading();
            console.error('支付失败', error);
            
            // 特殊错误处理
            if (error.message && error.message.includes('余额不足')) {
              // 余额不足，显示充值提示
              wx.showModal({
                title: '支付失败',
                content: error.message + '，是否前往充值？',
                confirmText: '去充值',
                cancelText: '取消',
                success: (modalRes) => {
                  if (modalRes.confirm) {
                    // 跳转到充值页面
                    wx.navigateTo({
                      url: '/pages/payment/wallet'
                    });
                  }
                }
              });
            } else if (error.message && error.message.includes('无权操作')) {
              // 无权操作提示
              wx.showModal({
                title: '支付失败',
                content: '您可能没有权限操作该需求，请确认您是需求创建者或联系客服。',
                showCancel: false
              });
            } else {
              // 其他错误
              wx.showToast({
                title: error.message || '支付失败，请重试',
                icon: 'none'
              });
            }
          }
        }
      }
    });
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    this.setData({ refreshing: true });
    
    // 清除缓存，强制获取最新数据
    try {
      const cacheKey = `demand_detail_${this.demandId}`;
      wx.removeStorageSync(cacheKey);
      wx.removeStorageSync(`${cacheKey}_time`);
      this.setData({ cachedDetail: null });
    } catch (error) {
      console.error('清除缓存失败', error);
    }
    
    this.loadDemandDetail().finally(() => {
      // 无论成功失败，都停止下拉刷新
      wx.stopPullDownRefresh();
      this.setData({ refreshing: false });
    });
  },
  
  // 反馈需求完成度
  feedbackDemand: function() {
    // 显示反馈表单
    this.setData({
      showFeedbackForm: true,
      feedbackContent: '',
      feedbackImages: []
    });
  },

  // 关闭反馈表单
  closeFeedbackForm: function() {
    this.setData({
      showFeedbackForm: false
    });
  },

  // 选择图片
  chooseImage: function() {
    const currentCount = this.data.feedbackImages.length;
    const remainingCount = this.data.maxImages - currentCount;
    
    if (remainingCount <= 0) {
      wx.showToast({
        title: `最多只能上传${this.data.maxImages}张图片`,
        icon: 'none'
      });
      return;
    }
    
    console.log('开始选择图片，剩余可上传数量:', remainingCount);
    
    wx.chooseImage({
      count: remainingCount,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        console.log('选择图片成功，选择的图片数量:', res.tempFilePaths.length);
        console.log('选择的图片路径:', res.tempFilePaths);
        this.uploadImages(res.tempFilePaths);
      },
      fail: (err) => {
        console.error('选择图片失败:', err);
        wx.showToast({
          title: `选择图片失败: ${err.errMsg || '未知错误'}`,
          icon: 'none',
          duration: 3000
        });
      }
    });
  },
  
  // 上传图片到服务器
  uploadImages: function(tempFilePaths) {
    wx.showLoading({
      title: '上传图片中...'
    });
    
    console.log('开始上传图片，上传数量:', tempFilePaths.length);
    console.log('上传文件路径:', tempFilePaths);
    
    const uploadPromises = tempFilePaths.map((filePath, index) => {
      return new Promise((resolve, reject) => {
        console.log(`开始上传第${index + 1}张图片，路径:`, filePath);
        
        wx.uploadFile({
          url: 'http://localhost:8082/upload/image',
          filePath: filePath,
          name: 'file',
          success: (res) => {
            console.log(`第${index + 1}张图片上传响应:`, res);
            console.log(`响应状态码:`, res.statusCode);
            console.log(`响应数据:`, res.data);
            
            try {
              const result = JSON.parse(res.data);
              console.log(`解析后的响应:`, result);
              
              if (result.code === 200) {
                // 兼容两种响应格式：URL可能在data字段或message字段中
                const imageUrl = result.data || result.message;
                console.log(`第${index + 1}张图片上传成功，返回URL:`, imageUrl);
                resolve(imageUrl);
              } else {
                console.error(`第${index + 1}张图片上传失败，服务器返回错误:`, result.message);
                reject(new Error(result.message || '上传失败'));
              }
            } catch (e) {
              console.error(`第${index + 1}张图片上传响应解析失败:`, e);
              reject(new Error('解析响应失败'));
            }
          },
          fail: (err) => {
            console.error(`第${index + 1}张图片上传失败:`, err);
            reject(err);
          }
        });
      });
    });
    
    Promise.all(uploadPromises)
      .then(urls => {
        wx.hideLoading();
        console.log('所有图片上传成功，返回的URL列表:', urls);
        
        const updatedImages = [...this.data.feedbackImages, ...urls];
        this.setData({
          feedbackImages: updatedImages
        });
        
        console.log('更新后的图片列表:', updatedImages);
        wx.showToast({
          title: `成功上传${urls.length}张图片`,
          icon: 'success'
        });
      })
      .catch(err => {
        wx.hideLoading();
        console.error('图片上传失败:', err);
        wx.showToast({
          title: `图片上传失败: ${err.message || '未知错误'}`,
          icon: 'none',
          duration: 3000
        });
      });
  },

  // 删除图片
  deleteImage: function(e) {
    const index = e.currentTarget.dataset.index;
    const images = [...this.data.feedbackImages];
    images.splice(index, 1);
    
    this.setData({
      feedbackImages: images
    });
  },

  // 预览图片
  previewImage: function(e) {
    const current = e.currentTarget.dataset.url;
    wx.previewImage({
      current: current,
      urls: this.data.feedbackImages
    });
  },

  // 输入反馈内容
  inputFeedbackContent: function(e) {
    this.setData({
      feedbackContent: e.detail.value
    });
  },

  // 提交反馈
  submitFeedback: async function() {
    const content = this.data.feedbackContent.trim();
    const images = this.data.feedbackImages;
    
    if (!content) {
      wx.showToast({
        title: '请填写反馈内容',
        icon: 'none'
      });
      return;
    }
    
    wx.showModal({
      title: '提交反馈',
      content: '确定要提交反馈吗？提交后将发送给管理员审核。',
      confirmColor: '#1890ff',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '提交反馈中...',
          });
          
          try {
            // 调用API提交反馈
            await api.submitDemandFeedback(this.demandId, {
              feedbackType: 'incomplete',
              feedbackContent: content,
              feedbackImages: images.join(','),
              demandId: this.demandId,
              userId: wx.getStorageSync('userInfo')?.id
            });
            
            wx.hideLoading();
            wx.showToast({
              title: '反馈提交成功',
              icon: 'success',
              duration: 2000
            });
            
            // 关闭表单
            setTimeout(() => {
              this.closeFeedbackForm();
            }, 1500);
          } catch (error) {
            wx.hideLoading();
            console.error('提交反馈失败', error);
            wx.showToast({
              title: error.message || '提交反馈失败，请重试',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  // 致电管理员
  callAdmin: function() {
    const adminPhone = '13800138000'; // 管理员电话号码
    wx.makePhoneCall({
      phoneNumber: adminPhone,
      success: () => {
        console.log('拨打电话成功');
      },
      fail: (err) => {
        console.error('拨打电话失败', err);
        wx.showToast({
          title: '拨打电话失败',
          icon: 'none'
        });
      }
    });
  },

  // 检查评价状态
  checkEvaluationStatus: function() {
    const demandId = this.demandId;
    const userId = wx.getStorageSync('userInfo')?.id;
    
    if (demandId && userId) {
      api.checkFlyerEvaluationStatus(demandId, userId)
        .then(response => {
          if (response && response.code === 200) {
            this.setData({
              hasEvaluated: response.data
            });
          }
        })
        .catch(error => {
          console.error('检查评价状态失败', error);
        });
    }
  },

  // 显示评价表单
  showEvaluation: function() {
    this.setData({
      showEvaluationForm: true,
      evaluation: {
        scoreQuality: 5,
        scorePunctuality: 5,
        scoreAttitude: 5,
        scoreEfficiency: 5,
        comment: ''
      }
    });
  },

  // 关闭评价表单
  closeEvaluationForm: function() {
    this.setData({
      showEvaluationForm: false
    });
  },

  // 选择评分
  selectScore: function(e) {
    const type = e.currentTarget.dataset.type;
    const score = parseInt(e.currentTarget.dataset.score);
    
    const evaluation = { ...this.data.evaluation };
    evaluation[type] = score;
    
    this.setData({
      evaluation
    });
  },

  // 输入评价内容
  inputComment: function(e) {
    const evaluation = { ...this.data.evaluation };
    evaluation.comment = e.detail.value;
    this.setData({
      evaluation
    });
  },

  // 提交评价
  submitEvaluation: function() {
    const evaluation = this.data.evaluation;
    const demandId = this.demandId;
    const userId = wx.getStorageSync('userInfo')?.id;
    const flyerId = this.data.demandDetail?.flyer_id;
    
    if (!flyerId) {
      wx.showToast({
        title: '无法获取飞手信息',
        icon: 'none'
      });
      return;
    }
    
    wx.showModal({
      title: '提交评价',
      content: '确定要提交评价吗？提交后将无法修改。',
      confirmColor: '#1890ff',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '提交评价中...',
          });
          
          api.addFlyerEvaluation({
            orderId: demandId,
            flyerId: flyerId,
            farmerId: userId,
            scoreQuality: evaluation.scoreQuality,
            scorePunctuality: evaluation.scorePunctuality,
            scoreAttitude: evaluation.scoreAttitude,
            scoreEfficiency: evaluation.scoreEfficiency,
            comment: evaluation.comment
          })
            .then(response => {
              wx.hideLoading();
              if (response && response.code === 200) {
                wx.showToast({
                  title: '评价成功',
                  icon: 'success',
                  duration: 2000
                });
                
                this.setData({
                  showEvaluationForm: false,
                  hasEvaluated: true
                });
              } else {
                wx.showToast({
                  title: response?.message || '评价失败',
                  icon: 'none'
                });
              }
            })
            .catch(error => {
              wx.hideLoading();
              console.error('提交评价失败', error);
              wx.showToast({
                title: error.message || '评价失败，请重试',
                icon: 'none'
              });
            });
        }
      }
    });
  },

  // 页面卸载时清理
  onUnload: function() {
    // 可以在这里清理定时器或其他需要释放的资源
    // 注意：小程序页面卸载时会自动清理大部分资源
  }
  }
);