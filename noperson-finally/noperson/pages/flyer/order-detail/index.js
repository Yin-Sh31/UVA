// 飞手的订单详情页面
const sprayDemandAPI = require('../../../utils/api/spray-demand-api');
const { SprayDemandStatusEnum } = require('../../../utils/enum');

Page({
  data: {
    orderId: '',
    orderInfo: {
      id: '',
      status: 0,
      type: 1,
      acceptTime: '',
      area: 0,
      location: '',
      amount: '0.00',
      farmerName: '',
      contactPhone: '',
      notes: ''
    },
    loading: true
  },

  onLoad: function(options) {
    // 从URL参数中获取订单ID，同时支持id和orderId两种参数格式
    const orderId = options.orderId || options.id;
    if (orderId) {
      this.setData({
        orderId: orderId
      });
      
      // 加载订单详情
      this.loadOrderDetail();
    } else {
      // 如果没有订单ID，显示错误提示
      wx.showToast({
        title: '订单ID不存在',
        icon: 'error',
        duration: 2000
      });
      
      this.setData({
        loading: false
      });
    }
  },

  // 加载订单详情
  loadOrderDetail: async function() {
    this.setData({ loading: true });
    
    try {
      console.log('开始加载订单详情，订单ID:', this.data.orderId);
      
      // 获取用户信息，确保传递userId和userRole参数以解决权限问题
      const userInfo = wx.getStorageSync('userInfo') || {};
      const userId = userInfo.id || '';
      
      // 调用更新后的API获取订单详情（使用路径参数），并传递用户信息
      const orderDetail = await sprayDemandAPI.getDemandById({
        demandId: this.data.orderId,
        userId: userId,
        userRole: 'flyer' // 明确指定当前用户角色为飞手
      });
      
      console.log('订单详情加载成功:', orderDetail);
      
      // 处理订单详情数据，确保服务费用正确显示
      // 注意：API返回的数据结构是{code: 200, message: "操作成功", data: {...}}，实际订单数据在data字段中
      const orderData = orderDetail.data || {};
      
      // 使用paymentAmount字段作为服务费用
      const processedOrderInfo = {
        ...orderData,
        // 确保服务费用正确映射，使用data字段中的paymentAmount
        paymentAmount: orderData.paymentAmount || 0,
        // 确保服务面积使用data字段中的landArea
        landArea: orderData.landArea || 0,
        // 保留原有字段的映射，以确保兼容性
        balance: orderData.paymentAmount || orderData.balance || 0,
        amount: (orderData.paymentAmount || orderData.balance || 0).toFixed(2),
        area: orderData.landArea || orderData.area || 0,
        location: orderData.landName || orderData.location || '',
        type: orderData.orderType || orderData.type || 1,
        id: orderData.demandId || orderData.id || '',
        // 确保包含农户信息
        farmerId: orderData.farmerId || orderData.userId || '',
        farmerName: orderData.farmerName || orderData.userName || '农户'
      };
      
      console.log('原始API返回数据:', orderDetail);
      console.log('实际订单数据:', orderData);
      console.log('处理后的订单信息:', processedOrderInfo);
      console.log('服务费用信息 - paymentAmount:', processedOrderInfo.paymentAmount, 'balance:', processedOrderInfo.balance, 'amount:', processedOrderInfo.amount);
      
      this.setData({
        orderInfo: processedOrderInfo,
        loading: false
      });
    } catch (error) {
      console.error('加载订单详情失败', error);
      wx.showToast({
        title: '加载订单详情失败',
        icon: 'none'
      });
      
      // 发生错误时使用模拟数据
      setTimeout(() => {
        const mockOrderInfo = this.generateMockOrderDetail();
        // 确保模拟数据中包含paymentAmount字段
    mockOrderInfo.paymentAmount = mockOrderInfo.paymentAmount || mockOrderInfo.balance || mockOrderInfo.budget || mockOrderInfo.amount || 0;
    mockOrderInfo.landArea = mockOrderInfo.landArea || mockOrderInfo.area || 0;
        
        this.setData({
          orderInfo: mockOrderInfo,
          loading: false
        });
      }, 1000);
    }
  },

  // 生成模拟订单详情数据（API调用失败时使用）
  generateMockOrderDetail: function() {
    // 根据传入的订单ID生成模拟数据
    const orderId = this.data.orderId;
    // 包含已完成状态(4)的状态列表
    const statuses = [
      SprayDemandStatusEnum.PROCESSING.value,
      SprayDemandStatusEnum.IN_OPERATION.value,
      SprayDemandStatusEnum.COMPLETED.value,
      SprayDemandStatusEnum.CANCELLED.value,
      4 // 直接添加已完成状态4
    ];
    const types = [1, 2]; // 1: 喷洒订单, 2: 巡检订单
    
    // 简单的哈希函数，根据订单ID生成相对稳定的随机数
    const hashId = orderId.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
    const randomSeed = hashId % 1000;
    
    const status = statuses[randomSeed % statuses.length];
    const type = types[randomSeed % types.length];
    
    // 生成服务费用，作为paymentAmount的基础
    const paymentAmount = Math.floor(randomSeed / 5) * 10 + 100;
    
    return {
      id: orderId,
      status: status,
      type: type,
      acceptTime: `2023-06-${10 + randomSeed % 20} ${Math.floor(randomSeed / 24)}:${String(randomSeed % 60).padStart(2, '0')}`,
      landArea: Math.floor(randomSeed / 2) + 10,
      location: `测试地点${randomSeed + 1}号地块`,
      // 使用paymentAmount字段作为服务费用
      paymentAmount: paymentAmount,
      // balance字段用于兼容旧代码
      balance: paymentAmount,
      // amount字段用于页面显示
      amount: paymentAmount.toFixed(2),
      farmerId: randomSeed + 1000, // 添加farmerId字段
      farmerName: `农户${randomSeed}`,
      contactPhone: `138****${randomSeed % 10000}`,
      notes: randomSeed % 2 === 0 ? '请按指定时间到达，服务区域较大，建议提前规划路线。' : ''
    };
  },

  // 获取状态文本
  getStatusText: function(status) {
    // 处理特定状态码4（已完成）
    if (status === 4) {
      return '已完成';
    }
    // 处理其他状态
    return SprayDemandStatusEnum[Object.keys(SprayDemandStatusEnum).find(key => SprayDemandStatusEnum[key].value === status)]?.label || '未知状态';
  },

  // 联系农户
  contactFarmer: function() {
    console.log('contactFarmer 函数被调用');
    const farmerId = this.data.orderInfo.farmerId;
    const farmerName = this.data.orderInfo.farmerName || '农户';
    const farmerPhone = this.data.orderInfo.contactPhone || '';
    console.log('农户信息:', farmerId, farmerName, farmerPhone);
    
    // 显示联系方式选择弹窗
    console.log('准备显示联系方式选择弹窗');
    wx.showActionSheet({
      itemList: ['在线联系', '拨打电话'],
      success: (res) => {
        console.log('选择了联系方式:', res.tapIndex);
        if (res.tapIndex === 0) {
          // 在线联系 - 跳转到现有的聊天详情页面
          console.log('准备跳转到聊天详情页面');
          
          // 确保farmerId是字符串格式
          const userId = String(farmerId);
          const userName = encodeURIComponent(farmerName || '农户');
          
          console.log('用户信息:', { userId, userName });
          
          wx.navigateTo({
            url: `/pages/flyer/chat/detail?otherUserId=${userId}&otherUserName=${userName}&otherUserType=1`,
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
          console.log('准备拨打电话:', farmerPhone);
          if (farmerPhone) {
            wx.makePhoneCall({
              phoneNumber: farmerPhone,
              fail: () => {
                wx.showToast({
                  title: '拨打电话失败',
                  icon: 'none'
                });
              }
            });
          } else {
            wx.showToast({
              title: '农户未设置电话号码',
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

  // 开始服务
  startService: async function() {
    // 检查当前订单状态，确保只有在待接取(0)状态下才能开始服务
    const currentStatus = this.data.orderInfo.status;
    const ALLOWED_STATUS = 0; // 0是待接取状态，这是可以接单的状态
    
    if (currentStatus !== ALLOWED_STATUS) {
      wx.showToast({
        title: '当前订单状态不允许开始服务',
        icon: 'none'
      });
      console.log('无法开始服务，当前订单状态:', currentStatus);
      return;
    }
    
    wx.showModal({
      title: '开始服务',
      content: '确定要开始服务吗？',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '处理中...',
          });
          
          try {
            // 获取用户信息
            const userInfo = wx.getStorageSync('userInfo') || {};
            const flyerId = userInfo.id || 1; // 默认使用1作为演示ID
            
            // 调用API开始服务，添加userId和userRole参数以确保权限正确
            const result = await sprayDemandAPI.startWork({
              demandId: this.data.orderInfo.id,
              flyerId: flyerId,
              userId: flyerId, // 添加userId参数，与flyerId保持一致
              userRole: 'flyer' // 明确指定当前用户角色为飞手
            });
            
            if (result) {
              this.setData({
                'orderInfo.status': SprayDemandStatusEnum.IN_OPERATION.value
              });
              
              wx.showToast({
                title: '服务已开始',
                icon: 'success',
                duration: 2000
              });
            } else {
              wx.showToast({
                title: '开始服务失败',
                icon: 'none'
              });
            }
          } catch (error) {
            console.error('开始服务失败', error);
            wx.showToast({
              title: '开始服务失败',
              icon: 'none'
            });
          } finally {
            // 确保无论成功还是失败都会调用hideLoading，避免配对错误
            wx.hideLoading();
          }
        }
      }
    });
  },

  // 完成服务（确认订单）
  completeService: async function() {
    // 获取订单信息，特别是paymentAmount字段（服务费用）和状态
    const { orderInfo } = this.data;
    const orderId = orderInfo.id;
    const currentStatus = orderInfo.status;
    const paymentAmount = orderInfo.paymentAmount || orderInfo.balance || 0;
    // 计算实际获得金额（90%）
    const actualAmount = paymentAmount * 0.9;
    
    // 状态检查，允许在状态为1(进行中)、2(待验收)或3(待确认)时调用完成服务
    // 新业务流程：飞手接取后状态直接变为3(待确认)，然后可以直接确认完成
    if (currentStatus !== 1 && currentStatus !== 2 && currentStatus !== 3) {
      wx.showToast({
        title: '订单状态错误，无法确认完成',
        icon: 'none',
        duration: 2000
      });
      console.warn('尝试确认完成服务，但订单状态不正确:', currentStatus);
      return;
    }
    
    wx.showModal({
      title: '完成服务确认',
      content: `确定已完成该需求服务吗？完成后平台将抽取10%服务费，您将获得¥${actualAmount.toFixed(2)}的报酬，资金将直接添加到您的账户余额中。`,
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '处理中...',
          });
          
          try {
            // 获取用户信息
            const userInfo = wx.getStorageSync('userInfo') || {};
            const flyerId = userInfo.id || 1; // 默认使用1作为演示ID
            
            console.log('准备完成服务，订单ID:', orderId, '将添加到飞手账户的金额:', actualAmount, '当前状态:', currentStatus, '服务费用:', paymentAmount);
            
            // 调用API完成服务，确保使用正确的参数名称demandId
            // 调用完成作业接口，状态从待确认(3)变为已完成(4)
            const result = await sprayDemandAPI.completeWork({
              demandId: orderId,
              flyerId: flyerId,
              userId: flyerId, // 添加userId参数，与flyerId保持一致
              userRole: 'flyer' // 明确指定当前用户角色为飞手
            });
            
            if (result) {
              // 更新订单状态为4（已完成）
              this.setData({
                'orderInfo.status': 4 // 直接设置为4，表示已完成状态
              });
              
              console.log('服务完成成功，订单状态已更新为已完成(4)');
              console.log(`¥${actualAmount.toFixed(2)}已成功添加到飞手账户余额中`);
              
              // 显示成功提示，包含金额信息
              wx.showToast({
                title: `确认成功，订单已完成，¥${actualAmount.toFixed(2)}已添加到您的账户`,
                icon: 'success',
                duration: 3000
              });
              
              // 刷新订单详情，确保显示最新状态和余额
              setTimeout(() => {
                this.loadOrderDetail();
              }, 2000);
            } else {
              wx.showToast({
                title: '完成服务失败，请重试',
                icon: 'none'
              });
            }
          } catch (error) {
              // 优化错误处理，直接使用error.message作为错误提示
              let errorMessage = '确认订单失败，请重试';
              
              // 优先使用error.message
              if (error && error.message) {
                // 直接使用后端返回的具体错误信息
                errorMessage = error.message;
              } else if (error && error.response && error.response.data) {
                // 尝试从error.response.data中获取更多错误信息
                if (error.response.data.message) {
                  errorMessage = error.response.data.message;
                } else if (error.response.data.msg) {
                  errorMessage = error.response.data.msg;
                } else {
                  errorMessage = String(error);
                }
              } else if (error) {
                errorMessage = String(error);
              }
              
              // 显示详细的错误信息
              wx.showToast({
                title: errorMessage,
                icon: 'none',
                duration: 5000 // 增加显示时间，让用户有足够时间阅读错误信息
              });
              
              // 记录详细的错误日志，包括可能的后端返回数据
              console.error('确认订单失败:', { 
                message: errorMessage, 
                error: error,
                response: error && error.response ? error.response : '无响应数据'
              });
          }
        }
      }
    });
  }
});