// pages/payment/history.js
const { api } = require('../../utils/request');

Page({
  data: {
    transactionList: [],
    loading: false,
    hasMore: true,
    pageNum: 1,
    pageSize: 10,
    currentMonth: '',
    searchText: ''
  },

  onLoad: function() {
    // 设置当前月份
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    this.setData({
      currentMonth: `${year}-${month}`
    });
    
    // 加载交易记录
    this.loadTransactionList(true);
  },

  // 加载交易记录
  loadTransactionList: function(isRefresh = false) {
    if (this.data.loading) return;
    
    this.setData({
      loading: true
    });
    
    const pageNum = isRefresh ? 1 : this.data.pageNum;
    const { pageSize, currentMonth, searchText } = this.data;
    
    // 准备请求参数
    const params = {
      pageNum,
      pageSize,
      month: currentMonth
    };
    
    // 如果有搜索文本，添加到参数中
    if (searchText) {
      params.keyword = searchText;
    }
    
    console.log('请求交易记录参数:', params);
    
    // 调用交易记录接口
    api.getUserTransactions(params)
      .then(res => {
        console.log('交易记录返回数据:', res);
        
        // 假设接口返回格式为 { data: { records: [], total, pageNum, pageSize } }
        const newList = res.data?.records || [];
        const total = res.data?.total || 0;
        
        // 格式化交易记录数据
        const formattedList = newList.map(item => ({
          ...item,
          // 确保amount字段存在且为数字
          amount: item.amount ? parseFloat(item.amount).toFixed(2) : '0.00',
          // 格式化时间
          createTime: item.createTime ? this.formatDateTime(item.createTime) : '',
          // 类型文本
          typeText: item.type === 'income' ? '收入' : '支出'
        }));
        
        this.setData({
          transactionList: isRefresh ? formattedList : [...this.data.transactionList, ...formattedList],
          pageNum: pageNum + 1,
          hasMore: this.data.transactionList.length + formattedList.length < total,
          loading: false
        });
      })
      .catch(err => {
        console.error('获取交易记录失败:', err);
        wx.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
        this.setData({
          loading: false
        });
      });
  },

  // 格式化日期时间
  formatDateTime: function(dateTime) {
    if (!dateTime) return '';
    
    try {
      const date = new Date(dateTime);
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');
      
      return `${month}-${day} ${hour}:${minute}`;
    } catch (error) {
      console.error('日期格式化错误:', error);
      return dateTime;
    }
  },

  // 日期变更处理
  onDateChange: function(e) {
    const value = e.detail.value;
    this.setData({
      currentMonth: value
    });
    // 重新加载数据
    this.loadTransactionList(true);
  },

  // 搜索输入处理
  onSearchInput: function(e) {
    const value = e.detail.value;
    this.setData({
      searchText: value
    });
    
    // 简单防抖
    if (this.searchTimer) {
      clearTimeout(this.searchTimer);
    }
    
    this.searchTimer = setTimeout(() => {
      // 重新加载数据
      this.loadTransactionList(true);
    }, 500);
  },

  // 加载更多
  loadMore: function() {
    if (!this.data.hasMore || this.data.loading) return;
    this.loadTransactionList(false);
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadTransactionList(true);
    setTimeout(() => {
      wx.stopPullDownRefresh();
    }, 500);
  }
});