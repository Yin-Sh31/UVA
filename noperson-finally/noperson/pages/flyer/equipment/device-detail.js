// flyer/equipment/device-detail.js
const { api } = require('../../../utils/request');

Page({
  data: {
    deviceId: '',
    deviceDetail: null,
    loading: true,
    errorMessage: '',
    available: false
  },
  
  onLoad: function(options) {
    if (options.deviceId) {
      this.setData({
        deviceId: options.deviceId
      });
      this.loadDeviceDetail();
    }
  },
  


  // 加载设备详情
  loadDeviceDetail: async function() {
    this.setData({
      loading: true,
      errorMessage: ''
    });
    
    try {
      // 加载设备详情
      const detailResponse = await api.getDeviceDetail(this.data.deviceId);
      // 从response.data.deviceInfo获取设备详情
      const rawDetail = detailResponse.data.deviceInfo || {};
      
      console.log('API返回的原始设备详情:', rawDetail);
      
      // 检查设备可用性 - 增强的可用性检查逻辑
      let available = false;
      
      // 首先检查rental_status，考虑可能的字段名不一致
      const rentalStatus = rawDetail.rental_status !== undefined ? rawDetail.rental_status : 
                          (rawDetail.rentalStatus !== undefined ? rawDetail.rentalStatus : null);
      const flyerId = rawDetail.flyerId !== undefined ? rawDetail.flyerId : 
                      (rawDetail.flyer_id !== undefined ? rawDetail.flyer_id : null);
      
      console.log('设备rental_status原始值:', rentalStatus, '(类型:', typeof rentalStatus, ')');
      console.log('设备flyerId原始值:', flyerId);
      
      // 优先根据rental_status判断设备是否可租借
      // 数据库中rental_status为0表示可租借，1表示已被租借
      if (rentalStatus === 1 || rentalStatus === '1' || rentalStatus === 'rented') {
        available = false;
        console.log('设备rental_status为1，标记为不可租借');
      } else if (rentalStatus === 0 && (flyerId === null || flyerId === undefined || flyerId === '')) {
        available = true;
        console.log('根据rental_status=0和flyerId为空，设备可租借');
      } else {
        // 尝试通过API检查设备可用性
        try {
          const availableResponse = await api.checkDeviceAvailable(this.data.deviceId);
          available = availableResponse.data || false;
          console.log('设备可用性API检查结果:', available);
        } catch (checkError) {
          console.error('检查设备可用性失败', checkError);
        }
      }
      
      // 检查是否由当前用户租借
      let isRentedByMe = false;
      // 当rental_status为1或flyerId有值时，表示设备已被租借
      if ((rentalStatus === 1 || rentalStatus === '1' || rentalStatus === 'rented') || 
          (flyerId !== null && flyerId !== undefined && flyerId !== '')) {
        isRentedByMe = true;
        console.log('设备已被租借，rentalStatus:', rentalStatus, 'flyerId:', flyerId);
      }
      
      console.log('设备ID:', this.data.deviceId);
      console.log('设备状态:', rawDetail.status);
      console.log('设备rental_status:', rentalStatus);
      console.log('设备是否可租借:', available);
      console.log('是否由当前用户租借:', isRentedByMe);
      
      // 字段映射，将API返回的字段名映射到WXML中使用的字段名
      const deviceDetail = {
        // 基本信息映射
        id: rawDetail.deviceId,
        name: rawDetail.deviceName || '无人机',
        type: rawDetail.deviceType || '未知型号',
        model: rawDetail.model || '未知型号',
        available: available,
        rentPrice: rawDetail.rentPrice || 0,
        manufacturer: rawDetail.manufacturer || '--',
        maxLoad: rawDetail.maxLoad || 0,
        maxFlightTime: rawDetail.endurance || 0,
        maxFlightAltitude: rawDetail.maxHeight || 0,
        // 根据API返回的实际数据设置飞行小时
        flightHours: rawDetail.flightHours || 0,
        // 维护信息
        lastMaintenanceDate: rawDetail.lastMaintainTime || '从未维护',
        // 机主信息
        ownerAvatar: '/assets/images/user-avatar.png', // 默认头像
        ownerName: detailResponse.data.ownerInfo?.realName || '未知机主', // 从ownerInfo获取
        ownerPhone: detailResponse.data.ownerInfo?.phone || '未知', // 从ownerInfo获取
        // 维护记录
        maintenanceRecords: detailResponse.data.maintainRecords || [],
        // 最后维护记录
        lastMaintainRecord: detailResponse.data.lastMaintainRecord || null,
        // 检查是否由当前用户租借
        isRentedByMe: isRentedByMe,
        // 显式保存rental_status以便在WXML中使用
        rental_status: rentalStatus,
        // 保留原始数据
        ...rawDetail
      };
      
      this.setData({
        deviceDetail: deviceDetail,
        available: available
      });
    } catch (error) {
      console.error('加载设备详情失败', error);
      this.setData({
        errorMessage: '加载失败，请重试',
        deviceDetail: null
      });
    } finally {
      this.setData({
        loading: false
      });
    }
  },
  
  // 租借设备
  rentDevice: async function() {
    const deviceId = this.data.deviceId;
    const deviceDetail = this.data.deviceDetail;
    
    try {
      // 检查飞手资质状态
      console.log('检查飞手资质状态...');
      const qualificationStatus = await api.getQualificationStatus();
      const status = qualificationStatus.data || 0; // 默认未提交
      
      console.log('飞手资质状态:', status);
      
      // 如果资质未通过（状态不是2），提示用户去完成资质认证
      if (status !== 2) {
        wx.showModal({
          title: '资质未通过',
          content: '您需要先完成飞手资质认证才能租借设备，是否立即去认证？',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({
                url: '/pages/flyer/qualification/index'
              });
            }
          }
        });
        return;
      }
      
      // 显示确认弹窗
      wx.showModal({
        title: '确认租借',
        content: `确定要租借该无人机吗？\n设备型号：${deviceDetail.model || '未知'}\n租借价格：¥${deviceDetail.rentPrice || 0}/小时`,
        success: async (res) => {
          if (res.confirm) {
            wx.showLoading({ title: '租借中...' });
            try {
              // 调用设备租借接口 - 使用rentDeviceByFlyer方法
              console.log('开始调用设备租借接口，设备ID:', deviceId);
              const rentResponse = await api.rentDeviceByFlyer(deviceId);
              console.log('租借设备接口返回:', rentResponse);
              
              wx.hideLoading(); // 提前隐藏loading
              
              // 直接更新本地状态，将设备标记为已租借
              this.setData({
                'deviceDetail.available': false,
                'deviceDetail.isRentedByMe': true,
                'deviceDetail.rental_status': 1,
                available: false
              });
              
              wx.showToast({
                title: '设备租借成功',
                icon: 'success'
              });
              
              // 重新加载设备详情以确保数据一致性
              setTimeout(async () => {
                console.log('租借后重新加载设备详情...');
                await this.loadDeviceDetail();
                
                // 检查重新加载后的设备状态
                const updatedRentalStatus = this.data.deviceDetail?.rental_status;
                const updatedAvailable = this.data.deviceDetail?.available;
                console.log('重新加载后设备rental_status:', updatedRentalStatus);
                console.log('重新加载后设备available:', updatedAvailable);
                
                // 如果重新加载后设备仍然显示可租借，提示用户可能存在问题
                if (updatedRentalStatus === 0 || updatedAvailable === true) {
                  console.warn('警告：数据库中的设备状态未更新，可能存在问题');
                  wx.showModal({
                    title: '租借状态确认',
                    content: '系统显示租借成功，但数据库状态可能未更新。建议刷新页面或稍后再试。',
                    showCancel: false
                  });
                }
              }, 1500);
            } catch (rentError) {
              console.error('租借设备失败', rentError);
              wx.hideLoading(); // 确保在错误情况下也隐藏loading
              // 检查是否有错误信息
              const errorMsg = rentError.message || rentError.data?.message || rentError.data?.msg || '租借失败，请重试';
              wx.showToast({
                title: errorMsg,
                icon: 'none'
              });
              // 尝试重新加载设备详情，确保显示最新状态
              await this.loadDeviceDetail();
            }
          }
        }
      });
    } catch (error) {
      console.error('租借设备流程异常', error);
      wx.hideLoading();
    }
  },
  
  // 归还设备
  returnDevice: async function() {
    const deviceId = this.data.deviceId;
    
    try {
      // 显示确认弹窗
      wx.showModal({
        title: '确认归还',
        content: '确定要归还该无人机吗？',
        success: async (res) => {
          if (res.confirm) {
            wx.showLoading({ title: '归还中...' });
            try {
              // 调用设备归还接口
              const returnResponse = await api.returnDeviceByFlyer(deviceId);
              console.log('归还设备接口返回:', returnResponse);
              
              wx.hideLoading(); // 提前隐藏loading
              
              // 直接更新本地状态，将设备标记为可租借
              this.setData({
                'deviceDetail.available': true,
                'deviceDetail.isRentedByMe': false,
                'deviceDetail.rental_status': 0,
                available: true
              });
              
              wx.showToast({
                title: '设备归还成功',
                icon: 'success'
              });
              
              // 重新加载设备详情以确保数据一致性
              setTimeout(async () => {
                console.log('归还后重新加载设备详情...');
                await this.loadDeviceDetail();
              }, 1500);
            } catch (returnError) {
              console.error('归还设备失败', returnError);
              wx.hideLoading(); // 确保在错误情况下也隐藏loading
              // 检查是否有错误信息
              const errorMsg = returnError.message || returnError.data?.message || returnError.data?.msg || '归还失败，请重试';
              wx.showToast({
                title: errorMsg,
                icon: 'none'
              });
              // 尝试重新加载设备详情，确保显示最新状态
              await this.loadDeviceDetail();
            }
          }
        }
      });
    } catch (error) {
      console.error('归还设备流程异常', error);
      wx.hideLoading();
    }
  },
  
  // 取消租借（全额退款）
  cancelRental: async function() {
    wx.showModal({
      title: '取消租借',
      content: '确定要取消租借该设备吗？取消后将全额退款。',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '处理中...' });
            // 调用取消租借接口
            await api.cancelDeviceRental(this.data.deviceId);
            
            wx.hideLoading(); // 提前隐藏loading
            
            wx.showToast({
              title: '取消成功，已全额退款',
              icon: 'success'
            });
            
            // 重新加载设备详情
            await this.loadDeviceDetail();
          } catch (error) {
            console.error('取消租借失败', error);
            wx.hideLoading(); // 确保在错误情况下也隐藏loading
            wx.showToast({
              title: '取消失败，请重试',
              icon: 'none'
            });
          }
        }
      }
    });
  }
});