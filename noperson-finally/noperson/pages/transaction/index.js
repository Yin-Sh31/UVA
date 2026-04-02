// pages/transaction/index.js

Page({
  data: {
    transactionList: [],
    loading: false,
    total: 0,
    page: 1,
    pageSize: 20,
    hasMore: true,
    selectedType: '' // ''表示全部，'INCOME'表示收入，'EXPENSE'表示支出
  },

  onLoad: function() {
    // 加载交易记录
    this.loadTransactions();
  },

  // 加载交易记录
  loadTransactions: function(isLoadMore = false) {
    if (!isLoadMore) {
      this.setData({
        loading: true,
        page: 1,
        transactionList: []
      });
    }
    
    // 获取用户token
    const token = wx.getStorageSync('token') || '';
    
    // 微信小程序中使用相对路径需要在项目配置中设置request合法域名
    // 或者使用完整的API基础路径
    const requestUrl = 'http://localhost:8082/user/transactions';
    
    wx.request({
      url: requestUrl,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${token}`, // 后端JWT认证要求添加Bearer前缀
        'Content-Type': 'application/json'
      },
      data: {
        page: this.data.page,
        pageSize: this.data.pageSize,
        // 只有当selectedType不为空字符串时才传递transactionType参数
        ...(this.data.selectedType ? { transactionType: this.data.selectedType } : {})
      },
      success: (res) => {
        console.log('原始API响应:', res);
        
        // 检查响应状态码
        if (res.statusCode === 200) {
          // 处理响应数据 - 按照MyBatis Plus的Page分页对象格式
          const responseData = res.data;
          console.log('响应数据内容:', responseData);
          
          // 检查data对象和records字段是否存在且为数组
          if (responseData && responseData.data && Array.isArray(responseData.data.records)) {
            const records = responseData.data.records;
            const total = responseData.data.total || 0;
            
                    // 格式化交易记录
            const transactions = records.map(item => {
              const isIncome = item.transactionType === 'INCOME';
              return {
                ...item,
                amount: item.amount ? parseFloat(item.amount).toFixed(2) : '0.00',
                createTime: this.formatTime(item.createTime),
                typeText: isIncome ? '收入' : '支出',
                title: item.description || (isIncome ? '收入' : '支出'),
                type: isIncome ? 'income' : 'expense' // 适配WXML中的类型判断
              };
            });
            
            const newList = isLoadMore ? [...this.data.transactionList, ...transactions] : transactions;
            
            this.setData({
              transactionList: newList,
              total: total,
              hasMore: newList.length < total,
              loading: false
            });
            
            console.log('成功处理交易记录，共', newList.length, '条，总记录数:', total);
          } else {
            console.error('数据格式错误，缺少必要的records字段:', responseData);
            wx.showToast({
              title: '获取交易记录失败，数据格式异常',
              icon: 'none',
              duration: 2000
            });
            this.setData({ loading: false });
          }
        } else {
          console.error('API请求失败，状态码:', res.statusCode);
          wx.showToast({
            title: '请求失败，请重试',
            icon: 'none'
          });
          this.setData({ loading: false });
        }
      },
      fail: (err) => {
        console.error('网络请求失败:', err);
        wx.showToast({
          title: '网络错误，请检查网络',
          icon: 'none'
        });
        this.setData({ loading: false });
      }
    });
  },

  // 格式化时间 - 按照API要求的格式处理
  formatTime: function(timeStr) {
    if (!timeStr) return '';
    try {
      // 尝试直接格式化时间字符串，避免时间戳解析问题
      if (typeof timeStr === 'string') {
        // 如果已经是yyyy-MM-dd HH:mm:ss格式，只保留到分钟
        if (timeStr.includes(' ')) {
          return timeStr.split(':').slice(0, 2).join(':');
        }
      }
      
      const date = new Date(timeStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hour}:${minute}`;
    } catch (e) {
      console.error('时间格式化错误:', e);
      return timeStr;
    }
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    this.loadTransactions(false);
    wx.stopPullDownRefresh();
  },

  // 切换交易类型
  switchTransactionType: function(e) {
    const type = e.currentTarget.dataset.type;
    if (type !== this.data.selectedType) {
      this.setData({
        selectedType: type,
        page: 1,
        transactionList: [],
        loading: true
      });
      this.loadTransactions(false);
    }
  },

  // 加载更多
  onReachBottom: function() {
    if (!this.data.loading && this.data.hasMore) {
      this.setData({
        page: this.data.page + 1,
        loading: true
      });
      this.loadTransactions(true);
    }
  }
});