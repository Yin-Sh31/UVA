// register.js
const { api } = require('../../utils/request');

Page({
  data: {
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
    realName: '',
    role: 'farmer', // 默认农户角色
    roleIndex: 0, // 默认选中农户（索引0）
    showPassword: false,
    showConfirmPassword: false,
    loading: false,
    passwordRule: '密码需包含数字、字母和特殊字符，长度8-20位',
    agreementChecked: false,
    roleMap: ['farmer', 'flyer'], // 索引对应的角色值（移除管理员和机主）
    roleOptions: ['农户', '飞手'] // 角色显示选项（移除管理员和机主）
  },
  
  // 输入用户名
  onUsernameInput: function(e) {
    this.setData({
      username: e.detail.value
    });
  },
  
  // 输入密码
  onPasswordInput: function(e) {
    this.setData({
      password: e.detail.value
    });
  },
  
  // 输入确认密码
  onConfirmPasswordInput: function(e) {
    this.setData({
      confirmPassword: e.detail.value
    });
  },
  
  // 输入手机号
  onPhoneInput: function(e) {
    this.setData({
      phone: e.detail.value
    });
  },
  
  // 输入真实姓名
  onRealNameInput: function(e) {
    this.setData({
      realName: e.detail.value
    });
  },
  
  // 切换角色
  onRoleChange: function(e) {
    const roleIndex = e.detail.value;
    const role = this.data.roleMap[roleIndex];
    this.setData({
      roleIndex: roleIndex,
      role: role
    });
  },
  
  // 切换密码显示状态
  togglePasswordVisibility: function() {
    this.setData({
      showPassword: !this.data.showPassword
    });
  },
  
  // 切换确认密码显示状态
  toggleConfirmPasswordVisibility: function() {
    this.setData({
      showConfirmPassword: !this.data.showConfirmPassword
    });
  },
  
  // 切换协议勾选状态
  toggleAgreement: function() {
    this.setData({
      agreementChecked: !this.data.agreementChecked
    });
  },
  
  // 校验密码格式
  validatePassword: function(password) {
    // 包含数字、字母和特殊字符，长度8-20位
    const pattern = /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,20}$/;
    return pattern.test(password);
  },
  
  // 注册
  register: async function() {
    const { username, password, confirmPassword, phone, realName, role, agreementChecked } = this.data;
    
    // 简单校验
    if (!username) {
      wx.showToast({
        title: '请输入用户名',
        icon: 'none'
      });
      return;
    }
    
    if (!password) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      });
      return;
    }
    
    if (!this.validatePassword(password)) {
      wx.showToast({
        title: this.data.passwordRule,
        icon: 'none',
        duration: 3000
      });
      return;
    }
    
    if (password !== confirmPassword) {
      wx.showToast({
        title: '两次输入的密码不一致',
        icon: 'none'
      });
      return;
    }
    
    if (!phone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      });
      return;
    }
    
    if (!realName) {
      wx.showToast({
        title: '请输入真实姓名',
        icon: 'none'
      });
      return;
    }
    
    if (!agreementChecked) {
      wx.showToast({
        title: '请阅读并同意用户协议',
        icon: 'none'
      });
      return;
    }
    
    this.setData({
      loading: true
    });
    
    try {
      const data = {
        username: username,
        password: password,
        phone: phone,
        realName: realName,
        role: role
      };
      
      await api.register(data);
      
      wx.showToast({
        title: '注册成功',
        icon: 'success'
      });
      
      // 注册成功后跳转到登录页
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/auth/login'
        });
      }, 1500);
    } catch (error) {
      console.error('注册失败', error);
    } finally {
      this.setData({
        loading: false
      });
    }
  },
  
  // 返回登录页
  goToLogin: function() {
    wx.navigateBack();
  }
})