// publish-spray-demand.js
const { api } = require('../../utils/request');
const tmap = require('../../utils/tmap.js');
const app = getApp();
const AddressStorage = require('../../utils/address-storage');

Page({
  data: {
    landId: '',
    landName: '',
    landBoundary: '',
    cropType: '',
    pestType: '',
    landLocation: '',
    landArea: '',
    expectedTime: '',
    budget: '',
    specialRequirements: '',
    loading: false,
    cropTypes: ['小麦', '水稻', '玉米', '大豆', '棉花', '蔬菜', '水果', '其他'],
    // 表单错误状态
    errors: {
      landName: '',
      landBoundary: '',
      cropType: '',
      pestType: '',
      landLocation: '',
      landArea: '',
      expectedTime: '',
      budget: '',
      specialRequirements: ''
    },
    // 字数统计
    charCount: {
      landName: 0,
      specialRequirements: 0
    },
    // 表单验证状态
    fieldFocus: {},
    // 今天和最大日期
    today: '',
    maxDate: ''
  },
  
  // 防抖函数
  debounce(fn, delay = 300) {
    let timer = null;
    return function(...args) {
      if (timer) clearTimeout(timer);
      timer = setTimeout(() => {
        fn.apply(this, args);
      }, delay);
    };
  },
  
  onLoad: function(options) {
    // 设置日期范围
    const now = new Date();
    const today = this.formatDate(now);
    
    // 设置默认日期为明天
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const defaultDate = this.formatDate(tomorrow);
    
    // 最大日期为1年后
    const maxDateObj = new Date();
    maxDateObj.setFullYear(maxDateObj.getFullYear() + 1);
    const maxDate = this.formatDate(maxDateObj);
    
    this.setData({
      expectedTime: defaultDate + 'T09:30:00',
      today: today + 'T00:00:00',
      maxDate: maxDate + 'T23:59:59'
    });
    
    // 初始化防抖验证函数
    this.debouncedValidateLandName = this.debounce(this.validateLandName, 500);
    this.debouncedValidateLandArea = this.debounce(this.validateLandArea, 500);
    this.debouncedValidateBudget = this.debounce(this.validateBudget, 500);
    
    // 检查是否有常用地址
    this.checkFavoriteAddress();
    
    // 检查是否有从病虫害识别传递过来的参数
    if (options && (options.cropType || options.pestType || options.specialRequirements)) {
      this.fillFormWithDiagnosisResult(options);
    }
  },
  
  // 检查是否有常用地址
  checkFavoriteAddress: async function() {
    const orderType = 1; // 喷洒需求
    const favoriteAddress = await AddressStorage.getAddressByType(orderType);
    
    if (favoriteAddress) {
      wx.showModal({
        title: '使用常用地址',
        content: '检测到您有常用地址，是否使用常用地址填写表单？',
        success: (res) => {
          if (res.confirm) {
            this.fillFormWithFavoriteAddress(favoriteAddress);
          }
        }
      });
    }
  },
  
  // 使用常用地址填写表单
  fillFormWithFavoriteAddress: function(address) {
    this.setData({
      landName: address.landName || '',
      landBoundary: address.landBoundary || '',
      landLocation: address.landLocation || '',
      landArea: address.landArea || '',
      cropType: address.cropType || '',
      pestType: address.pestType || ''
    });
    
    // 清除相关错误
    this.clearError('landName');
    this.clearError('landBoundary');
    this.clearError('landLocation');
    this.clearError('landArea');
    this.clearError('cropType');
    this.clearError('pestType');
    
    wx.showToast({
      title: '已使用常用地址填写',
      icon: 'success',
      duration: 1500
    });
  },
  
  // 格式化日期
  formatDate: function(date) {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  },
  
  // 清除指定字段的错误信息
  clearError(field) {
    if (this.data.errors[field]) {
      const errors = {...this.data.errors};
      errors[field] = '';
      this.setData({ errors });
    }
  },
  
  // 设置字段错误信息
  setError(field, message) {
    const errors = {...this.data.errors};
    errors[field] = message;
    this.setData({ errors });
  },
  
  // 处理输入框聚焦
  onFieldFocus(e) {
    const field = e.currentTarget.dataset.field;
    const fieldFocus = {...this.data.fieldFocus};
    fieldFocus[field] = true;
    this.setData({ fieldFocus });
  },
  
  // 处理输入框失焦
  onFieldBlur(e) {
    const field = e.currentTarget.dataset.field;
    const fieldFocus = {...this.data.fieldFocus};
    fieldFocus[field] = false;
    this.setData({ fieldFocus });
    
    // 失焦时验证
    switch(field) {
      case 'landName':
        this.validateLandName();
        break;
      case 'landArea':
        this.validateLandArea();
        break;
      case 'budget':
        this.validateBudget();
        break;
      case 'pestType':
        this.validatePestType();
        break;
    }
  },
  
  // 输入地块ID - 增强验证，只允许数字输入
  onLandIdInput: function(e) {
    // 只允许输入数字字符，且限制长度
    const value = e.detail.value.replace(/[^0-9]/g, '').slice(0, 20);
    this.setData({
      landId: value
    });
  },
  
  // 验证地块名称
  validateLandName() {
    const { landName } = this.data;
    if (!landName) {
      this.setError('landName', '请输入地块名称');
      return false;
    }
    if (landName.length > 100) {
      this.setError('landName', '地块名称不能超过100个字符');
      return false;
    }
    this.clearError('landName');
    return true;
  },
  
  // 输入地块名称
  onLandNameInput: function(e) {
    const value = e.detail.value;
    const charCount = {...this.data.charCount};
    charCount.landName = value.length;
    
    this.setData({
      landName: value,
      charCount
    });
    
    // 实时验证（防抖）
    this.debouncedValidateLandName();
  },
  
  // 验证地块边界
  validateLandBoundary() {
    const { landBoundary } = this.data;
    if (!landBoundary) {
      this.setError('landBoundary', '请输入地块边界');
      return false;
    }
    this.clearError('landBoundary');
    return true;
  },
  
  // 输入地块边界
  onLandBoundaryInput: function(e) {
    const value = e.detail.value;
    this.setData({
      landBoundary: value
    });
    
    // 输入时清除错误
    this.clearError('landBoundary');
  },
  
  // 验证作物类型
  validateCropType() {
    const { cropType } = this.data;
    if (!cropType) {
      this.setError('cropType', '请选择作物类型');
      return false;
    }
    this.clearError('cropType');
    return true;
  },
  
  // 选择作物类型
  onCropTypeChange: function(e) {
    const index = e.detail.value;
    this.setData({
      cropType: this.data.cropTypes[index]
    });
    this.validateCropType();
  },
  
  // 验证病虫害类型
  validatePestType() {
    const { pestType } = this.data;
    if (!pestType) {
      this.setError('pestType', '请输入病虫害类型');
      return false;
    }
    if (pestType.length > 100) {
      this.setError('pestType', '病虫害类型不能超过100个字符');
      return false;
    }
    this.clearError('pestType');
    return true;
  },
  
  // 输入病虫害类型
  onPestTypeInput: function(e) {
    const value = e.detail.value;
    this.setData({
      pestType: value
    });
    
    // 输入时清除错误
    if (value.length <= 100) {
      this.clearError('pestType');
    }
  },
  
  // 验证地块位置
  validateLandLocation() {
    const { landLocation } = this.data;
    if (!landLocation) {
      this.setError('landLocation', '请选择地块位置');
      return false;
    }
    this.clearError('landLocation');
    return true;
  },
  

  
  // 验证地块面积
  validateLandArea() {
    const { landArea } = this.data;
    if (!landArea) {
      this.setError('landArea', '请输入地块面积');
      return false;
    }
    if (isNaN(landArea) || parseFloat(landArea) <= 0) {
      this.setError('landArea', '请输入有效的地块面积（必须大于0）');
      return false;
    }
    if (parseFloat(landArea) > 99999) {
      this.setError('landArea', '地块面积不能超过99999亩');
      return false;
    }
    this.clearError('landArea');
    return true;
  },
  
  // 输入地块面积
    onLandAreaInput: function(e) {
      // 只允许输入数字和小数点
      let value = e.detail.value;
      // 确保只有一个小数点
      value = value.replace(/[^\d.]/g, '');
      if (value.indexOf('.') !== -1) {
        // 限制小数位数为2位
        const parts = value.split('.');
        if (parts[1] && parts[1].length > 2) {
          value = parts[0] + '.' + parts[1].substring(0, 2);
        }
      }
      
      this.setData({
        landArea: value
      });
      
      // 实时验证（防抖）
      this.debouncedValidateLandArea();
      
      // 自动计算预算（固定5元/亩）
      this.autoCalculateBudget();
    },
  
  // 验证期望时间
  validateExpectedTime() {
    const { expectedTime } = this.data;
    if (!expectedTime) {
      this.setError('expectedTime', '请选择期望时间');
      return false;
    }
    const expectedDate = new Date(expectedTime);
    const now = new Date();
    if (expectedDate <= now) {
      this.setError('expectedTime', '期望时间必须为未来时间');
      return false;
    }
    this.clearError('expectedTime');
    return true;
  },
  
  // 选择期望时间
  onDateTimeChange: function(e) {
    this.setData({
      expectedTime: e.detail.value
    });
    this.validateExpectedTime();
  },
  
  // 验证预算金额
  validateBudget() {
    const { budget } = this.data;
    if (!budget) {
      this.setError('budget', '请输入预算金额');
      return false;
    }
    if (isNaN(budget) || parseFloat(budget) <= 0) {
      this.setError('budget', '请输入有效的预算金额（必须大于0）');
      return false;
    }
    if (parseFloat(budget) > 999999) {
      this.setError('budget', '预算金额不能超过999999元');
      return false;
    }
    this.clearError('budget');
    return true;
  },
  
  // 输入预算金额
  onBudgetInput: function(e) {
    // 只允许输入数字和小数点
    let value = e.detail.value;
    // 确保只有一个小数点
    value = value.replace(/[^\d.]/g, '');
    if (value.indexOf('.') !== -1) {
      // 限制小数位数为2位
      const parts = value.split('.');
      if (parts[1] && parts[1].length > 2) {
        value = parts[0] + '.' + parts[1].substring(0, 2);
      }
    }
    
    this.setData({
      budget: value
    });
    
    // 实时验证（防抖）
    this.debouncedValidateBudget();
  },
  
  // 验证特殊要求
  validateSpecialRequirements() {
    const { specialRequirements } = this.data;
    if (specialRequirements && specialRequirements.length > 500) {
      this.setError('specialRequirements', '特殊要求不能超过500个字符');
      return false;
    }
    this.clearError('specialRequirements');
    return true;
  },
  
  // 输入特殊要求
  onSpecialRequirementsInput: function(e) {
    const value = e.detail.value;
    const charCount = {...this.data.charCount};
    charCount.specialRequirements = value.length;
    
    this.setData({
      specialRequirements: value,
      charCount
    });
    
    // 输入时验证
    this.validateSpecialRequirements();
  },
  
  // 使用地图选择地块
  selectLandByMap: function() {
    wx.showModal({
      title: '提示',
      content: '此功能需要调用地图API选择地块边界',
      showCancel: false
    });
    // 实际项目中应该调用wx.chooseLocation或自定义地图组件
  },
  
  // 表单验证
  validateForm: function() {
    // 执行所有字段的验证
    const validations = [
      this.validateLandName(),
      this.validateLandBoundary(),
      this.validateCropType(),
      this.validatePestType(),
      this.validateLandLocation(),
      this.validateLandArea(),
      this.validateExpectedTime(),
      this.validateBudget(),
      this.validateSpecialRequirements()
    ];
    
    // 检查是否有错误
    const hasErrors = validations.some(valid => !valid);
    
    if (hasErrors) {
      // 滚动到第一个错误的输入框
      const firstErrorField = Object.keys(this.data.errors).find(field => this.data.errors[field]);
      if (firstErrorField) {
        this.scrollToField(firstErrorField);
      }
      
      // 显示总体提示
      wx.showToast({
        title: '请完善必填信息',
        icon: 'none',
        duration: 2000
      });
      
      return false;
    }
    
    return true;
  },
  
  // 滚动到指定字段
  scrollToField(field) {
    const query = wx.createSelectorQuery();
    query.select(`[data-field="${field}"]`).boundingClientRect();
    query.selectViewport().scrollOffset();
    query.exec(res => {
      if (res[0] && res[1]) {
        const fieldTop = res[0].top;
        const scrollTop = res[1].scrollTop;
        const windowHeight = wx.getSystemInfoSync().windowHeight;
        
        // 计算滚动位置，确保字段在视口中上方区域
        const targetScrollTop = scrollTop + fieldTop - windowHeight / 4;
        
        wx.pageScrollTo({
          scrollTop: Math.max(0, targetScrollTop),
          duration: 300
        });
      }
    });
  },
  
  // 重置表单
  resetForm() {
    wx.showModal({
      title: '确认重置',
      content: '确定要清除所有已填写的内容吗？',
      success: res => {
        if (res.confirm) {
          // 设置默认日期为明天
          const tomorrow = new Date();
          tomorrow.setDate(tomorrow.getDate() + 1);
          const defaultDate = this.formatDate(tomorrow);
          
          this.setData({
            landId: '',
            landName: '',
            landBoundary: '',
            cropType: '',
            pestType: '',
            landLocation: '',
            landArea: '',
            expectedTime: defaultDate + 'T09:30:00',
            budget: '',
            specialRequirements: '',
            errors: {
              landName: '',
              landBoundary: '',
              cropType: '',
              pestType: '',
              landLocation: '',
              landArea: '',
              expectedTime: '',
              budget: '',
              specialRequirements: ''
            },
            charCount: {
              landName: 0,
              specialRequirements: 0
            }
          });
          
          wx.showToast({
            title: '表单已重置',
            icon: 'success',
            duration: 1500
          });
        }
      }
    });
  },
  
  // 自动计算预算（固定5元/亩）
  autoCalculateBudget() {
    if (!this.data.landArea) {
      this.setData({ budget: '' });
      return;
    }
    
    // 固定单价：5元/亩
    const unitPrice = 5;
    const area = parseFloat(this.data.landArea);
    const calculatedBudget = (area * unitPrice).toFixed(2);
    
    // 自动设置预算，不需要用户确认
    this.setData({ budget: calculatedBudget });
    this.validateBudget();
  },
  
  // 提交需求
  submitDemand: async function() {
    // 使用整合的表单验证逻辑
    if (!this.validateForm()) {
      return;
    }
    
    const { landId, landName, landBoundary, cropType, pestType, landLocation, landArea, expectedTime, budget, specialRequirements } = this.data;
    
    this.setData({
      loading: true
    });
    
    try {
      // 构建符合数据库demand表结构的请求参数
      const data = {
        orderType: 1, // 1-喷洒（固定值）
        landName: landName,
        landBoundary: landBoundary,
        cropType: cropType,
        pestType: pestType, // 喷洒订单特有且必填
        landLocation: landLocation,
        landArea: parseInt(landArea), // 数据库为bigint类型
        expectedTime: expectedTime,
        budget: parseFloat(budget), // 数据库为decimal类型
        status: 0, // 新建需求默认状态为待接取(0)
        paymentStatus: 0, // 默认未支付
        // 可选参数
        landId: landId && !isNaN(parseInt(landId)) ? parseInt(landId) : undefined,
        specialRequirements: specialRequirements
        // farmerId由后端自动填充，前端无需传递
      };
      
      console.log('提交的喷洒需求参数:', data);
      
      // 实际调用API发布需求 - 使用新的统一订单创建接口
      const res = await api.createOrderDemand(data);
      
      console.log('发布需求成功，返回结果:', res);
      
      wx.showToast({
        title: '需求发布成功',
        icon: 'success'
      });
      
      // 询问是否保存为常用地址
      this.askSaveFavoriteAddress();
      
      // 发布成功后直接跳转到农户首页
      setTimeout(() => {
        wx.redirectTo({
          url: '/pages/farmer/index'
        });
      }, 1500);
      
    } catch (error) {
      console.error('发布需求失败', error);
      console.error('错误详情:', error.originalError || '无原始错误信息');
      console.error('提交的参数:', error.data || '无法获取提交参数');
      
      // 增强错误处理，提供更具体的错误提示
      let errorMessage = '发布需求失败，请重试';
      let isPotentialSuccess = false;
      
      // 优先显示原始错误的具体信息
      if (error && error.originalError && error.originalError.message) {
        errorMessage = error.originalError.message;
      } else if (error && error.message) {
        errorMessage = error.message;
      } else if (error && error.errMsg) {
        errorMessage = error.errMsg;
      } else if (error && typeof error === 'string') {
        errorMessage = error;
      }
      
      // 检查错误响应内容，判断是否为潜在成功场景
      // 针对'需求不存在'的500错误做特殊处理，因为数据库可能已插入数据
      if ((errorMessage.includes('500') && errorMessage.includes('需求不存在')) || 
          (error.originalError && error.originalError.message && 
           error.originalError.message.includes('500') && 
           error.originalError.message.includes('需求不存在'))) {
        // 特殊处理：根据用户反馈，这种情况下数据库可能已经成功插入数据
        errorMessage = '需求已提交，请查看我的需求列表';
        isPotentialSuccess = true;
      } else if (errorMessage.includes('500')) {
        errorMessage = '服务器处理请求失败，请稍后重试';
      } else if (errorMessage.includes('400')) {
        errorMessage = '提交的参数有误，请检查并重新提交';
      }
      
      wx.showToast({
        title: errorMessage,
        icon: isPotentialSuccess ? 'success' : 'none',
        duration: 3000
      });
      
      // 如果是潜在成功的情况，1.5秒后跳转到农户首页
      if (isPotentialSuccess) {
        setTimeout(() => {
          wx.redirectTo({
            url: '/pages/farmer/index'
          });
        }, 1500);
      }
    } finally {
      this.setData({
        loading: false
      });
    }
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
          landLocation: locationInfo.address,
          longitude: locationInfo.longitude,
          latitude: locationInfo.latitude
        });

        // 验证位置
        this.validateLandLocation();

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
  },
  
  // 询问是否保存为常用地址
  askSaveFavoriteAddress: async function() {
    const orderType = 1; // 喷洒需求
    
    // 检查是否已有常用地址
    const hasAddress = await AddressStorage.hasAddress(orderType);
    if (!hasAddress) {
      wx.showModal({
        title: '保存常用地址',
        content: '是否将当前地址保存为常用地址？保存后下次发布需求时可以一键填写。',
        success: async (res) => {
          if (res.confirm) {
            await this.saveAsFavoriteAddress(orderType);
          }
        }
      });
    }
  },
  
  // 保存为常用地址
  saveAsFavoriteAddress: async function(orderType) {
    const { landName, landBoundary, landLocation, landArea, cropType, pestType } = this.data;
    
    const address = {
      landName: landName,
      landBoundary: landBoundary,
      landLocation: landLocation,
      landArea: landArea,
      cropType: cropType,
      pestType: pestType
    };
    
    const success = await AddressStorage.saveAddress(orderType, address);
    
    if (success) {
      wx.showToast({
        title: '常用地址已保存',
        icon: 'success',
        duration: 1500
      });
    } else {
      wx.showToast({
        title: '保存失败，请重试',
        icon: 'none',
        duration: 1500
      });
    }
  },
  
  // 使用病虫害识别结果填充表单
  fillFormWithDiagnosisResult: function(options) {
    // 解码URL参数
    const cropType = options.cropType ? decodeURIComponent(options.cropType) : '';
    const pestType = options.pestType ? decodeURIComponent(options.pestType) : '';
    const specialRequirements = options.specialRequirements ? decodeURIComponent(options.specialRequirements) : '';
    
    // 设置表单数据
    this.setData({
      cropType: cropType,
      pestType: pestType,
      specialRequirements: specialRequirements
    });
    
    // 清除相关错误
    this.clearError('cropType');
    this.clearError('pestType');
    
    // 更新字符计数
    const charCount = {...this.data.charCount};
    charCount.specialRequirements = specialRequirements.length;
    this.setData({ charCount });
    
    wx.showToast({
      title: '已自动填充识别结果',
      icon: 'success',
      duration: 1500
    });
  }
})