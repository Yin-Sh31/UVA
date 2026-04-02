const { api } = require('../../../utils/request');
const tmap = require('../../../utils/tmap.js');

Page({
  data: {
    userInfo: {},
    editable: false,
    formData: {
      realName: '',
      avatar: '',
      phone: '',
      skillLevel: '',
      address: '',
      location: '',
      pricePerAcre: 0,
      isFree: 1,
      introduction: ''
    },
    isLoading: false,
    serverAvatarUrl: '',
    showDistrictPicker: false
  },

  onLoad: function() {
    console.log('=== 页面加载 ===');
    
    // 设置页面导航栏返回按钮的点击事件
    wx.setNavigationBarTitle({
      title: '用户信息'
    });
    
    // 获取事件通道，用于接收从个人中心页面传递的用户信息
    const eventChannel = this.getOpenerEventChannel();
    if (eventChannel) {
      console.log('设置事件通道监听器');
      eventChannel.on('userInfoData', (res) => {
        console.log('接收到用户信息:', res.userInfo);
        if (res.userInfo) {
          // 如果通过事件通道接收到用户信息，优先使用
          this.handleReceivedUserInfo(res.userInfo);
        }
      });
    } else {
      console.log('未找到事件通道，继续使用默认加载逻辑');
    }
    
    // 加载用户信息
    this.loadUserInfo();
  },

  // 处理接收到的用户信息
  handleReceivedUserInfo: function(userInfo) {
    console.log('处理用户信息:', userInfo);
    
    // 处理头像URL，确保可以正确加载
    let avatarUrl = userInfo.avatar || '';
    // 清理可能的额外字符（如空格、引号等）
    if (avatarUrl) {
      avatarUrl = avatarUrl.trim().replace(/^["']|['"]$/g, '');
      // 如果是相对路径，添加服务器地址
      if (avatarUrl && !avatarUrl.startsWith('http') && !avatarUrl.startsWith('/assets')) {
        avatarUrl = 'http://localhost:8082' + avatarUrl;
      }
    }
    // 只有在URL为空或明显无效时才使用默认头像
    if (!avatarUrl || avatarUrl === 'null' || avatarUrl === 'undefined') {
      console.log('未设置头像，使用本地默认头像');
      avatarUrl = '/assets/images/user-avatar.png';
    }
    
    // 更新用户信息和表单数据
    this.setData({
      userInfo: {
        ...userInfo,
        avatar: avatarUrl,
        skillLevel: userInfo.skillLevel || '未设置',
        certification: this.getAuditStatusText(userInfo.auditStatus),
        realName: userInfo.userName || userInfo.realName || userInfo.username || '未设置',
        userName: userInfo.userName || userInfo.username || '未设置'
      },
      formData: {
        // 基本信息字段
        userId: userInfo.userId || '',
        username: userInfo.username || '',
        userName: userInfo.userName || '',
        realName: userInfo.userName || userInfo.realName || '',
        phone: userInfo.phone || '',
        avatar: avatarUrl,
        
        // 可编辑字段
        introduction: userInfo.introduction || '',
        skillLevel: userInfo.skillLevel || '',
        location: userInfo.location || '',
        pricePerAcre: userInfo.pricePerAcre || 0,
        isFree: userInfo.isFree !== undefined ? userInfo.isFree : 1,
        
        // 保留原始数据
        ...userInfo
      }
    });
    
    console.log('用户信息处理完成，userInfo:', this.data.userInfo);
    console.log('表单数据处理完成，formData:', this.data.formData);
  },
  
  // 加载用户信息
  loadUserInfo: function() {
    this.setData({
      isLoading: true
    });

    api.getFlyerInfo()
      .then(res => {
        console.log('API返回数据:', res);
        // 处理不同的返回数据结构
        let userData = res;
        if (res && res.data) {
          userData = res.data;
        }
        this.handleReceivedUserInfo(userData);
      })
      .catch(err => {
        console.error('获取飞手详细信息失败:', err);
        wx.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({
          isLoading: false
        });
      });
  },

  // 输入框内容变化处理
  onInputChange: function(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    // 对于数字类型的字段，转换为Number
    const processedValue = field === 'pricePerAcre' ? Number(value) : value;
    
    this.setData({
      [`formData.${field}`]: processedValue
    });
  },
  
  // 选择器变化
  onPickerChange: function(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    // 处理技能等级选择
    if (field === 'skillLevel') {
      const skillLevels = ['喷洒认证', '巡检认证', '全能认证'];
      this.setData({
        [`formData.${field}`]: skillLevels[value]
      });
    } else {
      this.setData({
        [`formData.${field}`]: Number(value)
      });
    }
  },

  // 显示地址选择器
  showDistrictPicker() {
    this.setData({ showDistrictPicker: true });
  },

  // 处理地址选择确认
  handleAddressConfirm(e) {
    const addressData = e.detail;
    this.setData({
      'formData.address': addressData.fullAddress,
      'formData.location': addressData.longitude && addressData.latitude 
        ? `${addressData.longitude},${addressData.latitude}` 
        : this.data.formData.location
    });
  },

  // 处理地址选择取消
  handleAddressCancel() {
    this.setData({ showDistrictPicker: false });
  },

  // 切换编辑状态
  toggleEdit: function() {
    console.log('=== 点击了编辑/取消按钮 ===');
    console.log('当前editable状态:', this.data.editable);
    
    // 直接切换状态
    const newEditableState = !this.data.editable;
    this.setData({
      editable: newEditableState
    });
    
    // 显示一个提示，确认状态已改变
    wx.showToast({
      title: newEditableState ? '已进入编辑模式' : '已退出编辑模式',
      icon: 'success',
      duration: 1500
    });
    
    console.log('更新后的editable状态:', this.data.editable);
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
      // 直接使用临时文件路径进行显示
      let displayAvatarUrl = filePath;
      
      // 仅存储临时文件路径用于后续保存
      this.setData({
        [`formData.avatar`]: displayAvatarUrl,
        // 存储临时文件路径用于后续保存
        serverAvatarUrl: filePath
      });
      
      console.log('头像处理成功，显示用 URL:', displayAvatarUrl);
      
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
      // 确保 hideLoading 总是被调用，与 showLoading 配对
      wx.hideLoading();
    }
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

  // 保存用户信息
  saveUserInfo: function() {
    const { formData } = this.data;
    
    // 验证手机号格式
    if (formData.phone && formData.phone.trim()) {
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(formData.phone.trim())) {
        wx.showToast({
          title: '请输入正确的手机号格式',
          icon: 'none'
        });
        return;
      }
    }
    
    // 位置字段不再验证格式，支持中文输入

    this.setData({
      isLoading: true
    });

    wx.showLoading({
      title: '保存中...'
    });

    // 创建一个新对象用于保存，符合FlyerUpdateDTO要求，只包含提供的非空字段
    const dataToSave = {};
    
    // 根据接口文档，只添加非空字段
    if (formData.location && formData.location.trim()) dataToSave.location = formData.location.trim();
    if (formData.pricePerAcre !== undefined && formData.pricePerAcre >= 0.01) dataToSave.pricePerAcre = formData.pricePerAcre;
    if (formData.isFree !== undefined) dataToSave.isFree = formData.isFree;
    if (formData.skillLevel && formData.skillLevel.trim()) dataToSave.skillLevel = formData.skillLevel.trim();
    if (formData.introduction && formData.introduction.trim()) dataToSave.introduction = formData.introduction.trim();
    if (formData.phone && formData.phone.trim()) dataToSave.phone = formData.phone.trim();
    if (formData.avatar && formData.avatar.trim() && formData.avatar.includes('http')) dataToSave.avatar = formData.avatar.trim();

    console.log('准备保存的飞手信息:', dataToSave);

    // 如果有新头像文件，先上传头像
    const savePromise = (this.data.serverAvatarUrl && !this.data.serverAvatarUrl.includes('http')) 
      ? this.uploadAvatarToServer(this.data.serverAvatarUrl)
          .then(uploadedUrl => {
            dataToSave.avatar = uploadedUrl;
            return api.updateFlyerInfo(dataToSave);
          })
      : api.updateFlyerInfo(dataToSave);

    // 调用新的更新飞手信息接口
    savePromise
      .then(() => {
        console.log('更新飞手信息成功');
        
        // 先隐藏当前的loading
        wx.hideLoading();
        
        wx.showToast({
          title: '信息更新成功',
          icon: 'success'
        });
        
        // 切换回查看模式
        this.setData({
          editable: false
        });
        
        // 延迟重新加载用户信息，确保showToast显示完成
        setTimeout(() => {
          this.loadUserInfo();
        }, 1500);
      })
      .catch(err => {
        console.error('保存飞手信息失败:', err);
        wx.hideLoading();
        wx.showToast({
          title: '保存失败，请重试',
          icon: 'none'
        });
      })
      .finally(() => {
        this.setData({
          isLoading: false
        });
      });
  },

  // 获取技能等级文本
    getSkillLevelText: function(level) {
      switch(level) {
        case 1:
          return '初级飞手';
        case 2:
          return '中级飞手';
        case 3:
          return '高级飞手';
        default:
          return '未设置';
      }
    },
    
    // 获取审核状态文本
    getAuditStatusText: function(status) {
      switch(status) {
        case 0:
          return '待审核';
        case 1:
          return '已认证';
        case 2:
          return '认证失败';
        default:
          return '未认证';
      }
    },
    
    // 返回上一页
    goBack: function() {
      wx.navigateBack();
    },
    
    // 页面卸载时确保导航栈正确
    onUnload: function() {
      // 页面卸载时不需要特别处理，但保留这个钩子以便将来扩展
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
            'formData.location': locationInfo.address,
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