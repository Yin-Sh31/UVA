// login.js
const { api } = require('../../utils/request');

Page({
  data: {
    phone: '',
    password: '',
    code: '',
    role: 'farmer', // 默认农户角色
    selectedRole: 'farmer', // 与WXML绑定的变量名保持一致
    loginType: 'password', // 登录方式：password或code
    showPassword: false,
    loading: false,
    countdown: 0,
    timer: null
  },
  
  onLoad: function() {
    const app = getApp();
    this.setData({
      appName: app.globalData.appName
    });
  },
  
  // 输入手机号
  onPhoneInput: function(e) {
    this.setData({
      phone: e.detail.value
    });
  },
  
  // 输入密码
  onPasswordInput: function(e) {
    this.setData({
      password: e.detail.value
    });
  },
  
  // 切换角色 - WXML中实际使用的方法名
  onRoleSelect: function(e) {
    const role = e.currentTarget.dataset.role;
    this.setData({
      role: role,
      selectedRole: role // 同时更新两个变量保持一致
    });
  },
  
  // 切换密码显示状态
  togglePasswordVisibility: function() {
    this.setData({
      showPassword: !this.data.showPassword
    });
  },
  
  // 切换登录方式
  switchLoginType: function(e) {
    const loginType = e.currentTarget.dataset.type;
    this.setData({
      loginType: loginType,
      password: '',
      code: ''
    });
  },
  
  // 输入验证码
  onCodeInput: function(e) {
    this.setData({
      code: e.detail.value
    });
  },
  
  // 发送验证码
  sendVerificationCode: async function() {
    const { phone, role } = this.data;
    
    // 简单校验
    if (!phone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      });
      return;
    }
    
    // 手机号格式校验
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({
        title: '请输入正确的手机号',
        icon: 'none'
      });
      return;
    }
    
    try {
      // 发送验证码 - 直接传递参数作为URL查询参数
      const res = await api.sendCode(phone, role);
      
      console.log('发送验证码成功', res);
      wx.showToast({
        title: '验证码已发送',
        icon: 'success'
      });
      
      // 开始倒计时
      this.startCountdown();
      
    } catch (error) {
      console.error('发送验证码失败', error);
      wx.showToast({
        title: error.message || '发送失败',
        icon: 'none'
      });
    }
  },
  
  // 开始倒计时
  startCountdown: function() {
    let seconds = 60;
    this.setData({
      countdown: seconds
    });
    
    // 清除之前的定时器
    if (this.data.timer) {
      clearInterval(this.data.timer);
    }
    
    // 设置新的定时器
    const timer = setInterval(() => {
      seconds--;
      if (seconds <= 0) {
        clearInterval(timer);
        this.setData({
          countdown: 0,
          timer: null
        });
      } else {
        this.setData({
          countdown: seconds
        });
      }
    }, 1000);
    
    this.setData({
      timer: timer
    });
  },
  
  // 登录 - 与WXML绑定的方法名保持一致
  onLogin: async function() {
    const { phone, password, code, role, loginType } = this.data;
    
    // 简单校验
    if (!phone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      });
      return;
    }
    
    if (loginType === 'password') {
      if (!password) {
        wx.showToast({
          title: '请输入密码',
          icon: 'none'
        });
        return;
      }
    } else {
      if (!code) {
        wx.showToast({
          title: '请输入验证码',
          icon: 'none'
        });
        return;
      }
    }
    
    this.setData({
      loading: true
    });
    
    try {
      const data = {
        phone: phone,
        role: role,
        loginType: loginType
      };
      
      // 根据登录方式添加对应字段
      if (loginType === 'password') {
        data.password = password;
      } else {
        data.code = code;
      }
      
      const res = await api.login(data);
      
      console.log('登录成功，返回数据：', res);
      
      // 保存用户信息到全局 - token在res.data中，user也在res.data中
      const app = getApp();
      app.setUserInfo(res.data?.token || '', res.data?.user || {}, role);
      
      // 先显示成功提示
      wx.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1500
      });
      
      // 使用setTimeout确保提示显示后再执行跳转
      setTimeout(() => {
        // 根据用户选择的角色直接跳转到对应角色的首页
        // 注意：对于tabBar页面，必须使用wx.switchTab，不能使用wx.navigateTo
        switch(role) {
            case 'admin':
              // 管理员功能开发中
              wx.showToast({
                title: '管理员功能开发中',
                icon: 'none'
              });
              break;
            default:
              // 使用app提供的导航辅助方法
              const homePath = app.getHomePathByRole(role);
              const navigateMethod = app.getNavigateMethodByRole(role);
              
              // 根据获取的跳转方式执行导航
              if (navigateMethod === 'switchTab') {
                wx.switchTab({
                  url: homePath
                });
              } else {
                wx.reLaunch({
                  url: homePath
                });
              }
              break;
          }
      }, 500);
      
    } catch (error) {
      console.error('登录失败', error);
    } finally {
      this.setData({
        loading: false
      });
    }
  },
  
  // 跳转到注册页面
  goToRegister: function() {
    wx.navigateTo({
      url: '/pages/auth/register'
    });
  }
})