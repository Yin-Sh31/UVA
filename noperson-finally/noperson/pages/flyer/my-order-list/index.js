// 飞手的我的接单页面
const sprayDemandAPI = require('../../../utils/api/spray-demand-api');
const { SprayDemandStatusEnum } = require('../../../utils/enum');

Page({
  // 处理返回按钮点击事件
  onBackTap: function() {
    console.log('返回按钮被点击');
    wx.navigateBack({ delta: 1 });
  },
  data: {
    statusList: [
      { name: '全部订单', value: -1 },
      { name: '处理中', value: SprayDemandStatusEnum.PROCESSING.value },
    { name: '作业中', value: SprayDemandStatusEnum.IN_OPERATION.value },
    { name: '待确认', value: SprayDemandStatusEnum.PENDING_CONFIRM.value },
      { name: '已完成', value: SprayDemandStatusEnum.COMPLETED.value },
      { name: '已取消', value: SprayDemandStatusEnum.CANCELLED.value }
    ],
    currentStatusIndex: 0,
    currentStatus: -1,
    orderList: [],
    loading: false,
    refreshing: false,
    hasMore: true,
    pageNum: 1,
    pageSize: 10
  },

  onLoad: function() {
    this.loadOrders();
  },

  onShow: function() {
    // 不再自动刷新数据，只在用户手动刷新时更新
  },
  
  // 生命周期函数，确保页面可以正确卸载
  onUnload: function() {
    console.log('页面卸载');
    // 清除页面相关的定时器或监听器
  },
  
  // 下拉刷新处理
  onRefresh: function() {
    this.setData({ 
      refreshing: true,
      pageNum: 1,
      orderList: [],
      hasMore: true
    });
    this.loadOrders();
  },
  
  // 加载更多
  onLoadMore: function() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadOrders();
    }
  },

  // 切换筛选状态
  switchStatus: function(e) {
    const index = e.detail.value;
    const status = this.data.statusList[index].value;
    this.setData({
      currentStatusIndex: index,
      currentStatus: status,
      pageNum: 1,
      orderList: [],
      hasMore: true
    });
    this.loadOrders();
  },

  // 加载订单列表
  loadOrders: async function() {
    if (this.data.loading || !this.data.hasMore) {
      return;
    }

    this.setData({ loading: true });

    try {
      // 准备请求参数，使用新接口不需要传入flyerId
      const params = {
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      };
      
      // 如果不是查询全部状态，则添加状态筛选参数
      if (this.data.currentStatus !== -1) {
        params.statusStr = this.data.currentStatus.toString();
      }
      
      // 调用新API获取当前飞手已接取的需求
      const response = await sprayDemandAPI.getMyDemands(params);
      console.log('API返回完整数据:', response); // 添加日志以便调试
      
      // 刷新完成后结束下拉刷新状态
      if (this.data.refreshing) {
        wx.stopPullDownRefresh();
      }
      
      const mockOrders = this.data.pageNum === 1 ? [] : this.generateMockOrders();
      
      // 处理API返回的数据，转换字段名以匹配WXML中的引用
      let newList = [];
      // 正确访问response.data.records而不是response.records
      if (response.data && response.data.records && response.data.records.length > 0) {
        // 将API返回的数据转换为WXML中需要的格式
        const formattedList = response.data.records.map(item => ({
          id: item.demandId, // 使用demandId作为订单ID
          status: item.status,
          type: item.orderType, // 将orderType映射为type
          acceptTime: item.acceptTime,
          area: item.landArea || 0, // 将landArea映射为area，确保有默认值
          location: item.landName || '未知地点', // 使用landName作为location，确保有默认值
          farmerName: item.flyerName || '农户用户', // 使用flyerName作为farmerName
          paymentAmount: item.paymentAmount || 0, // 服务费用
          // 保留原始数据以便详情页面使用
          originalData: item
        }));
        
        newList = this.data.pageNum === 1 ? formattedList : [...this.data.orderList, ...formattedList];
      } else {
        // 使用模拟数据
        newList = mockOrders;
      }
      
      this.setData({
        orderList: newList,
        loading: false,
        refreshing: false,
        hasMore: (response.data && response.data.records ? response.data.records.length : mockOrders.length) === this.data.pageSize,
        pageNum: this.data.pageNum + 1
      });
    } catch (error) {
      console.error('加载订单列表失败', error);
      wx.showToast({
        title: '加载订单列表失败',
        icon: 'none'
      });
      
      // 发生错误时使用模拟数据
      setTimeout(() => {
        // 刷新完成后结束下拉刷新状态
        if (this.data.refreshing) {
          wx.stopPullDownRefresh();
        }
        
        const mockOrders = this.generateMockOrders();
        
        this.setData({
          orderList: this.data.pageNum === 1 ? mockOrders : [...this.data.orderList, ...mockOrders],
          loading: false,
          refreshing: false,
          hasMore: mockOrders.length === this.data.pageSize,
          pageNum: this.data.pageNum + 1
        });
      }, 1000);
    }
  },

  // 生成模拟订单数据（API调用失败时使用）
  generateMockOrders: function() {
    const orders = [];
    const statuses = [
      SprayDemandStatusEnum.PROCESSING.value,
      SprayDemandStatusEnum.IN_OPERATION.value,
      SprayDemandStatusEnum.PENDING_CONFIRM.value,
      SprayDemandStatusEnum.COMPLETED.value,
      SprayDemandStatusEnum.CANCELLED.value
    ];
    const types = [1, 2]; // 1: 喷洒订单, 2: 巡检订单
    
    for (let i = 0; i < this.data.pageSize; i++) {
      const randomIndex = (this.data.pageNum - 1) * this.data.pageSize + i;
      const status = this.data.currentStatus === -1 ? statuses[Math.floor(Math.random() * statuses.length)] : this.data.currentStatus;
      const type = types[Math.floor(Math.random() * types.length)];
      
      orders.push({
        id: `ORD${100000 + randomIndex}`,
        status: status,
        type: type,
        acceptTime: `2023-06-${10 + randomIndex % 20} ${Math.floor(Math.random() * 24)}:${Math.floor(Math.random() * 60).toString().padStart(2, '0')}`,
        area: Math.floor(Math.random() * 500) + 10,
        location: `测试地点${randomIndex + 1}号地块`,
        farmerName: `农户${Math.floor(Math.random() * 1000)}`,
        paymentAmount: Math.floor(Math.random() * 200) + 50 // 添加模拟服务费用
      });
    }
    
    return orders;
  },

  // 获取状态文本
  getStatusText: function(status) {
    return SprayDemandStatusEnum[Object.keys(SprayDemandStatusEnum).find(key => SprayDemandStatusEnum[key].value === status)]?.label || '未知状态';
  },

  // 查看订单详情
  viewOrderDetail: function(e) {
    console.log('查看详情按钮被点击，事件数据:', e);
    
    // 1. 首先尝试从事件数据中获取订单ID
    const dataset = e.currentTarget.dataset;
    let orderId = dataset.id;
    
    // 2. 如果事件中没有ID，使用当前订单项的demandId
    let orderItem;
    if (orderId) {
      console.log('从事件数据中获取到订单ID:', orderId);
      // 查找对应的订单项
      orderItem = this.data.orderList.find(item => String(item.demandId) === String(orderId) || String(item.id) === String(orderId));
    }
    
    // 3. 如果没有找到订单项，默认使用第一个订单项
    if (!orderItem && this.data.orderList && this.data.orderList.length > 0) {
      orderItem = this.data.orderList[0];
      console.log('使用第一个订单项:', orderItem);
    }
    
    // 4. 确保有有效的订单ID
    if (orderItem) {
      const actualOrderId = orderItem.demandId || orderItem.id;
      console.log('最终使用的订单ID:', actualOrderId);
      
      // 使用正确的路径格式
      const url = `/pages/flyer/order-detail/index?id=${actualOrderId}`;
      console.log('跳转URL:', url);
      
      // 执行页面跳转
      wx.navigateTo({
        url: url,
        success: function() {
          console.log('页面跳转成功');
        },
        fail: function(err) {
          console.error('页面跳转失败:', err);
          wx.showModal({
            title: '跳转失败',
            content: '错误: ' + err.errMsg,
            showCancel: false
          });
        }
      });
    } else {
      console.log('未找到有效的订单项');
      wx.showToast({
        title: '未找到订单信息',
        icon: 'none'
      });
    }
  },

  // 取消接单
  cancelOrder: async function(e) {
    const orderId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '取消接单',
      content: '确定要取消此订单吗？',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({
            title: '处理中...',
          });
          
          try {
            // 获取用户信息
            const userInfo = wx.getStorageSync('userInfo') || {};
          const operatorId = userInfo.id || 1; // 默认使用1作为演示ID
          
          // 验证orderId不为空
              if (!orderId && orderId !== 0) {
                console.error('取消订单失败：订单ID不能为空');
                wx.hideLoading();
                wx.showToast({
                  title: '操作失败：订单ID无效',
                  icon: 'none'
                });
                return;
              }
              
              // 输出完整的订单信息以便调试
              const orderItem = this.data.orderList.find(item => item.id === orderId);
              console.log('订单详情:', orderItem);
              
              // 从订单数据中获取正确的demandId字段
              const actualDemandId = orderItem?.demandId || orderId;
              console.log('使用的需求ID:', actualDemandId);
              
              // 调用API取消订单
              const result = await sprayDemandAPI.cancelDemand({
                demandId: actualDemandId,
                operatorId: operatorId,
                reason: '飞手主动取消'
              });
            
            wx.hideLoading();
            
            if (result) {
              wx.showToast({
                title: '订单已取消',
                icon: 'success'
              });
            } else {
              wx.showToast({
                title: '取消订单失败',
                icon: 'none'
              });
            }
            
            // 重新加载订单列表
            this.setData({
              pageNum: 1,
              orderList: [],
              hasMore: true
            });
            this.loadOrders();
          } catch (error) {
            wx.hideLoading();
            wx.showToast({
              title: '取消订单失败',
              icon: 'none'
            });
            console.error('取消订单失败', error);
          }
        }
      }
    });
  },

  // 上拉加载更多
  onLoadMore: function() {
    this.loadOrders();
  },

  // 下拉刷新
  onRefresh: function() {
    this.setData({
      pageNum: 1,
      orderList: [],
      hasMore: true,
      refreshing: true
    });
    this.loadOrders();
  },
  
  // 页面下拉刷新配置
  onPullDownRefresh: function() {
    this.onRefresh();
  }
});