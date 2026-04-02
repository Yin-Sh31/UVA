Page({
  data: {
    historyList: [],
    loading: false,
    userId: null
  },

  onLoad: function (options) {
    // 获取用户ID
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.userId) {
      this.setData({
        userId: userInfo.userId
      });
      this.loadHistory();
    }
  },

  // 加载诊断历史记录
  loadHistory: function () {
    const userId = this.data.userId;
    if (!userId) {
      wx.showToast({
        title: '用户未登录',
        icon: 'none'
      });
      return;
    }

    this.setData({ loading: true });

    wx.request({
      url: 'http://localhost:8082/api/ai/history/user/' + userId,
      method: 'GET',
      success: (res) => {
        console.log('诊断历史接口返回:', res);
        if (res.data && res.data.code === 200) {
          const historyList = res.data.data || [];
          // 格式化时间
          const formattedList = historyList.map(item => {
            return {
              ...item,
              createTime: this.formatDate(item.createTime)
            };
          });
          this.setData({
            historyList: formattedList
          });
        } else {
          wx.showToast({
            title: '加载失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error('加载诊断历史失败:', err);
        wx.showToast({
          title: '网络错误',
          icon: 'none'
        });
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  // 跳转到喷洒需求发布页面
  goToSpray: function (e) {
    const item = e.currentTarget.dataset.item;
    wx.navigateTo({
      url: `/pages/farmer/publish-spray-demand?cropType=${encodeURIComponent(item.cropType)}&pestType=${encodeURIComponent(item.diseaseName)}&pesticide=${encodeURIComponent(item.recommendedPesticide || '')}`
    });
  },

  // 格式化日期
  formatDate: function (dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  },

  // 下拉刷新
  onPullDownRefresh: function () {
    this.loadHistory();
    wx.stopPullDownRefresh();
  }
});
