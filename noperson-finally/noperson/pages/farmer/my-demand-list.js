// farmer/my-demand-list.js
const { api } = require('../../utils/request');

Page({
  data: {
    demandList: [],
    loading: false,
    refreshing: false,
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    currentStatusIndex: 0,
    statusList: [
      { id: -1, name: '全部' },
      { id: 0, name: '待接取' },
      { id: 1, name: '处理中' },
      { id: 2, name: '作业中' },
      { id: 3, name: '待确认' },
      { id: 4, name: '已完成' },
      { id: 5, name: '已取消' }
    ],
    userInfo: null,
    cachedList: null,
    error: false,
    errorMsg: ''
  },

  onLoad: function(options) {
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      userInfo: userInfo
    });

    if (options.status !== undefined) {
      const statusIndex = this.data.statusList.findIndex(item => item.id === parseInt(options.status));
      if (statusIndex !== -1) {
        this.setData({
          currentStatusIndex: statusIndex
        });
      }
    }

    this.loadDemands();
  },

  onShow: function() {
    if (this.data.demandList.length === 0) {
      this.loadDemands();
    }
  },

  onPullDownRefresh: function() {
    this.setData({
      demandList: [],
      pageNum: 1,
      hasMore: true,
      refreshing: true,
      error: false,
      errorMsg: ''
    });
    this.loadDemands();
  },
  
  // 下拉刷新处理
  onRefresh: function() {
    this.setData({
      demandList: [],
      pageNum: 1,
      hasMore: true,
      refreshing: true,
      error: false,
      errorMsg: ''
    });
    this.loadDemands();
  },

  onLoadMore: function() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadDemands();
    }
  },

  loadDemands: async function() {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true, error: false, errorMsg: '' });

    try {
      const userInfo = wx.getStorageSync('userInfo');
      if (!userInfo || !userInfo.id) {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        this.setData({ loading: false, refreshing: false });
        return;
      }

      const currentStatus = this.data.statusList[this.data.currentStatusIndex].id;
      let result;

      // 使用 getFarmerAllDemandsList 接口获取所有需求列表
      result = await api.getFarmerAllDemandsList(this.data.pageNum, this.data.pageSize, currentStatus === -1 ? undefined : currentStatus);

      // 处理不同的返回数据结构
      let demands = [];
      if (Array.isArray(result.data)) {
        demands = result.data;
      } else if (result.data && Array.isArray(result.data.list)) {
        demands = result.data.list;
      } else if (result.data && Array.isArray(result.data.records)) {
        demands = result.data.records;
      } else if (Array.isArray(result)) {
        demands = result;
      }

      const formattedDemands = demands.map(demand => {
        // 统一字段名映射（后端返回驼峰命名，前端使用下划线命名）
        const formattedDemand = {
          ...demand,
          // 基础字段映射
          id: demand.demandId || demand.id,
          order_type: demand.orderType !== undefined ? demand.orderType : demand.order_type,
          land_area: demand.landArea !== undefined ? demand.landArea : demand.land_area,
          land_location: demand.landLocation || demand.land_location,
          land_name: demand.landName || demand.land_name,
          crop_type: demand.cropType || demand.crop_type,
          pest_type: demand.pestType || demand.pest_type,
          create_time: demand.createTime || demand.create_time,
          expected_time: demand.expectedTime || demand.expected_time,
          accept_time: demand.acceptTime || demand.accept_time,
          budget: demand.budget,
          payment_amount: demand.paymentAmount || demand.payment_amount,
          payment_method: demand.paymentMethod || demand.payment_method,
          payment_status: demand.paymentStatus !== undefined ? demand.paymentStatus : demand.payment_status,
          payment_time: demand.paymentTime || demand.payment_time,
          flyer_id: demand.flyerId || demand.flyer_id,
          flyer_name: demand.flyerName || demand.flyer_name,
          farmer_id: demand.farmerId || demand.farmer_id,
          farmer_name: demand.farmerName || demand.farmer_name,
          inspection_purpose: demand.inspectionPurpose || demand.inspection_purpose,
          expected_resolution: demand.expectedResolution || demand.expected_resolution,
          report_id: demand.reportId || demand.report_id,
          // 格式化时间
          formattedCreateTime: this.formatDateTime(demand.createTime || demand.create_time),
          formattedExpectedTime: (demand.expectedTime || demand.expected_time) ? this.formatDateTime(demand.expectedTime || demand.expected_time) : null,
          // 添加状态文本和颜色类
          statusText: this.getStatusText(demand.status),
          statusColorClass: this.getStatusColorClass(demand.status)
        };

        // 设置需求类型描述
        if (formattedDemand.order_type !== undefined) {
          formattedDemand.orderTypeDesc = formattedDemand.order_type === 2 ? '巡检' : '喷洒';
        }

        return formattedDemand;
      });

      this.setData({
        demandList: this.data.pageNum === 1 ? formattedDemands : [...this.data.demandList, ...formattedDemands],
        hasMore: demands.length === this.data.pageSize,
        pageNum: this.data.pageNum + 1,
        loading: false,
        refreshing: false,
        cachedList: result
      });

      wx.stopPullDownRefresh();
    } catch (error) {
      console.error('获取需求列表失败', error);
      this.setData({
        loading: false,
        refreshing: false,
        error: true,
        errorMsg: '加载失败，请重试'
      });
      wx.stopPullDownRefresh();
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    }
  },

  switchStatus: function(e) {
    const index = e.detail.value;
    this.setData({
      currentStatusIndex: index,
      demandList: [],
      pageNum: 1,
      hasMore: true
    });
    this.loadDemands();
  },

  viewDemandDetail: function(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/farmer/demand-detail?id=${id}`
    });
  },

  cancelDemand: function(e) {
    const id = e.currentTarget.dataset.id;

    wx.showModal({
      title: '确认取消',
      content: '确定要取消这个需求吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const result = await api.cancelDemand(id, '用户主动取消');
            if (result.success || result.code === 200) {
              wx.showToast({
                title: '取消成功',
                icon: 'success'
              });
              this.setData({
                demandList: [],
                pageNum: 1,
                hasMore: true
              });
              this.loadDemands();
            } else {
              wx.showToast({
                title: result.message || '取消失败',
                icon: 'none'
              });
            }
          } catch (error) {
            wx.showToast({
              title: '取消失败，请重试',
              icon: 'none'
            });
            console.error('取消需求失败', error);
          }
        }
      }
    });
  },

  createReview: function(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/farmer/demand-detail?id=${id}`
    });
  },

  refreshPage: function() {
    this.setData({
      demandList: [],
      pageNum: 1,
      hasMore: true,
      refreshing: true,
      error: false,
      errorMsg: ''
    });
    this.loadDemands();
  },

  getStatusText: function(status) {
    const statusMap = {
      0: '待接取',
      1: '处理中',
      2: '作业中',
      3: '待确认',
      4: '已完成',
      5: '已取消'
    };
    return statusMap[status] || '未知';
  },

  getStatusColorClass: function(status) {
    const colorMap = {
      0: 'status-pending',
      1: 'status-processing',
      2: 'status-operation',
      3: 'status-confirm',
      4: 'status-completed',
      5: 'status-cancelled'
    };
    return colorMap[status] || 'status-default';
  },

  formatDateTime: function(dateTime) {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  },

  // 导航到首页
  navigateToHome: function() {
    wx.redirectTo({
      url: '/pages/farmer/index'
    });
  },

  // 导航到消息页面
  navigateToChat: function() {
    wx.redirectTo({
      url: '/pages/farmer/chat/list'
    });
  },

  // 导航到我的页面
  navigateToProfile: function() {
    wx.redirectTo({
      url: '/pages/farmer/profile'
    });
  }
});
