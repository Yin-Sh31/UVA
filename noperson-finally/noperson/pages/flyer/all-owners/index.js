// flyer/all-owners/index.js

const { request } = require('../../../utils/request');

Page({
  data: {
    owners: [],
    currentPage: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    noData: false,
    refreshing: false
  },

  onLoad() {
    // 默认加载机主列表
    this.loadOwnersList();
  },

  // 加载机主列表
  loadOwnersList() {
    const { currentPage, pageSize } = this.data;
    
    this.setData({ loading: true });
    
    const apiUrl = '/flyer/owners/list';
    
    // 直接使用request函数调用API
    request(apiUrl, 'GET', {
      pageNum: currentPage,
      pageSize: pageSize
    })
      .then(res => {
        let records = [];
        let total = 0;
        
        // 优先检查标准API响应格式：{code, message, data:{records:[]}}
        if (res && res.data && res.data.records && Array.isArray(res.data.records)) {
          records = res.data.records;
          total = res.data.total || 0;
        }
        // 检查API响应格式：{code, message, data:[]}
        else if (res && res.data && Array.isArray(res.data)) {
          records = res.data;
          total = records.length;
        }
        // 检查标准分页结构（如果直接返回data部分）
        else if (res && res.records && Array.isArray(res.records)) {
          records = res.records;
          total = res.total || 0;
        } 
        // 检查是否直接返回数组
        else if (Array.isArray(res)) {
          records = res;
          total = records.length;
        }
        // 降级处理：如果res本身就是对象数组元素
        else if (res && typeof res === 'object' && !Array.isArray(res)) {
          // 检查是否是单个对象，或者对象本身包含机主信息
          const hasOwnerInfo = res.username || res.phone || res.createTime || res.lastLoginTime;
          if (hasOwnerInfo) {
            records = [res]; // 包装成数组
            total = 1;
          }
        }
        
        // 处理数据，使用API返回的实际数据
        const processedOwners = records.map((record, index) => ({
          userId: record.userId || `owner_${(currentPage - 1) * pageSize + index + 1}`,
          username: record.companyName || record.username || `机主${(currentPage - 1) * pageSize + index + 1}`,
          phone: record.phone || '未设置手机号',
          avatar: record.avatar || '/assets/images/user-avatar.png',
          createTime: record.createTime || '',
          lastLoginTime: record.lastLoginTime || '',
          // 保留原始数据中的其他字段
          ...record
        }));
        
        // 更新数据
        this.setData({
          owners: processedOwners,
          total: total,
          noData: processedOwners.length === 0,
          loading: false,
          refreshing: false
        });
      })
      .catch(error => {
        console.error('加载机主列表失败:', error);
        this.setData({
          loading: false,
          refreshing: false
        });
        
        wx.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
      });
  },

  // 查看机主详情
  viewOwnerDetail(e) {
    const ownerId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/flyer/owner-info/index?id=${ownerId}`
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.setData({ currentPage: 1, refreshing: true });
    this.loadOwnersList();
  },

  // 上拉加载更多
  onReachBottom() {
    const { loading, currentPage, pageSize, total } = this.data;
    
    if (!loading && currentPage * pageSize < total) {
      this.setData({ currentPage: currentPage + 1 });
      this.loadOwnersList();
    }
  },
  
  // 返回主页
  backToHome() {
    wx.switchTab({
      url: '/pages/flyer/index'
    });
  }
});