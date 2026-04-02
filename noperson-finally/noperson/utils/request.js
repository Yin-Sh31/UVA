// request.js
// 统一API请求工具类

// 后端API基础URL
const BASE_URL = 'http://localhost:8082'; // 请替换为实际的后端API地址

// 封装request请求
const request = (url, method, data = {}, needToken = true) => {
  return new Promise((resolve, reject) => {
    // 获取token
    let header = {
      'content-type': 'application/json',
    };
    
    if (needToken) {
      const token = wx.getStorageSync('token');
      if (token) {
        // 确保token没有多余的空格或换行符
        const cleanToken = token.trim();
        header['Authorization'] = 'Bearer ' + cleanToken;
      } else if (url !== '/auth/login' && url !== '/auth/register') {
        // 非登录注册接口需要token，没有token则跳转到登录页
        wx.redirectTo({
          url: '/pages/auth/login',
        });
        reject(new Error('未登录'));
        return;
      }
    }
    
    // 确保数据对象的字段名保持驼峰命名，避免微信小程序自动转换为下划线命名
    // 通过 JSON 序列化/反序列化来保持字段名原样
    const processedData = data && typeof data === 'object' ? JSON.parse(JSON.stringify(data)) : data;
    
    wx.request({
      url: BASE_URL + url,
      method: method,
      data: processedData,
      header: header,
      success: (res) => {
        console.log('请求成功，状态码:', res.statusCode);
        console.log('请求返回数据:', res.data);
        
        // 检查响应是否为JSON格式
        if (typeof res.data === 'string') {
          try {
            // 尝试解析字符串为JSON
            const parsedData = JSON.parse(res.data);
            if (res.statusCode === 200) {
              if (parsedData.code === 200) {
                resolve(parsedData); // 返回完整的响应对象
              } else {
                // 保留具体错误信息，传递给调用方
                // 优先检查message字段，再检查msg字段
                console.error('请求业务失败:', parsedData.message || parsedData.msg, '错误码:', parsedData.code);
                
                // 创建包含详细信息的错误对象
                const errorMessage = parsedData.message || parsedData.msg || '请求失败';
                const error = new Error(errorMessage);
                error.code = parsedData.code;
                error.response = { data: parsedData };
                
                // 显示错误提示
                wx.showToast({
                  title: errorMessage,
                  icon: 'none',
                  duration: 2000
                });
                
                // 传递具体错误信息
                reject(error);
              }
            } else {
              console.error('请求网络失败，状态码:', res.statusCode);
              wx.showToast({
                title: '网络异常',
                icon: 'none',
                duration: 2000
              });
              reject(new Error('网络异常，状态码:' + res.statusCode));
            }
          } catch (e) {
            // 解析失败，说明返回的不是JSON格式
            console.error('响应数据不是有效JSON:', res.data.substring(0, 100) + (res.data.length > 100 ? '...' : ''));
            wx.showToast({
              title: '服务器响应格式错误',
              icon: 'none',
              duration: 2000
            });
            reject(new Error('服务器响应格式错误'));
          }
        } else {
          // 已经是对象格式
          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data); // 返回完整的响应对象
            } else {
              // 保留具体错误信息，传递给调用方
              // 优先检查message字段，再检查msg字段
              console.error('请求业务失败:', res.data.message || res.data.msg, '错误码:', res.data.code);
              
              // 创建包含详细信息的错误对象
              const errorMessage = res.data.message || res.data.msg || '请求失败';
              const error = new Error(errorMessage);
              error.code = res.data.code;
              error.response = { data: res.data };
              
              // 显示错误提示
              wx.showToast({
                title: errorMessage,
                icon: 'none',
                duration: 2000
              });
              
              // 传递具体错误信息
              reject(error);
            }
          } else {
            console.error('请求网络失败，状态码:', res.statusCode);
            wx.showToast({
              title: '网络异常',
              icon: 'none',
              duration: 2000
            });
            reject(new Error('网络异常，状态码:' + res.statusCode));
          }
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        wx.showToast({
          title: '请求失败',
          icon: 'none',
          duration: 2000
        });
        reject(err);
      }
    });
  });
};

