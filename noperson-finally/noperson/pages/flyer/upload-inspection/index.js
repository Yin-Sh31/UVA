// flyer/upload-inspection/index.js
const { api } = require('../../../utils/request');

Page({
  data: {
    // 订单信息
    orderInfo: null,
    orderId: '',
    
    // 表单数据
    inspectionData: {
      growthStatus: '', // 作物生长状态
      pestSituation: '', // 病虫害情况
      suggestions: '', // 建议
      yieldEstimate: '', // 预估产量
      weatherCondition: '' // 天气状况
    },
    
    // 上传的图片列表
    imageList: [],
    
    // 表单验证状态
    formErrors: {},
    
    // 提交状态
    submitting: false
  },
  
  onLoad: function(options) {
    // 获取订单ID
    if (options.orderId) {
      this.setData({
        orderId: options.orderId
      });
      this.getOrderDetail();
    } else {
      wx.showToast({
        title: '订单ID不能为空',
        icon: 'none',
        complete: () => {
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      });
    }
  },
  
  // 获取订单详情
  getOrderDetail: async function() {
    try {
      wx.showLoading({
        title: '加载中...',
      });
      
      const orderDetail = await api.getOrderDetail(this.data.orderId);
      
      this.setData({
        orderInfo: orderDetail
      });
    } catch (error) {
      console.error('获取订单详情失败', error);
      wx.showToast({
        title: '获取订单详情失败',
        icon: 'none',
        complete: () => {
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      });
    } finally {
      wx.hideLoading();
    }
  },
  
  // 输入框值改变
  onInputChange: function(e) {
    const { field } = e.currentTarget.dataset;
    const { value } = e.detail;
    
    this.setData({
      [`inspectionData.${field}`]: value
    });
    
    // 清除该字段的错误提示
    if (this.data.formErrors[field]) {
      const newErrors = { ...this.data.formErrors };
      delete newErrors[field];
      this.setData({
        formErrors: newErrors
      });
    }
  },
  
  // 上传图片
  uploadImage: function() {
    if (this.data.imageList.length >= 9) {
      wx.showToast({
        title: '最多上传9张图片',
        icon: 'none'
      });
      return;
    }
    
    wx.chooseImage({
      count: 9 - this.data.imageList.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const tempFilePaths = res.tempFilePaths;
        const newImageList = [...this.data.imageList];
        
        wx.showLoading({
          title: '上传中...',
        });
        
        try {
          // 上传图片到服务器
          for (let i = 0; i < tempFilePaths.length; i++) {
            const uploadResult = await api.uploadFile(tempFilePaths[i]);
            newImageList.push(uploadResult.url);
          }
          
          this.setData({
            imageList: newImageList
          });
        } catch (error) {
          console.error('上传图片失败', error);
          wx.showToast({
            title: '上传图片失败，请重试',
            icon: 'none'
          });
        } finally {
          wx.hideLoading();
        }
      }
    });
  },
  
  // 预览图片
  previewImage: function(e) {
    const { index } = e.currentTarget.dataset;
    wx.previewImage({
      current: this.data.imageList[index],
      urls: this.data.imageList
    });
  },
  
  // 删除图片
  deleteImage: function(e) {
    const { index } = e.currentTarget.dataset;
    const newImageList = [...this.data.imageList];
    newImageList.splice(index, 1);
    
    this.setData({
      imageList: newImageList
    });
  },
  
  // 表单验证
  validateForm: function() {
    const { inspectionData, imageList } = this.data;
    const errors = {};
    
    if (!inspectionData.growthStatus.trim()) {
      errors.growthStatus = '请描述作物生长状态';
    }
    
    if (!inspectionData.pestSituation.trim()) {
      errors.pestSituation = '请描述病虫害情况';
    }
    
    if (!inspectionData.suggestions.trim()) {
      errors.suggestions = '请提供建议';
    }
    
    if (!imageList.length) {
      errors.imageList = '请至少上传一张巡检照片';
    }
    
    this.setData({
      formErrors: errors
    });
    
    return Object.keys(errors).length === 0;
  },
  
  // 提交巡检报告
  submitInspection: async function() {
    // 表单验证
    if (!this.validateForm()) {
      return;
    }
    
    // 防止重复提交
    if (this.data.submitting) {
      return;
    }
    
    this.setData({
      submitting: true
    });
    
    try {
      const { inspectionData, imageList, orderId } = this.data;
      
      // 构建提交参数
      const submitData = {
        orderId: orderId,
        growthStatus: inspectionData.growthStatus,
        pestSituation: inspectionData.pestSituation,
        suggestions: inspectionData.suggestions,
        yieldEstimate: inspectionData.yieldEstimate ? Number(inspectionData.yieldEstimate) : null,
        weatherCondition: inspectionData.weatherCondition,
        imageUrls: imageList
      };
      
      // 调用提交巡检报告API
      await api.submitInspectionReport(submitData);
      
      wx.hideLoading();
      wx.showToast({
        title: '提交成功',
        icon: 'success',
        duration: 2000
      });
      
      // 延时返回上一页或跳转到订单列表页
      setTimeout(() => {
        wx.navigateTo({
          url: '/pages/flyer/accept-order/index'
        });
      }, 2000);
    } catch (error) {
      console.error('提交巡检报告失败', error);
      wx.hideLoading();
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({
        submitting: false
      });
    }
  }
})