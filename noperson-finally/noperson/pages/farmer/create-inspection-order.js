// create-inspection-order.js
const { api } = require('../../utils/request');
const tmap = require('../../utils/tmap.js');
const app = getApp();
const AddressStorage = require('../../utils/address-storage');

// 防抖函数
function debounce(fn, delay = 300) {
  let timer = null;
  return function(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
}

Page({
  data: {
    landName: '',
    landBoundary: '',
    cropType: '',
    inspectionPurpose: 1, // 默认值，1-病虫害检测
    selectedPurpose: '病虫害检测', // 默认巡检目的名称
    expectedResolution: '高清',
    landLocation: '',
    landArea: '',
    expectedTime: '',
    budget: '',
    specialRequirements: '',
    loading: false,
    // 新增表单验证相关状态
    errors: {},
    charCount: {
      landName: 0,
      specialRequirements: 0
    },
    fieldFocus: {},
    // 表单选项
    purposeOptions: [
      { id: 1, name: '病虫害检测' },
      { id: 2, name: '作物生长状况评估' },
      { id: 3, name: '土壤状况分析' }
    ],
    resolutionOptions: ['高清', '标清', '超清'],
    cropTypes: ['小麦', '水稻', '玉米', '大豆', '棉花', '蔬菜', '水果', '其他'],
    // 时间范围设置
    today: '',
    maxDate: ''
  },
  
  onLoad: function() {
    // 设置日期范围
    const today = new Date();
    const maxDate = new Date();
    maxDate.setDate(today.getDate() + 365); // 最大可选择一年后
    
    // 设置默认日期为明天
    const tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);
    const defaultDate = this.formatDate(tomorrow);
    
    this.setData({
      expectedTime: defaultDate + 'T09:30:00',
      today: this.formatDate(today),
      maxDate: this.formatDate(maxDate)
    });
    
    // 初始化防抖验证函数
    this.debouncedValidateLandName = debounce(this.validateLandName, 300);
    this.debouncedValidateLandArea = debounce(this.validateLandArea, 300);
    this.debouncedValidateBudget = debounce(this.validateBudget, 300);
    
    // 检查是否有常用地址
    this.checkFavoriteAddress();
  },
  
  // 格式化日期
  formatDate: function(date) {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  },
  
  // 设置错误信息
  setError: function(field, message) {
    this.setData({
      [`errors.${field}`]: message
    });
  },
  
  // 清除错误信息
  clearError: function(field) {
    this.setData({
      [`errors.${field}`]: ''
    });
  },
  
  // 清除所有错误
  clearAllErrors: function() {
    this.setData({
      errors: {}
    });
  },
  
  // 处理字段聚焦
  onFieldFocus: function(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`fieldFocus.${field}`]: true
    });
  },
  
  // 处理字段失焦
  onFieldBlur: function(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`fieldFocus.${field}`]: false
    });
    
    // 失焦时验证字段
    switch(field) {
      case 'landName':
        this.validateLandName();
        break;
      case 'landBoundary':
        this.validateLandBoundary();
        break;
      case 'landLocation':
        this.validateLandLocation();
        break;
      case 'landArea':
        this.validateLandArea();
        break;
      case 'budget':
        this.validateBudget();
        break;
      case 'specialRequirements':
        this.validateSpecialRequirements();
        break;
    }
  },
  
  // 验证地块名称
  validateLandName: function() {
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
  
  // 验证地块边界
  validateLandBoundary: function() {
    const { landBoundary } = this.data;
    
    if (!landBoundary) {
      this.setError('landBoundary', '请输入地块边界');
      return false;
    }
    
    this.clearError('landBoundary');
    return true;
  },
  
  // 验证巡检目的
  validateInspectionPurpose: function() {
    const { inspectionPurpose } = this.data;
    
    if (!inspectionPurpose || inspectionPurpose === 1) { // 默认值为1，需要用户明确选择
      this.setError('inspectionPurpose', '请选择巡检目的');
      return false;
    }
    
    this.clearError('inspectionPurpose');
    return true;
  },
  
  // 验证地块位置
  validateLandLocation: function() {
    const { landLocation } = this.data;
    
    if (!landLocation) {
      this.setError('landLocation', '请输入地块位置');
      return false;
    }
    
    if (landLocation.length > 200) {
      this.setError('landLocation', '地块位置不能超过200个字符');
      return false;
    }
    
    this.clearError('landLocation');
    return true;
  },
  
  // 验证地块面积
  validateLandArea: function() {
    const { landArea } = this.data;
    
    if (!landArea) {
      this.setError('landArea', '请输入地块面积');
      return false;
    }
    
    if (isNaN(landArea) || parseFloat(landArea) <= 0) {
      this.setError('landArea', '请输入有效的地块面积');
      return false;
    }
    
    if (parseFloat(landArea) > 10000) {
      this.setError('landArea', '地块面积不能超过10000亩');
      return false;
    }
    
    this.clearError('landArea');
    return true;
  },
  
  // 验证期望时间
  validateExpectedTime: function() {
    const { expectedTime } = this.data;
    
    if (!expectedTime) {
      this.setError('expectedTime', '请选择期望时间');
      return false;
    }
    
    // 检查期望时间是否为未来时间
    const now = new Date();
    const expectedDateTime = new Date(expectedTime);
    if (expectedDateTime <= now) {
      this.setError('expectedTime', '期望时间必须为未来时间');
      return false;
    }
    
    this.clearError('expectedTime');
    return true;
  },
  
  // 验证预算
  validateBudget: function() {
    const { budget } = this.data;
    
    if (!budget) {
      this.setError('budget', '请输入预算金额');
      return false;
    }
    
    if (isNaN(budget) || parseFloat(budget) <= 0) {
      this.setError('budget', '请输入有效的预算金额');
      return false;
    }
    
    if (parseFloat(budget) > 100000) {
      this.setError('budget', '预算金额不能超过100000元');
      return false;
    }
    
    this.clearError('budget');
    return true;
  },
  
  // 验证特殊要求
  validateSpecialRequirements: function() {
    const { specialRequirements } = this.data;
    
    if (specialRequirements && specialRequirements.length > 500) {
      this.setError('specialRequirements', '特殊要求不能超过500个字符');
      return false;
    }
    
    this.clearError('specialRequirements');
    return true;
  },
  
  // 验证所有字段
  validateForm: function() {
    const validations = [
      this.validateLandName(),
      this.validateLandBoundary(),
      this.validateInspectionPurpose(),
      this.validateLandLocation(),
      this.validateLandArea(),
      this.validateExpectedTime(),
      this.validateBudget(),
      this.validateSpecialRequirements()
    ];
    
    // 查找第一个错误字段并滚动到该位置
    const firstErrorField = Object.keys(this.data.errors)[0];
    if (firstErrorField) {
      const errorElement = wx.createSelectorQuery().select(`[data-field="${firstErrorField}"]`);
      errorElement.boundingClientRect((rect) => {
        if (rect) {
          wx.createSelectorQuery().select('.form-scroll').scrollOffset((scroll) => {
            wx.createSelectorQuery().select('.form-scroll').fields({
              scrollOffset: true,
              scrollView: true
            }, (res) => {
              wx.createSelectorQuery().select('.form-scroll').scrollTo({
                scrollTop: scroll.scrollTop + rect.top - 100,
                duration: 300
              });
            }).exec();
          }).exec();
        }
      }).exec();
    }
    
    return validations.every(v => v === true);
  },
  
  // 输入地块名称
  onLandNameInput: function(e) {
    const value = e.detail.value;
    this.setData({
      landName: value,
      'charCount.landName': value.length
    });
    
    // 防抖验证
    this.debouncedValidateLandName();
  },
  
  // 输入地块边界
  onLandBoundaryInput: function(e) {
    this.setData({
      landBoundary: e.detail.value
    });
  },
  
  // 选择巡检目的
  onPurposeChange: function(e) {
    const index = e.detail.value;
    this.setData({
      inspectionPurpose: this.data.purposeOptions[index].name, // 保存为字符串名称
      selectedPurpose: this.data.purposeOptions[index].name
    });
    this.validateInspectionPurpose();
  },
  
  // 选择作物类型
  onCropTypeChange: function(e) {
    const index = e.detail.value;
    this.setData({
      cropType: this.data.cropTypes[index]
    });
  },
  
  // 输入地块位置
  onLandLocationInput: function(e) {
    this.setData({
      landLocation: e.detail.value
    });
  },
  
  // 输入地块面积
  onLandAreaInput: function(e) {
    const value = e.detail.value;
    this.setData({
      landArea: value
    });
    
    // 防抖验证
    this.debouncedValidateLandArea();
    
    // 自动计算预算（固定5元/亩）
    this.autoCalculateBudget();
  },
  
  // 输入特殊要求
  onSpecialRequirementsInput: function(e) {
    const value = e.detail.value;
    this.setData({
      specialRequirements: value,
      'charCount.specialRequirements': value.length
    });
  },
  
  // 选择期望分辨率
  onResolutionChange: function(e) {
    const index = e.detail.value;
    this.setData({
      expectedResolution: this.data.resolutionOptions[index]
    });
  },
  
  // 选择期望时间
  onDateChange: function(e) {
    this.setData({
      expectedTime: e.detail.value + 'T09:30:00'
    });
    this.validateExpectedTime();
  },
  
  // 输入预算
  onBudgetInput: function(e) {
    const value = e.detail.value;
    this.setData({
      budget: value
    });
    
    // 防抖验证
    this.debouncedValidateBudget();
  },
  
  // 自动估算预算（固定5元/亩）
  autoCalculateBudget: function() {
    const { landArea } = this.data;
    
    if (!landArea) {
      this.setData({ budget: '' });
      return;
    }
    
    // 固定单价：5元/亩
    const unitPrice = 5;
    const area = parseFloat(landArea);
    const estimatedBudget = (area * unitPrice).toFixed(2);
    
    this.setData({
      budget: estimatedBudget
    });
    
    // 自动验证预算
    this.validateBudget();
  },
  
  // 重置表单
  resetForm: function() {
    wx.showModal({
      title: '确认重置',
      content: '确定要重置当前表单吗？所有已填写信息将丢失。',
      success: (res) => {
        if (res.confirm) {
          // 设置默认日期为明天
          const tomorrow = new Date();
          tomorrow.setDate(tomorrow.getDate() + 1);
          const defaultDate = this.formatDate(tomorrow);
          
          this.setData({
            landName: '',
            landBoundary: '',
            cropType: '',
            inspectionPurpose: 1,
            selectedPurpose: '病虫害检测',
            expectedResolution: '高清',
            landLocation: '',
            landArea: '',
            expectedTime: defaultDate + 'T09:30:00',
            budget: '',
            specialRequirements: '',
            errors: {},
            charCount: {
              landName: 0,
              specialRequirements: 0
            },
            fieldFocus: {}
          });
          
          wx.showToast({
            title: '表单已重置',
            icon: 'success'
          });
        }
      }
    });
  },
  
  // 显示地址选择器
  showDistrictPicker: function() {
    // 在实际项目中应该调用地址选择器组件
    wx.showToast({
      title: '地址选择器功能开发中',
      icon: 'none'
    });
  },
  
  // 处理地址选择确认
  handleAddressConfirm: function(e) {
    const addressInfo = e.detail;
    // 组装地址信息
    const fullAddress = `${addressInfo.province}${addressInfo.city}${addressInfo.area}${addressInfo.detail || ''}`;
    this.setData({
      landLocation: fullAddress
    });
    this.validateLandLocation();
  },
  
  // 处理地址选择取消
  handleAddressCancel: function() {
    console.log('地址选择已取消');
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
  
  // 提交订单
  submitOrder: async function() {
    // 先清除之前的错误信息
    this.clearAllErrors();
    
    // 表单验证
    if (!this.validateForm()) {
      wx.showToast({
        title: '表单存在错误，请检查后再提交',
        icon: 'none'
      });
      return;
    }
    
    const { landName, landBoundary, cropType, inspectionPurpose, expectedResolution, landLocation, landArea, expectedTime, budget, specialRequirements } = this.data;
    
    // 添加支付确认对话框
    wx.showModal({
      title: '支付确认',
      content: `创建需求时将自动从您的账户中扣除 ${budget} 元作为订单预算。确认创建并支付吗？`,
      success: async (res) => {
        if (res.confirm) {
          // 用户确认支付，继续创建订单
          this.setData({
            loading: true
          });
          
          try {
            // 转换分辨率为cm单位
            let resolutionCm = 5; // 默认5cm
            switch(expectedResolution) {
              case '高清':
                resolutionCm = 5;
                break;
              case '标清':
                resolutionCm = 10;
                break;
              case '超清':
                resolutionCm = 3;
                break;
            }
            
            // 构建符合接口规范的请求参数
            const data = {
              orderType: 2, // 2-巡检
              landName: landName,
              landBoundary: landBoundary,
              cropType: cropType,
              landLocation: landLocation,
              landArea: parseFloat(landArea), // 地块面积（亩）
              expectedTime: expectedTime, // ISO-8601格式
              budget: parseFloat(budget), // 预算金额（元）
              inspectionPurpose: inspectionPurpose, // 巡检订单特有
              expectedResolution: resolutionCm.toString(), // 期望分辨率（cm），转换为字符串类型
              specialRequirements: specialRequirements || '' // 可选参数
            };
            
            console.log('提交的巡检订单参数:', data);
            
            // 调用统一订单创建接口
            const res = await api.createOrderDemand(data);
            
            console.log('创建订单成功，返回结果:', res);
            
            // 显示成功消息
            wx.showToast({
              title: res.message || '巡检订单创建成功',
              icon: 'success'
            });
            
            // 询问是否保存为常用地址
            this.askSaveFavoriteAddress();
            
            // 订单创建成功后立即跳转到农户首页
            setTimeout(() => {
              wx.switchTab({
                url: '/pages/farmer/index',
                success: function() {
                  console.log('成功跳转至农户首页');
                },
                fail: function(error) {
                  console.error('跳转失败:', error);
                  // 如果switchTab失败，尝试使用navigateBack
                  setTimeout(() => {
                    wx.navigateBack({
                      delta: 1
                    });
                  }, 100);
                }
              });
            }, 1500);
            
          } catch (error) {
            console.error('创建订单失败', error);
            // 处理余额不足的特定错误
            const errorMsg = error.message || '创建订单失败，请重试';
            wx.showToast({
              title: errorMsg,
              icon: 'none',
              duration: 3000
            });
            
            // 如果是余额不足，可以提示用户去充值
            if (errorMsg.includes('余额不足')) {
              setTimeout(() => {
                wx.showModal({
                  title: '提示',
                  content: '余额不足，是否前往充值页面？',
                  success: (res) => {
                    if (res.confirm) {
                      wx.navigateTo({
                        url: '/pages/payment/wallet'
                      });
                    }
                  }
                });
              }, 1000);
            }
          } finally {
            this.setData({
              loading: false
            });
          }
        } else if (res.cancel) {
          // 用户取消支付，不创建订单
          console.log('用户取消创建并支付订单');
          wx.showToast({
            title: '已取消创建订单',
            icon: 'none'
          });
        }
      }
    });
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
  
  // 检查是否有常用地址
  checkFavoriteAddress: async function() {
    const orderType = 2; // 巡检需求
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
      inspectionPurpose: address.inspectionPurpose || '',
      selectedPurpose: address.selectedPurpose || '',
      expectedResolution: address.expectedResolution || '高清'
    });
    
    // 清除相关错误
    this.clearError('landName');
    this.clearError('landBoundary');
    this.clearError('landLocation');
    this.clearError('landArea');
    this.clearError('cropType');
    this.clearError('inspectionPurpose');
    
    wx.showToast({
      title: '已使用常用地址填写',
      icon: 'success',
      duration: 1500
    });
  },
  
  // 询问是否保存为常用地址
  askSaveFavoriteAddress: async function() {
    const orderType = 2; // 巡检需求
    
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
    const { landName, landBoundary, landLocation, landArea, cropType, inspectionPurpose, expectedResolution } = this.data;
    
    const address = {
      landName: landName,
      landBoundary: landBoundary,
      landLocation: landLocation,
      landArea: landArea,
      cropType: cropType,
      inspectionPurpose: inspectionPurpose,
      expectedResolution: expectedResolution
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
  }
})