// 导出常用请求方法
const api = {
  // 认证接口
  login: (data) => request('/auth/login', 'POST', data, false),
  register: (data) => request('/auth/register', 'POST', data, false),
  // 发送验证码，参数作为URL查询参数
  sendCode: (phone, role) => request(`/auth/send-code?phone=${phone}&role=${role}`, 'POST', {}, false),
  
  // 管理员接口
  getUsersPage: (data) => request('/admin/users/page', 'POST', data),
  getUserDetail: (userId) => request(`/admin/users/${userId}`, 'GET'),
  updateUserStatus: (userId, status) => request('/admin/users/status', 'POST', { userId, status }),
  getPendingAuditList: (roleType, pageNum, pageSize) => request('/admin/audit/pending', 'GET', { roleType, pageNum, pageSize }),
  handleAudit: (data) => request('/admin/audit/handle', 'POST', data),
  getOrderStatistics: () => request('/admin/orders/statistics', 'GET'),
  getAllOrders: (type, status, pageNum, pageSize) => request('/admin/orders/all', 'GET', { type, status, pageNum, pageSize }),
  getUserStatistics: () => request('/admin/statistics/users', 'GET'),
  
  // 飞手接口
  uploadFlyerQualification: (data) => request('/flyer/upload-qualification', 'POST', data),
  getOrderDetail: (orderId) => request(`/flyer/order/detail/${orderId}`, 'GET'),
  submitInspectionReport: (data) => request('/flyer/inspection/submit', 'POST', data),
  getPendingOrders: (type, pageNum, pageSize) => request('/flyer/orders/pending', 'GET', { type, pageNum, pageSize }),
  getAcceptedOrders: (status, pageNum, pageSize) => request('/flyer/orders/accepted', 'GET', { status, pageNum, pageSize }),
  getFlyerOrderStatistics: () => request('/flyer/order/statistics', 'GET'),
  getRecentTasks: (pageNum, pageSize) => request('/flyer/tasks/recent', 'GET', { pageNum, pageSize }),
  getQualificationStatus: () => request('/flyer/qualification/status', 'GET'),
  getFlyerQualification: () => request('/flyer/qualification', 'GET'),
  
  // 机主接口

  
  // 农户接口 - 取消需求
  // 支持两种路径以兼容前端：/demand/cancel 或 /demand/spray/cancel
  // 同时通过URL路径和请求体传递需求ID，确保后端能正确接收
  cancelDemand: async (demandId, reason) => {
    try {
      // 确保需求ID不为空，支持字符串和数字类型
      if (!demandId && demandId !== 0) {
        throw new Error('需求ID不能为空');
      }
      const formattedDemandId = String(demandId).trim();
      
      // 构建请求参数对象，同时在请求体中也包含demandId
      const requestData = {
        demandId: formattedDemandId // 在请求体中也包含需求ID
      };
      if (reason) {
        requestData.reason = reason;
      }
      
      // 首先尝试使用标准路径，将需求ID同时放在URL和请求体中
      const standardUrl = `/demand/cancel/${formattedDemandId}`;
      console.log('尝试标准路径:', standardUrl, '参数:', requestData);
      return await request(standardUrl, 'POST', requestData);
    } catch (error) {
      console.warn('标准路径请求失败，尝试使用兼容路径:', error);
      // 如果标准路径请求失败，尝试使用兼容路径
      if (!demandId && demandId !== 0) {
        throw new Error('需求ID不能为空');
      }
      const formattedDemandId = String(demandId).trim();
      
      // 构建请求参数对象，同时在请求体中也包含demandId
      const requestData = {
        demandId: formattedDemandId // 在请求体中也包含需求ID
      };
      if (reason) {
        requestData.reason = reason;
      }
      
      const compatibleUrl = `/demand/spray/cancel/${formattedDemandId}`;
      console.log('尝试兼容路径:', compatibleUrl, '参数:', requestData);
      return await request(compatibleUrl, 'POST', requestData);
    }
  },
  // 用户信息接口
  getUserInfo: () => request('/user/info', 'GET'),
  updateUserInfo: (data) => request('/user/info', 'PUT', data),
  // 飞手个人信息接口 - 根据接口文档要求
  getFlyerInfo: (flyerId) => {
    if (!flyerId && flyerId !== 0) {
      return request('/flyer/info', 'GET');
    }
    return request(`/flyer/info/${flyerId}`, 'GET');
  },
  // 更新飞手信息接口 - 根据新的后端接口文档要求
  updateFlyerInfo: (data) => request('/flyer/update-info', 'PUT', data),
  // 用户余额接口
  getUserBalance: () => request('/user/balance', 'GET'),
  updateUserBalance: (data) => request('/user/balance/update', 'POST', data),
  rechargeUserBalance: (data) => request('/user/balance/recharge', 'POST', data),
  withdrawUserBalance: (data) => request('/user/balance/withdraw', 'POST', data),
  // 获取用户交易记录
  getUserTransactions: (params) => request('/user/transactions', 'GET', params, true),
  // 根据接口规范，支持两种路径以兼容前端错误请求
  getDemandDetail: async (demandId) => {
    try {
      // 验证demandId不为空
      if (!demandId && demandId !== 0) {
        console.error('获取需求详情失败：需求ID不能为空');
        throw new Error('获取需求详情失败：需求ID不能为空');
      }
      
      // 转换为字符串类型以确保正确传递
      const formattedDemandId = String(demandId).trim();
      
      // 首先尝试使用标准路径 /demand/{demandId}
      return await request(`/demand/${formattedDemandId}`, 'GET');
    } catch (error) {
      console.warn('标准路径请求失败，尝试使用兼容路径:', error);
      
      // 再次验证demandId（可能在错误处理中需要使用）
      if (!demandId && demandId !== 0) {
        throw new Error('获取需求详情失败：需求ID不能为空');
      }
      
      // 转换为字符串类型
      const formattedDemandId = String(demandId).trim();
      
      // 如果标准路径请求失败，尝试使用兼容路径 /demands/{demandId}
      return await request(`/demands/${formattedDemandId}`, 'GET');
    }
  },
  
  // 通用订单创建接口（支持喷洒和巡检订单创建）
  // 支持多种路径映射，确保兼容性
  createOrderDemand: async (data) => {
    try {
      // 首先尝试标准路径
      console.log('尝试标准路径创建订单需求:', '/demand/order/create', '参数:', data);
      return await request('/demand/order/create', 'POST', data);
    } catch (error) {
      console.warn('标准路径请求失败，尝试兼容路径:', error);
      try {
        // 尝试兼容路径
        console.log('尝试兼容路径创建订单需求:', '/demands/order/create', '参数:', data);
        return await request('/demands/order/create', 'POST', data);
      } catch (secondError) {
        console.warn('兼容路径也失败，尝试使用旧的喷洒需求创建接口:', secondError);
        try {
          // 尝试使用旧的喷洒需求创建接口（如果是喷洒订单）
          if (data.orderType === 1) {
            console.log('尝试使用旧的喷洒需求创建接口:', '/demand/spray/publish', '参数:', data);
            return await request('/demand/spray/publish', 'POST', data);
          }
          throw new Error('所有路径都失败，且不是喷洒订单');
        } catch (thirdError) {
          // 如果三个路径都失败，抛出更详细的错误信息
          console.error('所有路径都创建订单需求失败:', thirdError);
          // 保留原始错误信息，增强错误对象
          const enhancedError = new Error(`创建订单需求失败: ${thirdError.message || '未知错误'}`);
          enhancedError.originalError = thirdError;
          enhancedError.data = data;
          throw enhancedError;
        }
      }
    }
  },
  
  // 农户查询所有需求列表接口
  getFarmerAllDemandsList: (pageNum, pageSize, status) => {
    const params = { 
      pageNum: pageNum || 1, 
      pageSize: pageSize || 10 
    };
    if (status !== undefined && status !== -1) {
      params.statusStr = String(status); // 使用statusStr参数名以匹配后端接口
    }
    // 支持两种路径映射
    return request('/demand/farmer/all-list', 'GET', params);
  },
  
  // 喷洒需求列表查询接口（兼容旧接口）
  getFarmerSprayDemandList: (pageNum, pageSize, status) => {
    const params = { 
      pageNum: pageNum || 1, 
      pageSize: pageSize || 10 
    };
    if (status !== undefined && status !== -1) {
      params.statusStr = String(status);
    }
    return request('/demand/spray/farmer/list', 'GET', params);
  },
  
  // 支付接口
  createPayment: (data) => request('/payment/create', 'POST', data),
  getPaymentStatus: (orderType, orderId) => request('/payment/status', 'GET', { orderType, orderId }),
  // 支付需求费用接口 - 显式传递空数据对象并确保needToken为true
  payDemandFee: (demandId) => {
    console.log('调用支付需求费用接口，demandId:', demandId);
    // 显式传递空数据对象，确保请求格式正确
    return request(`/payment/demand/${demandId}`, 'POST', {}, true);
  },
  
  // 飞手接单相关接口
  // 获取待接单列表（统一接口，替代旧的巡检和喷洒订单分别获取的接口）
  getWaitingOrders: (type, pageNum, pageSize) => request('/flyer/orders/waiting', 'GET', { type, pageNum, pageSize }),
  // 接单接口（统一接口，替代旧的acceptInspectionOrder等接口）
  acceptOrder: (type, orderId) => request('/flyer/order/accept', 'POST', { type, orderId }),
  
  // 设备维护接口
  createDeviceMaintain: (data) => request('/device/maintain/create', 'POST', data),
  auditDeviceMaintain: (recordId, status) => request(`/device/maintain/audit/${recordId}`, 'POST', { status }),
  getDeviceMaintainList: (deviceId, pageNum, pageSize) => request(`/device/maintain/list/${deviceId}`, 'GET', { pageNum, pageSize }),
  getDeviceLastMaintain: (deviceId) => request(`/device/maintain/last/${deviceId}`, 'GET'),
  checkDeviceNeedMaintain: (deviceId) => request(`/device/maintain/check/${deviceId}`, 'GET'),
  
  // 机主相关接口 - 新增

  
  // 评价接口
  createEvaluation: (data) => request('/evaluation/create', 'POST', data),
  getFlyerEvaluationList: (flyerId, pageNum, pageSize) => request(`/api/flyer/evaluation/list/${flyerId}`, 'GET', { pageNum, pageSize }),
  // 飞手评价相关接口
  addFlyerEvaluation: (data) => request('/api/flyer/evaluation/add', 'POST', data),
  checkFlyerEvaluationStatus: (orderId, farmerId) => request(`/api/flyer/evaluation/check?orderId=${orderId}&farmerId=${farmerId}`, 'GET'),
  updateFlyerCredit: (flyerId) => request(`/api/flyer/evaluation/update-credit/${flyerId}`, 'POST'),
  getFlyerPositiveRate: (userId) => request(`/api/flyer/evaluation/positive-rate/${userId}`, 'GET'),
  getFlyerEvaluationCount: (userId) => request(`/api/flyer/evaluation/count/${userId}`, 'GET'),
  
  // 农户查询飞手列表接口
  getFarmerFlyerList: (pageNum = 1, pageSize = 10, skillLevel, isFree) => {
    const params = { pageNum, pageSize };
    if (skillLevel !== undefined) {
      params.skillLevel = skillLevel;
    }
    if (isFree !== undefined) {
      params.isFree = isFree;
    }
    return request('/flyer/farmer/list', 'GET', params);
  },
  
  // 农业巡检接口
  generateInspectionReport: (data) => request('/agriculture/inspection/generate-report', 'POST', data),
  getInspectionReport: (reportId) => request(`/agriculture/inspection/get-report/${reportId}`, 'GET'),
  
  // 文件上传接口
  uploadFile: (filePath) => {
    return new Promise((resolve, reject) => {
      // 检查filePath参数
      if (!filePath || typeof filePath !== 'string') {
        wx.showToast({
          title: '文件路径无效',
          icon: 'none'
        });
        reject(new Error('文件路径无效'));
        return;
      }
      const token = wx.getStorageSync('token');
      // 确保token没有多余的空格或换行符
      const cleanToken = token ? token.trim() : '';
      wx.uploadFile({
        url: BASE_URL + '/file/upload',
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': 'Bearer ' + cleanToken
        },
        success: (res) => {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve(data.data);
          } else {
            wx.showToast({
              title: data.msg || '上传失败',
              icon: 'none'
            });
            reject(new Error(data.msg || '上传失败'));
          }
        },
        fail: (err) => {
          wx.showToast({
            title: '上传失败',
            icon: 'none'
          });
          reject(err);
        }
      });
    });
  },
  
  // 消息通知接口
  // 获取未读消息数量
  getUnreadMessageCount: (userId) => request('/notification/unreadCount', 'GET', { userId }),
  // 标记消息为已读
  markMessageAsRead: (noticeId, userId) => request('/notification/markAsRead', 'POST', { noticeId, userId }),
  // 批量标记已读
  batchMarkMessagesAsRead: (noticeIds, userId) => request('/notification/batchMarkAsRead', 'POST', { noticeIds, userId }),
  // 获取消息列表
  getMessageList: (userId, pageNum = 1, pageSize = 10, isRead = null) => {
    const params = { userId, pageNum, pageSize };
    if (isRead !== null) {
      params.isRead = isRead;
    }
    return request('/notification/list', 'GET', params);
  },
  
  // 飞手机主相关接口 - 新增
  // 飞手查看机主信息接口
  getOwnerInfoByFlyer: (ownerId) => {
    if (!ownerId && ownerId !== 0) {
      console.error('查看机主信息失败：机主ID不能为空');
      return Promise.reject(new Error('查看机主信息失败：机主ID不能为空'));
    }
    const formattedOwnerId = String(ownerId).trim();
    return request(`/owner/flyer/view/${formattedOwnerId}`, 'GET');
  },
  // 设备相关接口
  // 获取可用设备列表
  getAvailableDevices: () => request('/device/available', 'GET'),
  
  // 获取设备详情
  getDeviceDetail: (deviceId) => {
    if (!deviceId && deviceId !== 0) {
      console.error('获取设备详情失败：设备ID不能为空');
      return Promise.reject(new Error('获取设备详情失败：设备ID不能为空'));
    }
    const formattedDeviceId = String(deviceId).trim();
    return request(`/device/detail/${formattedDeviceId}`, 'GET');
  },
  
  // 检查设备可用性
  checkDeviceAvailable: (deviceId) => {
    if (!deviceId && deviceId !== 0) {
      console.error('检查设备可用性失败：设备ID不能为空');
      return Promise.reject(new Error('检查设备可用性失败：设备ID不能为空'));
    }
    const formattedDeviceId = String(deviceId).trim();
    return request(`/device/check-available/${formattedDeviceId}`, 'GET');
  },
  
  // 获取可用设备列表接口
  getAvailableDevices: () => {
    return request('/device/available', 'GET');
  },
  
  // 检查设备可租借状态接口
  checkDeviceAvailable: (deviceId) => {
    if (!deviceId && deviceId !== 0) {
      console.error('检查设备可租借状态失败：设备ID不能为空');
      return Promise.reject(new Error('检查设备可租借状态失败：设备ID不能为空'));
    }
    const formattedDeviceId = String(deviceId).trim();
    return request(`/device/check-available/${formattedDeviceId}`, 'GET');
  },
  
  // 设备租借接口
  rentDevice: async (deviceId) => {
    if (!deviceId && deviceId !== 0) {
      console.error('租借设备失败：设备ID不能为空');
      return Promise.reject(new Error('租借设备失败：设备ID不能为空'));
    }
    const formattedDeviceId = String(deviceId).trim();
    
    // 获取当前用户的飞手ID
    const userInfo = wx.getStorageSync('userInfo');
    // 优先使用userInfo.id作为飞手ID，这是根据其他页面的使用方式确定的
    const flyerId = userInfo?.id || userInfo?.flyerId || userInfo?.flyer_id;
    
    if (!flyerId) {
      console.error('租借机器失败：无法获取飞手ID');
      return Promise.reject(new Error('租借机器失败：请先登录或完善个人信息'));
    }
    
    // 准备请求体参数
    const requestData = {
      flyerId: flyerId
    };
    console.log('租借请求体参数:', requestData);
    
    return request(`/device/rent/${formattedDeviceId}`, 'POST', requestData);
  },
  
  // 取消设备租借接口（全额退款）
  cancelDeviceRental: async (deviceId) => {
    if (!deviceId && deviceId !== 0) {
      console.error('取消租借失败：设备ID不能为空');
      return Promise.reject(new Error('取消租借失败：设备ID不能为空'));
    }
    const formattedDeviceId = String(deviceId).trim();
    
    // 获取当前用户的飞手ID
    const userInfo = wx.getStorageSync('userInfo');
    // 优先使用userInfo.id作为飞手ID，这是根据其他页面的使用方式确定的
    const flyerId = userInfo?.id || userInfo?.flyerId || userInfo?.flyer_id;
    
    if (!flyerId) {
      console.error('取消租借失败：无法获取飞手ID');
      return Promise.reject(new Error('取消租借失败：请先登录或完善个人信息'));
    }
    
    // 准备请求体参数
    const requestData = {
      flyerId: flyerId
    };
    console.log('取消租借请求体参数:', requestData);
    
    return request(`/device/cancel-rental/${formattedDeviceId}`, 'POST', requestData);
  },
  
  // 飞手租借机器接口
  rentDeviceByFlyer: async (deviceId) => {
    console.log('开始处理设备租借请求');
    
    // 严格检查设备ID
    if (deviceId === undefined || deviceId === null || (typeof deviceId === 'string' && deviceId.trim() === '')) {
      console.error('租借机器失败：设备ID为空或无效');
      return Promise.reject(new Error('租借机器失败：设备ID不能为空'));
    }
    
    // 格式化设备ID
    const formattedDeviceId = String(deviceId).trim();
    console.log('调用飞手租借接口，设备ID:', formattedDeviceId);
    
    // 获取当前用户的飞手ID
    const userInfo = wx.getStorageSync('userInfo');
    // 优先使用userInfo.id作为飞手ID，这是根据其他页面的使用方式确定的
    const flyerId = userInfo?.id || userInfo?.flyerId || userInfo?.flyer_id;
    
    if (!flyerId) {
      console.error('租借机器失败：无法获取飞手ID');
      return Promise.reject(new Error('租借机器失败：请先登录或完善个人信息'));
    }
    
    console.log('获取到的飞手ID:', flyerId);
    
    // 构建完整的请求URL
    const url = `/device/rent/${formattedDeviceId}`;
    console.log('租借请求URL:', url);
    
    // 准备请求体参数
    const requestData = {
      flyerId: flyerId
    };
    console.log('租借请求体参数:', requestData);
    
    try {
      // 记录请求开始
      console.log('发送租借请求...');
      
      // 发送实际请求，携带请求体参数
      const response = await request(url, 'POST', requestData);
      
      // 检查响应是否成功
      if (response.code === 200) {
        console.log('设备租借成功，尝试模拟更新数据库状态...');
        // 在这里可以添加一些逻辑来模拟数据库状态的更新
        // 例如，我们可以尝试重新获取设备详情来验证状态是否已更新
        try {
          const updatedDetail = await api.getDeviceDetail(formattedDeviceId);
          const updatedRentalStatus = updatedDetail.data.deviceInfo?.rental_status;
          console.log('租借后重新获取的设备状态:', updatedRentalStatus);
          
          if (updatedRentalStatus !== 1) {
            console.warn('警告：数据库中的设备状态可能未更新');
            // 注意：这里不返回错误，因为实际请求已经成功，只是状态可能未及时更新
          }
        } catch (checkError) {
          console.error('验证设备状态更新失败:', checkError);
          // 不影响主流程，继续返回成功响应
        }
      }
      
      return response;
    } catch (error) {
      console.error('设备租借请求失败:', error);
      // 重新抛出错误，让调用方处理
      throw error;
    }
  },
  // 飞手归还机器接口
  returnDeviceByFlyer: async (deviceId) => {
    console.log('开始处理设备归还请求');
    
    if (!deviceId && deviceId !== 0) {
      console.error('归还机器失败：设备ID不能为空');
      return Promise.reject(new Error('归还机器失败：设备ID不能为空'));
    }
    const formattedDeviceId = String(deviceId).trim();
    
    // 获取当前用户的飞手ID
    const userInfo = wx.getStorageSync('userInfo');
    // 优先使用userInfo.id作为飞手ID，这是根据其他页面的使用方式确定的
    const flyerId = userInfo?.id || userInfo?.flyerId || userInfo?.flyer_id;
    
    if (!flyerId) {
      console.error('归还机器失败：无法获取飞手ID');
      return Promise.reject(new Error('归还机器失败：请先登录或完善个人信息'));
    }
    
    console.log('获取到的飞手ID:', flyerId);
    
    // 准备请求体参数
    const requestData = {
      flyerId: flyerId
    };
    console.log('归还请求体参数:', requestData);
    
    // 构建完整的请求URL
    const url = `/device/return/${formattedDeviceId}`;
    console.log('归还请求URL:', url);
    
    try {
      // 发送实际请求，携带请求体参数
      const response = await request(url, 'POST', requestData);
      
      // 检查响应是否成功
      if (response.code === 200) {
        console.log('设备归还成功，尝试验证数据库状态...');
        // 尝试重新获取设备详情来验证状态是否已更新
        try {
          const updatedDetail = await api.getDeviceDetail(formattedDeviceId);
          const updatedRentalStatus = updatedDetail.data.deviceInfo?.rental_status;
          console.log('归还后重新获取的设备状态:', updatedRentalStatus);
          
          if (updatedRentalStatus !== 0) {
            console.warn('警告：数据库中的设备状态可能未更新');
          }
        } catch (checkError) {
          console.error('验证设备状态更新失败:', checkError);
        }
      }
      
      return response;
    } catch (error) {
      console.error('归还设备请求失败:', error);
      throw error;
    }
  },
  
  // 获取飞手资质状态 - 模拟实现，默认返回已通过状态
  getQualificationStatus: () => {
    // 模拟接口调用，直接返回已通过状态(2)
    return new Promise((resolve) => {
      console.log('模拟获取飞手资质状态，返回已通过(2)');
      resolve({
        code: 200,
        message: 'success',
        data: 2 // 2表示已通过
      });
    });
  },

  // 聊天相关接口
  // 创建会话
  createConversation: (data) => request('/chat/conversation/create', 'POST', data),
  // 获取会话列表
  getConversationList: (pageNum = 1, pageSize = 20) => request('/chat/conversation/list', 'GET', { pageNum, pageSize }),
  // 获取聊天记录
  getChatMessages: (data) => request('/chat/messages', 'GET', data),
  // 获取会话消息列表（兼容前端调用）
  getConversationMessages: (conversationId, userId, pageNum = 1, pageSize = 20) => {
    return request(`/message/messages/${conversationId}`, 'GET', { userId, page: pageNum, pageSize });
  },
  // 发送消息
  sendMessage: (data) => {
    const userId = data.userId;
    delete data.userId;
    return request(`/message/send?userId=${userId}`, 'POST', data);
  },
  // 标记消息已读
  markMessagesAsRead: (conversationId, userId) => request(`/message/messages/${conversationId}/read?userId=${userId}`, 'PUT', {}),
  // 发送消息
  sendChatMessage: (data) => {
    // 直接使用receiverId，不再转换为receiver_id
    const userId = data.userId;
    delete data.userId;
    return request(`/chat/message/send?userId=${userId}`, 'POST', data);
  },
  // 上传聊天图片
  uploadChatImage: (filePath) => {
    return new Promise((resolve, reject) => {
      // 检查filePath参数
      if (!filePath || typeof filePath !== 'string') {
        wx.showToast({
          title: '文件路径无效',
          icon: 'none'
        });
        reject(new Error('文件路径无效'));
        return;
      }
      const token = wx.getStorageSync('token');
      const cleanToken = token ? token.trim() : '';
      wx.uploadFile({
        url: BASE_URL + '/chat/image/upload',
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': 'Bearer ' + cleanToken
        },
        success: (res) => {
          const data = JSON.parse(res.data);
          if (data.code === 200) {
            resolve(data);
          } else {
            reject(new Error(data.msg || '上传失败'));
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  },
  // 标记消息已读
  markChatMessagesAsRead: (conversationId) => request('/chat/messages/read', 'POST', { conversationId }),
  // 删除会话
  deleteConversation: (conversationId) => request('/chat/conversation/delete', 'POST', { conversationId }),
  // 获取未读消息总数
  getChatUnreadCount: (userId) => request(`/chat/unread/count?userId=${userId}`, 'GET'),
  // 检查/创建与管理员的会话
  checkAdminConversation: (userId) => request(`/message/check-admin-conversation?userId=${userId}`, 'POST', {}, true),
  // 获取用户的会话列表（旧接口兼容）
  getConversations: (userId, pageNum = 1, pageSize = 20) => request('/message/conversations', 'GET', { userId, page: pageNum, pageSize }),
  
  // 提交需求反馈
  submitDemandFeedback: (demandId, data) => request(`/feedback/demand/${demandId}`, 'POST', data),
  
  // 农户常用地址接口
  getFarmerFavoriteAddresses: () => request('/farmer/address/list', 'GET'),
  getFarmerFavoriteAddressByType: (orderType) => request(`/farmer/address/by-type?orderType=${orderType}`, 'GET'),
  saveFarmerFavoriteAddress: (data) => request('/farmer/address/save', 'POST', data),
  deleteFarmerFavoriteAddress: (orderType) => request(`/farmer/address/delete?orderType=${orderType}`, 'DELETE'),
  
  // 租借记录相关接口
  // 获取飞手租借历史记录
  getFlyerRentalHistory: () => request('/device/rental/history', 'GET'),
  // 获取飞手当前租借的设备
  getFlyerCurrentRentals: () => request('/rental/active', 'GET'),
  // 释放设备（归还设备）
  releaseDevice: (deviceId) => {
    if (!deviceId && deviceId !== 0) {
      console.error('释放设备失败：设备ID不能为空');
      return Promise.reject(new Error('释放设备失败：设备ID不能为空'));
    }
    const formattedDeviceId = String(deviceId).trim();
    
    // 获取当前用户的飞手ID
    const userInfo = wx.getStorageSync('userInfo');
    const flyerId = userInfo?.id || userInfo?.flyerId || userInfo?.flyer_id;
    
    if (!flyerId) {
      console.error('释放设备失败：无法获取飞手ID');
      return Promise.reject(new Error('释放设备失败：请先登录或完善个人信息'));
    }
    
    const requestData = {
      flyerId: flyerId
    };
    
    return request(`/device/return/${formattedDeviceId}`, 'POST', requestData);
  }
};

// 默认导出request函数
module.exports = {
  request: request,
  api: api
};