// payment/create.js
const { api } = require('../../utils/request');

Page({
  data: {
    orderType: '',
    orderId: '',
    amount: 0,
    orderTitle: '',
    loading: false,
    paymentMethods: [
      { id: 'WECHAT', name: '微信支付', icon: '/assets/images/wechat-pay.png' }
    ],
    selectedMethod: 'WECHAT',
    type: '', // 支付类型
    deviceId: '', // 设备ID
    deviceName: '' // 设备名称
  },
  
  onLoad: function(options) {
    const { orderType, orderId, amount } = options;
    
    // 根据订单类型设置订单标题
    let orderTitle = '';
    if (orderType === 'INSPECTION') {
      orderTitle = '农业巡检服务';
    } else if (orderType === 'SPRAY') {
      orderTitle = '农业喷洒服务';
    }
    
    this.setData({
      orderType: orderType || '',
      orderId: orderId || '',
      amount: parseFloat(amount) || 0,
      orderTitle: orderTitle,
      type: options.type || '',
      deviceId: options.deviceId || '',
      deviceName: decodeURIComponent(options.deviceName || '')
    });
    
    // 根据支付类型更新导航栏标题
    if (this.data.type === 'device_rent') {
      wx.setNavigationBarTitle({
        title: `租借${this.data.deviceName || '设备'}支付`
      });
    }
  },
  
  // 选择支付方式
  selectPaymentMethod: function(e) {
    const methodId = e.currentTarget.dataset.id;
    this.setData({
      selectedMethod: methodId
    });
  },
  
  // 创建支付订单
  createPayment: async function() {
    if (!this.data.orderType || !this.data.orderId || this.data.amount <= 0) {
      wx.showToast({
        title: '支付信息不完整',
        icon: 'none'
      });
      return;
    }
    
    this.setData({
      loading: true
    });
    
    try {
      // 模拟创建支付订单
      setTimeout(() => {
        // 生成模拟的支付订单数据
        const paymentData = {
          orderType: this.data.orderType,
          orderId: this.data.orderId,
          paymentMethod: this.data.selectedMethod,
          amount: this.data.amount,
          outTradeNo: 'PAY' + Date.now(),
          status: 'WAITING'
        };
        
        // 模拟支付过程
        this.simulatePayment(paymentData);
      }, 1000);
    } catch (error) {
      console.error('创建支付订单失败', error);
      wx.showToast({
        title: '创建支付订单失败，请重试',
        icon: 'none'
      });
      this.setData({
        loading: false
      });
    }
  },
  
  // 模拟支付过程
  simulatePayment: async function(paymentData) {
    // 显示支付确认弹窗
    wx.showModal({
      title: '支付确认',
      content: `确认支付${paymentData.amount}元？`,
      success: async (res) => {
        if (res.confirm) {
          // 模拟支付中
          wx.showLoading({
            title: '支付处理中...',
            mask: true
          });
          
          // 模拟支付结果
          setTimeout(async () => {
            // 模拟支付结果（这里固定为成功，实际项目中应该调用真实的支付SDK）
            const paymentSuccess = true;
            
            if (paymentSuccess) {
              try {
                // 根据支付类型执行不同的后续操作
                if (this.data.type === 'device_rent' && this.data.deviceId) {
                  // 设备租借支付成功后调用租借接口
                  await api.rentDevice(this.data.deviceId);
                  
                  wx.hideLoading();
                  wx.showToast({
                    title: '支付成功，设备已租借',
                    icon: 'success',
                    duration: 2000
                  });

                  // 支付成功后返回设备详情页
                  setTimeout(() => {
                    wx.navigateBack();
                  }, 2000);
                } else {
                  // 其他类型支付
                  wx.hideLoading();
                  wx.showToast({
                    title: '支付成功',
                    icon: 'success',
                    duration: 2000
                  });

                  // 支付成功后跳转到农户首页
                  setTimeout(() => {
                    wx.switchTab({
                      url: '/pages/farmer/index'
                    });
                  }, 2000);
                }
              } catch (error) {
                wx.hideLoading();
                console.error('支付后操作失败', error);
                wx.showToast({
                  title: '支付成功，但操作失败',
                  icon: 'none'
                });
              }
            } else {
              wx.hideLoading();
              wx.showToast({
                title: '支付失败，请重试',
                icon: 'none'
              });
            }
            
            this.setData({
              loading: false
            });
          }, 2000);
        } else {
          this.setData({
            loading: false
          });
        }
      }
    });
  },
  
  // 取消支付
  cancelPayment: function() {
    wx.navigateBack();
  }
})