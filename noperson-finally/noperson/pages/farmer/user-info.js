const { api } = require('../../utils/request');
const tmap = require('../../utils/tmap.js');

Page({
  data: {
    userInfo: {},
    editable: false,
    formData: {
      realName: '',
      avatar: '',
      address: ''
    },
    loading: false,
    serverAvatarUrl: '',
    // 添加表单验证状态
    formErrors: {}
  },
  
  onLoad: function(options) {
    console.log('页面加载，参数:', options);
    
    // 初始化基础数据结构
    this.initDefaultData();
    
    // 加载用户信息
    this.loadUserInfo();
  },
  
  // 初始化默认数据，提供更好的体验和容错性
  initDefaultData: function() {
    const defaultUserInfo = {
      realName: '农户用户',
      username: '未设置',
      phone: '未设置',
      roleType: 1,
      createTime: '未设置',
      lastLoginTime: '未设置',
      avatar: '/assets/images/user-avatar.png',
      address: '未设置'
    };
    
    this.setData({
      editable: false,
      loading: false,
      userInfo: defaultUserInfo,
      formData: {
        realName: defaultUserInfo.realName,
        avatar: defaultUserInfo.avatar,
        address: defaultUserInfo.address
      },
      formErrors: {}
    });
    
    console.log('默认数据初始化完成');
  },
  
  // 加载用户信息 - 优化版
  loadUserInfo: function() {
    wx.showLoading({
      title: '加载中...',
    });
    
    // 使用Promise确保正确的异步流程
    Promise.resolve()
      .then(() => api.getUserInfo())
      .then(res => {
        console.log('获取用户信息成功:', res);
        
        // 安全地获取数据，处理各种可能的响应格式
        const userData = res && res.data ? res.data : {};
        console.log('实际用户数据:', userData);
        
        // 处理并清理用户数据
        const processedData = this.processUserData(userData);
        
        this.setData({
          userInfo: processedData,
          formData: {
            realName: processedData.realName || '',
            avatar: processedData.avatar,
            address: processedData.address
          },
          formErrors: {}
        });
      })
      .catch(error => {
        console.error('获取用户信息失败:', error);
        // 显示更友好的错误信息
        const errorMsg = error && error.message ? `加载失败: ${error.message}` : '获取用户信息失败';
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        });
        
        // 保持使用已有的默认数据，避免覆盖当前显示
        console.log('保持使用默认数据');
      })
      .finally(() => {
        wx.hideLoading();
      });
  },
  
  // 处理和清理用户数据
  processUserData: function(userData) {
    // 创建安全的数据副本
    const safeData = { ...userData };
    
    // 处理头像URL
    safeData.avatar = this.processAvatarUrl(userData.avatar);
    
    // 处理地址字段
    safeData.address = userData.address || '未设置';
    
    // 处理用户名
    safeData.username = userData.username || '未设置';
    
    // 处理手机号
    safeData.phone = userData.phone ? this.formatPhone(userData.phone) : '未设置';
    
    // 处理时间格式
    if (userData.createTime) {
      safeData.createTime = this.formatTime(userData.createTime);
    }
    if (userData.lastLoginTime) {
      safeData.lastLoginTime = this.formatTime(userData.lastLoginTime);
    }
    
    // 确保roleType有效
    safeData.roleType = userData.roleType || 0;
    
    return safeData;
  },
  
  // 处理头像URL
  processAvatarUrl: function(avatarUrl) {
    let url = avatarUrl || '';
    
    // 清理可能的额外字符
    if (url) {
      url = url.trim().replace(/^["']|['"]$/g, '');
    }
    
    // 验证URL是否有效
    if (!url || url === 'null' || url === 'undefined' || !this.isValidUrl(url)) {
      return '/assets/images/user-avatar.png';
    }
    
    // 如果是阿里云OSS的URL，使用代理接口访问
    if (url.includes('aliyuncs.com')) {
      // 获取用户ID
      const app = getApp();
      let userId = app.globalData.userId;
      if (!userId) {
        const userInfo = wx.getStorageSync('userInfo');
        userId = userInfo ? userInfo.userId : null;
      }
      
      if (userId) {
        return `http://localhost:8082/file/proxy/${userId}/avatar`;
      }
    }
    
    return url;
  },
  
  // 简单的URL有效性检查
  isValidUrl: function(url) {
    try {
      // 检查是否是相对路径或完整URL
      return url.startsWith('/') || url.startsWith('http://') || url.startsWith('https://');
    } catch (e) {
      return false;
    }
  },
  
  // 格式化手机号（显示为138****8000格式）
  formatPhone: function(phone) {
    if (!phone || phone.length !== 11) return phone;
    return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
  },
  
  // 格式化时间显示
  formatTime: function(timeStr) {
    if (!timeStr) return timeStr;
    
    // 简单处理ISO格式时间
    if (timeStr.includes('T')) {
      return timeStr.split('T')[0];
    }
    return timeStr;
  },


  
  // 切换编辑状态方法 - 优化版
  toggleEdit: function() {
    console.log('切换编辑状态');
    
    const newEditableState = !this.data.editable;
    
    // 如果从编辑模式切换回查看模式，恢复原始数据
    if (newEditableState === false && this.data.editable === true) {
      wx.showModal({
        title: '确认',
        content: '放弃未保存的修改？',
        success: (res) => {
          if (res.confirm) {
            this.resetFormData();
            this.setData({
              editable: false,
              formErrors: {}
            });
            wx.showToast({
              title: '已退出编辑模式',
              icon: 'success',
              duration: 1500
            });
          }
        }
      });
    } else {
      // 进入编辑模式
      this.setData({
        editable: true,
        formErrors: {}
      });
      wx.showToast({
        title: '已进入编辑模式',
        icon: 'success',
        duration: 1500
      });
    }
  },
  
  // 重置表单数据为用户信息
  resetFormData: function() {
    const { userInfo } = this.data;
    this.setData({
      formData: {
        realName: userInfo.realName || '',
        avatar: userInfo.avatar,
        address: userInfo.address
      },
      serverAvatarUrl: '' // 清除临时头像URL
    });
  },
  
  // 新增：简单的按钮测试方法，不依赖任何API
  simpleTest: function() {
    console.log('===== 简单测试方法被调用 =====');
    wx.showToast({
      title: '按钮点击事件正常！',
      icon: 'success',
      duration: 2000
    });
    console.log('===== 测试完成 =====');
  },
  
  // 测试方法，用于验证点击事件是否正常
  testClick: function() {
    console.log('测试点击事件触发成功！');
    wx.showToast({
      title: '点击事件正常工作',
      icon: 'success',
      duration: 2000
    });
  },
  
  // 输入框内容变化 - 添加实时验证
  onInputChange: function(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    // 更新表单数据
    this.setData({
      [`formData.${field}`]: value
    });
    
    // 清除该字段的错误信息
    if (this.data.formErrors[field]) {
      this.setData({
        [`formErrors.${field}`]: ''
      });
    }
    
    // 对特定字段进行实时验证
    if (field === 'realName') {
      this.validateRealName(value);
    }
  },
  
  // 验证真实姓名
  validateRealName: function(value) {
    let errorMsg = '';
    
    if (!value || !value.trim()) {
      errorMsg = '请输入真实姓名';
    } else if (value.length < 2 || value.length > 10) {
      errorMsg = '姓名长度应在2-10个字符之间';
    }
    
    this.setData({
      'formErrors.realName': errorMsg
    });
    
    return !errorMsg;
  },
  
  // 选择头像
  chooseAvatar: function() {
    const that = this;
    
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        const tempFilePath = res.tempFilePaths[0];
        
        // 上传头像文件
        that.uploadAvatar(tempFilePath);
      },
      fail: function(err) {
        console.error('选择头像失败:', err);
        wx.showToast({
          title: '选择头像失败，请重试',
          icon: 'none'
        });
      }
    });
  },
  
  // 上传头像
  uploadAvatar: function(filePath) {
    wx.showLoading({
      title: '处理中...',
    });
    
    try {
      // 直接使用临时文件路径进行显示，不再保存到本地存储以避免超出存储限制
      // 微信小程序的临时文件在会话期间是有效的
      let displayAvatarUrl = filePath;
      
      // 仅存储临时文件路径用于后续保存
      this.setData({
        [`formData.avatar`]: displayAvatarUrl,
        // 存储临时文件路径用于后续保存
        serverAvatarUrl: filePath
      });
      
      console.log('头像处理成功，显示用URL:', displayAvatarUrl);
      
      wx.showToast({
        title: '头像设置成功',
        icon: 'success',
        duration: 2000
      });
    } catch (error) {
      console.error('处理头像失败:', error);
      // 详细的错误信息展示
      let errorMsg = '处理头像失败';
      if (error && error.errMsg) {
        errorMsg += ': ' + error.errMsg;
      } else if (error && error.message) {
        errorMsg += ': ' + error.message;
      }
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 3000
      });
    } finally {
      // 确保hideLoading总是被调用，与showLoading配对
      wx.hideLoading();
    }
  },
  
  // 保存用户信息 - 优化版
  saveUserInfo: function() {
    const { formData, serverAvatarUrl } = this.data;
    
    // 表单验证
    if (!this.validateForm()) {
      wx.showToast({
        title: '请检查输入信息',
        icon: 'none'
      });
      return;
    }
    
    // 设置loading状态
    this.setData({
      loading: true
    });
    
    wx.showLoading({
      title: '保存中...',
    });
    
    // 创建一个新对象用于保存，移除不必要的字段
    const dataToSave = {
      realName: formData.realName.trim(),
      address: formData.address || '',
      // 处理头像URL（如果有新上传的头像）
      avatar: serverAvatarUrl || formData.avatar
    };
    
    console.log('准备保存的用户信息:', dataToSave);
    
    // 保存逻辑 - 使用 Promise 链确保流程清晰
    Promise.resolve()
      .then(() => {
        // 如果有新头像文件，先上传头像
        console.log('检查是否需要上传头像 - serverAvatarUrl:', serverAvatarUrl);
        if (serverAvatarUrl && (serverAvatarUrl.startsWith('wxfile://') || serverAvatarUrl.startsWith('http://tmp/'))) {
          console.log('开始上传头像到服务器...');
          return this.uploadAvatarToServer(serverAvatarUrl)
            .then(uploadedUrl => {
              dataToSave.avatar = uploadedUrl;
              return api.updateUserInfo(dataToSave);
            });
        }
        // 否则直接更新用户信息
        console.log('直接更新用户信息，不上传头像');
        return api.updateUserInfo(dataToSave);
      })
      .then(res => {
        console.log('更新用户信息成功:', res);
        
        wx.hideLoading();
        wx.showToast({
          title: '信息更新成功',
          icon: 'success',
          duration: 2000
        });
        
        // 清除服务器头像URL缓存
        this.setData({
          serverAvatarUrl: ''
        });
        
        // 延迟重新加载用户信息，确保toast显示完成
        setTimeout(() => {
          this.loadUserInfo();
          this.setData({
            editable: false
          });
        }, 1500);
      })
      .catch(error => {
        console.error('更新用户信息失败:', error);
        wx.hideLoading();
        
        // 显示更具体的错误信息
        const errorMsg = error && error.message 
          ? `保存失败: ${error.message}` 
          : '保存失败，请稍后重试';
        
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        });
      })
      .finally(() => {
        this.setData({
          loading: false
        });
      });
  },
  
  // 表单验证
  validateForm: function() {
    let isValid = true;
    
    // 验证真实姓名
    if (!this.validateRealName(this.data.formData.realName)) {
      isValid = false;
    }
    
    return isValid;
  },
  
  // 上传头像到服务器
  uploadAvatarToServer: function(filePath) {
    return new Promise((resolve, reject) => {
      // 获取存储的 token
      const token = wx.getStorageSync('token');
      
      if (!token) {
        console.error('未找到 token，无法上传头像');
        reject(new Error('未登录'));
        return;
      }
      
      console.log('开始上传头像到服务器:', filePath);
      
      // 调用后端上传接口，wx.uploadFile 会自动处理 formData
      wx.uploadFile({
        url: 'http://localhost:8082/file/upload',
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          console.log('头像上传响应:', res);
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200 && data.data) {
              console.log('头像上传成功，URL:', data.data);
              resolve(data.data);
            } else {
              console.error('头像上传失败:', data.message || data);
              reject(new Error(data.message || '头像上传失败'));
            }
          } catch (e) {
            console.error('解析上传响应失败:', e);
            reject(new Error('头像上传失败'));
          }
        },
        fail: (err) => {
          console.error('头像上传失败:', err);
          reject(new Error('头像上传失败：' + (err.errMsg || '网络错误')));
        }
      });
    });
  },
  
  // 返回上一页
  onBackPress: function() {
    wx.navigateBack();
  },

  // 获取当前位置
  getCurrentLocation: function() {
    wx.showLoading({
      title: '获取位置中...'
    });

    tmap.getCurrentLocation()
      .then(locationInfo => {
        console.log('获取位置成功:', locationInfo);
        wx.hideLoading();

        // 更新表单数据
        this.setData({
          'formData.address': locationInfo.address,
          'formData.longitude': locationInfo.longitude,
          'formData.latitude': locationInfo.latitude
        });

        wx.showToast({
          title: '位置获取成功',
          icon: 'success'
        });
      })
      .catch(error => {
        console.error('获取位置失败:', error);
        wx.hideLoading();
        wx.showToast({
          title: '获取位置失败，请检查定位权限',
          icon: 'none'
        });
      });
  }
});