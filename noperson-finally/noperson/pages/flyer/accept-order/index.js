// flyer/accept-order/index.js
const { api } = require('../../../utils/request');
const { SprayDemandStatusEnum } = require('../../../utils/enum');
const sprayDemandAPI = require('../../../utils/api/spray-demand-api');

Page({
  data: {
    orderType: 'all', // 'all', 'inspection' 或 'spray'
    waitingOrders: [],
    loading: false,
    refreshing: false,
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    currentLocation: null // 飞手当前位置
  },
  
  onLoad: async function(options) {
    // 优先从本地存储获取类型参数（用于switchTab跳转的情况）
    const storedType = wx.getStorageSync('acceptOrderType');
    if (storedType) {
      this.setData({
        orderType: storedType
      });
      // 清除本地存储中的参数，避免影响后续跳转
      wx.removeStorageSync('acceptOrderType');
    } else if (options && options.type) {
      // 如果没有本地存储参数，再检查URL参数
      this.setData({
        orderType: options.type
      });
    }
    

    
    // 检查全局接单状态
    await this.checkGlobalOrderStatus();
    
    // 检查是否有特定需求ID参数（可能来自URL或本地存储）
    const storedId = wx.getStorageSync('acceptOrderId');
    if (storedId) {
      // 如果存在特定需求ID，先加载完整列表
      await this.loadWaitingOrders();
      
      // 查找并跳转到对应的需求详情
      const orderIndex = this.data.waitingOrders.findIndex(item => item.id === storedId);
      if (orderIndex !== -1) {
        this.viewOrderDetail({ currentTarget: { dataset: { id: storedId } } });
      }
      
      // 清除本地存储中的ID参数
      wx.removeStorageSync('acceptOrderId');
    } else if (options && options.id) {
      // 先加载完整列表
      await this.loadWaitingOrders();
      
      // 查找并跳转到对应的需求详情
      const orderIndex = this.data.waitingOrders.findIndex(item => item.id === options.id);
      if (orderIndex !== -1) {
        this.viewOrderDetail({ currentTarget: { dataset: { id: options.id } } });
      }
    } else {
      // 如果没有特定ID参数，正常加载等待列表
      await this.loadWaitingOrders();
    }
  },
  
  // 切换订单类型
  switchOrderType: async function(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({
      orderType: type,
      waitingOrders: [],
      pageNum: 1,
      hasMore: true
    });
    await this.loadWaitingOrders();
  },
  

  
  // 加载待接单列表
  loadWaitingOrders: async function() {
    if (!this.data.hasMore || this.data.loading) {
      return;
    }
    
    this.setData({
      loading: true
    });
    
    try {
      // 获取用户信息
      const userInfo = wx.getStorageSync('userInfo') || {};
      const flyerId = userInfo.id || 1; // 默认使用1作为演示ID
      
      // 准备请求参数
      const params = {
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize,
        status: `${SprayDemandStatusEnum.WAITING.value}`
        // 只获取待接取状态的需求
      };
      
      // 根据订单类型选择不同的API调用方式
      let response;
      if (this.data.orderType === 'all') {
        // 获取全部需求
        // 注意：全量接口使用的是statusStr参数
        const allParams = {...params};
        allParams.statusStr = allParams.status;
        delete allParams.status;
        response = await sprayDemandAPI.getAllFarmersDemandsForFlyer(allParams);
      } else if (this.data.orderType === 'inspection') {
        // 获取巡检需求
        response = await sprayDemandAPI.getInspectionOnlyDemands(params);
      } else {
        // 获取喷洒需求
        response = await sprayDemandAPI.getSprayOnlyDemands(params);
      }
      
      // 处理返回的数据 - API响应格式为 {code: 200, message: "操作成功", data: {...}}，需要访问data.records
      const newOrders = response?.data?.records || [];
      
      // 先进行需求类型筛选，确保显示的需求类型与选择的页面类型一致
      let typeFilteredOrders = newOrders;
      if (this.data.orderType === 'inspection') {
        // 只显示巡检需求
        typeFilteredOrders = newOrders.filter(order => 
          order.orderType === 2 || 
          order.demandType === 'inspection' || 
          order.type === 2 || 
          order.type === 'inspection'
        );
      } else if (this.data.orderType === 'spray') {
        // 只显示喷洒需求
        typeFilteredOrders = newOrders.filter(order => 
          order.orderType === 1 || 
          (!order.orderType && !order.demandType && order.type !== 2 && order.type !== 'inspection')
        );
      }
      
      // 不需要距离筛选，直接使用类型筛选后的结果
      // 格式化时间
      const filteredOrders = typeFilteredOrders.map(order => ({
        ...order,
        createTime: this.formatTime(order.createTime),
        expectedTime: this.formatTime(order.expectedTime)
      }));
      
      this.setData({
        waitingOrders: this.data.pageNum === 1 ? filteredOrders : [...this.data.waitingOrders, ...filteredOrders],
        hasMore: newOrders.length === this.data.pageSize && response?.data?.total > this.data.waitingOrders.length + newOrders.length,
        pageNum: this.data.pageNum + 1
      });
      

    } catch (error) {
      console.error('加载待接单列表失败', error);
      wx.showToast({
        title: '加载待接单列表失败',
        icon: 'none'
      });
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
      hasMore: true
    });
    this.loadWaitingOrders();
  },
  
  // 上拉加载更多
  onLoadMore: function() {
    this.loadWaitingOrders();
  },
  
  // 接单
  acceptOrder: function(e) {
    // 详细日志记录，帮助诊断问题
    console.log('acceptOrder事件触发', { 
      event: e, 
      currentTarget: e?.currentTarget, 
      target: e?.target,
      dataset: e?.currentTarget?.dataset 
    });
    
    // 确保e和currentTarget存在
    if (!e) {
      console.error('接单失败：事件对象为空');
      wx.showToast({
        title: '操作失败：参数错误',
        icon: 'none'
      });
      return;
    }
    
    if (!e.currentTarget || !e.currentTarget.dataset) {
      console.error('接单失败：无效的事件参数，无法获取dataset');
      wx.showToast({
        title: '操作失败：参数错误',
        icon: 'none'
      });
      return;
    }
    
    // 直接从dataset获取需求ID，根据WXML中的data-demand-id属性
    const demandId = e.currentTarget.dataset.demandId;
    
    console.log('获取的需求ID:', demandId);
    
    // 验证需求ID不为空
    if (!demandId && demandId !== 0) {
      console.error('接单失败：需求ID为空');
      wx.showToast({
        title: '操作失败：需求ID无效',
        icon: 'none'
      });
      return;
    }
    
    // 输出完整的订单信息以便调试
    // 根据需求ID查找对应的订单项
    const orderItem = this.data.waitingOrders.find(item => 
      item.id === demandId || item.demandId === demandId
    );
    console.log('订单详情:', orderItem);
    
    // 直接使用从按钮获取的demandId
    const actualDemandId = demandId;
    console.log('使用的需求ID:', actualDemandId);
    
    wx.showModal({
      title: '接单确认',
      content: '确定要接取这个订单吗？',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '检查中...',
          });
          
          try {
            // 先检查飞手是否有未完成订单
            const hasUnfinishedOrder = await this.checkUnfinishedOrder();
            if (hasUnfinishedOrder) {
              wx.hideLoading();
              wx.showToast({
                title: '您有未完成的订单，无法接新单',
                icon: 'none',
                duration: 2000
              });
              return;
            }
            
            wx.showLoading({
              title: '处理中...',
            });
            
            // 根据用户要求，所有类型的需求都使用同一个接单接口：/demand/spray/accept/{demandId}
            // 新业务逻辑：无论需求是否已支付，接单后系统都会立即将需求状态设置为已接单
            // 根据后端接口定义，只需传递demandId作为URL参数
            const result = await sprayDemandAPI.acceptSprayDemand({
              demandId: actualDemandId
            });
            
            wx.hideLoading();
            
            // 判断接单是否成功（根据后端返回的格式）
            const isSuccess = result && (result.demandId || result.data || result === true);
            
            if (isSuccess) {
              // 成功接取需求
              console.log('接单成功，返回的订单信息:', result);
              
              wx.showToast({
                title: '接单成功',
                icon: 'success'
              });
              
              // 刷新订单列表
              this.setData({
                waitingOrders: [],
                pageNum: 1,
                hasMore: true
              });
              await this.loadWaitingOrders();
            } else {
              wx.showToast({
                title: '接单失败，请重试',
                icon: 'none'
              });
            }
          } catch (error) {
            wx.hideLoading();
            // 如果是后端返回的未完成订单错误，显示友好提示
            if (error.message && error.message.includes('未完成的订单')) {
              wx.showToast({
                title: '您有未完成的订单，无法接新单',
                icon: 'none',
                duration: 2000
              });
            } else {
              wx.showToast({
                title: '接单失败，请重试',
                icon: 'none'
              });
            }
            console.error('接单失败', error);
          }
        }
      }
    });
  },
  
  // 查看订单详情
  viewOrderDetail: function(e) {
    // 使用data-demand-id获取需求ID
    const demandId = e.currentTarget.dataset.demandId;
    console.log('查看详情，需求ID:', demandId);
    
    wx.navigateTo({
      url: `/pages/flyer/order-detail/index?orderId=${demandId}&orderType=${this.data.orderType}`
    });
  },
  
  // 获取订单状态文本
  getStatusText: function(status) {
    const statusMap = {
      0: '待接单',
      1: '处理中',
      2: '已完成',
      3: '已取消'
    };
    return statusMap[status] || '未知';
  },

  // 检查飞手是否有未完成订单
  checkUnfinishedOrder: async function() {
    try {
      // 获取用户信息
      const userInfo = wx.getStorageSync('userInfo') || {};
      const flyerId = userInfo.id || 1; // 默认使用1作为演示ID
      
      // 查询飞手已接单的需求列表
      const response = await sprayDemandAPI.getMyDemands({
        pageNum: 1,
        pageSize: 10
      });
      
      // 检查是否有未完成的订单（状态不是已完成或已取消）
      const orders = response?.data?.records || [];
      const unfinishedOrders = orders.filter(order => {
        // 未完成状态：待确认、处理中、作业中等
        return order.status !== 4 && order.status !== 6;
      });
      
      return unfinishedOrders.length > 0;
    } catch (error) {
      console.error('检查未完成订单失败:', error);
      // 出错时返回false，让后端继续检查
      return false;
    }
  },

  // 检查全局接单状态
  checkGlobalOrderStatus: async function() {
    try {
      const response = await sprayDemandAPI.getFlyerOrderStatus();
      const isBanned = response?.data || false;
      
      if (isBanned) {
        wx.showToast({
          title: '由于天气问题，目前禁止接单！',
          icon: 'none',
          duration: 3000
        });
      }
      
      return isBanned;
    } catch (error) {
      console.error('检查全局接单状态失败:', error);
      return false;
    }
  },

  // 格式化时间
  formatTime: function(time) {
    if (!time) return '';
    
    const date = new Date(time);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}年${month}月${day}日${hours}:${minutes}:${seconds}`;
  },

  // 联系农户 - 显示联系方式选择
  contactFarmer: function(e) {
    const farmerId = e.currentTarget.dataset.farmerId;
    const farmerName = e.currentTarget.dataset.farmerName;
    const farmerPhone = e.currentTarget.dataset.farmerPhone || '';
    
    // 保存当前农户信息到data，供后续使用
    this.setData({
      currentContactFarmer: {
        farmerId: farmerId,
        farmerName: farmerName,
        farmerPhone: farmerPhone
      }
    });
    
    // 显示联系方式选择弹窗
    wx.showActionSheet({
      itemList: ['在线联系', '拨打电话'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 在线联系 - 直接跳转到聊天详情页面，创建会话
          wx.navigateTo({
            url: `/pages/chat/chat?targetUserId=${farmerId}&targetUserName=${encodeURIComponent(farmerName)}`,
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
  }
})