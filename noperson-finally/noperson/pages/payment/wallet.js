const { api } = require('../../utils/request');

Page({
  data: {
    userInfo: {},
    balance: 0.00,
    rechargeAmount: '',
    withdrawAmount: '',
    loading: false,
    showRechargeModal: false,
    showWithdrawModal: false
  },

  onLoad: function() {
    this.loadUserInfoAndBalance();
  },

  onShow: function() {
    // 每次页面显示时刷新用户信息和余额
    this.loadUserInfoAndBalance();
  },

  // 加载用户信息和余额
  loadUserInfoAndBalance: function() {
    this.setData({
      loading: true
    });

    // 同时获取用户信息和余额
    Promise.all([
      api.getUserInfo(),
      api.getUserBalance()
    ]).then(results => {
      const userInfo = results[0];
      const balanceInfo = results[1];

      console.log('获取用户信息成功:', userInfo);
      console.log('获取用户余额成功:', balanceInfo);
      console.log('余额数据类型:', typeof balanceInfo);

      // 安全处理余额数据
      let balance = 0.00;
      // 尝试各种可能的数据结构
      if (typeof balanceInfo === 'object' && balanceInfo !== null) {
        if (balanceInfo.data !== undefined) {
          if (typeof balanceInfo.data === 'object' && balanceInfo.data.balance !== undefined) {
            balance = parseFloat(balanceInfo.data.balance) || 0.00;
          } else {
            balance = parseFloat(balanceInfo.data) || 0.00;
          }
        } else if (balanceInfo.balance !== undefined) {
          balance = parseFloat(balanceInfo.balance) || 0.00;
        }
      } else {
        balance = parseFloat(balanceInfo) || 0.00;
      }
      
      console.log('最终处理后的余额:', balance);

      this.setData({
        userInfo: userInfo || {},
        // 将余额格式化为两位小数
          balance: parseFloat(balance).toFixed(2)
      });
    }).catch(error => {
      console.error('获取用户信息或余额失败:', error);
      wx.showToast({
        title: '获取信息失败',
        icon: 'none'
      });
      // 出错时也设置默认余额，确保页面正常显示
      this.setData({
        balance: 0.00
      });
    }).finally(() => {
      this.setData({
        loading: false
      });
    });
  },

  // 打开充值弹窗
  openRechargeModal: function() {
    this.setData({
      rechargeAmount: '',
      showRechargeModal: true
    });
  },

  // 打开提现弹窗
  openWithdrawModal: function() {
    this.setData({
      withdrawAmount: '',
      showWithdrawModal: true
    });
  },

  // 关闭充值弹窗
  closeRechargeModal: function() {
    this.setData({
      showRechargeModal: false
    });
  },

  // 关闭提现弹窗
  closeWithdrawModal: function() {
    this.setData({
      showWithdrawModal: false
    });
  },

  // 输入充值金额
  onRechargeAmountInput: function(e) {
    this.setData({
      rechargeAmount: e.detail.value
    });
  },

  // 输入提现金额
  onWithdrawAmountInput: function(e) {
    this.setData({
      withdrawAmount: e.detail.value
    });
  },

  // 确认充值
  confirmRecharge: function() {
    const amount = parseFloat(this.data.rechargeAmount);

    if (isNaN(amount) || amount <= 0) {
      wx.showToast({
        title: '请输入有效的充值金额',
        icon: 'none'
      });
      return;
    }

    this.setData({
      loading: true
    });

    api.rechargeUserBalance({ amount: amount })
      .then(res => {
        console.log('充值成功:', res);
        wx.showToast({
          title: '充值成功',
          icon: 'success'
        });
        this.closeRechargeModal();
        // 充值成功后刷新余额
        this.loadUserInfoAndBalance();
      })
      .catch(error => {
        console.error('充值失败:', error);
        wx.showToast({
          title: '充值失败，请重试',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({
          loading: false
        });
      });
  },

  // 确认提现
  confirmWithdraw: function() {
    const amount = parseFloat(this.data.withdrawAmount);

    if (isNaN(amount) || amount <= 0) {
      wx.showToast({
        title: '请输入有效的提现金额',
        icon: 'none'
      });
      return;
    }

    if (amount > this.data.balance) {
      wx.showToast({
        title: '提现金额不能大于余额',
        icon: 'none'
      });
      return;
    }

    this.setData({
      loading: true
    });

    api.withdrawUserBalance({ amount: amount })
      .then(res => {
        console.log('提现成功:', res);
        wx.showToast({
          title: '提现成功',
          icon: 'success'
        });
        this.closeWithdrawModal();
        // 提现成功后刷新余额
        this.loadUserInfoAndBalance();
      })
      .catch(error => {
        console.error('提现失败:', error);
        wx.showToast({
          title: '提现失败，请重试',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({
          loading: false
        });
      });
  },

  // 返回上一页
  navigateBack: function() {
    wx.navigateBack();
  }
})