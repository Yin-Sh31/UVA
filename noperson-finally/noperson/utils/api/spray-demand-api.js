const request = require('../request').request;

/**
 * 喷洒需求相关API
 */
const sprayDemandAPI = {
  /**
   * 接取喷洒需求（新接口 - 统一处理支付和未支付状态）
   * 农户支付后需求状态为待接取(0)，飞手接取后状态直接变为待确认(3)
   * @param {Object} params - 参数对象
   * @param {number|string} params.demandId - 需求ID
   * @returns {Promise<Object>} 返回包含DemandVO的结果
   */
  acceptSprayDemand: (params) => {
    // 验证参数
    if (!params || !params.demandId && params.demandId !== 0) {
      return Promise.reject(new Error('需求ID不能为空'));
    }
    
    // 确保demandId转换为字符串，符合后端接口要求
    const demandId = String(params.demandId).trim();
    
    // 注意：根据后端接口定义，demandId作为URL路径参数传递
    // 后端接口会自动从Principal获取飞手ID，前端不需要传递flyerId
    // 新业务逻辑：农户支付后状态为待接取(0)，飞手接取后状态直接变为待确认(3)
    return request(`/demand/spray/accept/${demandId}`, 'POST', {});
  },

  /**
   * 接取需求
   * 农户支付后需求状态为待接取(0)，飞手接取后状态直接变为待确认(3)
   * @param {Object} params - 参数对象
   * @param {number|string} params.demandId - 需求ID
   * @param {number} params.deviceId - 设备ID（兼容使用）
   * @returns {Promise<boolean>} 是否接取成功
   */
  acceptDemand: (params) => {
    // 验证参数
    if (!params || !params.demandId && params.demandId !== 0) {
      return Promise.reject(new Error('需求ID不能为空'));
    }
    
    // 确保demandId转换为字符串，符合后端接口要求
    const formattedParams = {
      ...params,
      demandId: String(params.demandId).trim()
    };
    
    // 注意：根据后端接口定义，flyerId应由后端从Principal获取
    // 但为兼容现有API，我们仍传递完整参数对象
    // 新业务逻辑：农户支付后状态为待接取(0)，飞手接取后状态直接变为待确认(3)
    return request('/spray-demand/accept', 'POST', formattedParams);
  },

  /**
   * 接取需求（兼容旧接口）
   * 农户支付后需求状态为待接取(0)，飞手接取后状态直接变为待确认(3)
   * @param {Object} params - 参数对象
   * @param {number|string} params.demandId - 需求ID
   * @returns {Promise<boolean>} 是否接取成功
   */
  acceptPaidDemand: (params) => {
    // 验证参数
    if (!params || !params.demandId && params.demandId !== 0) {
      return Promise.reject(new Error('需求ID不能为空'));
    }
    
    // 确保demandId转换为字符串，符合后端接口要求
    const formattedParams = {
      ...params,
      demandId: String(params.demandId).trim()
    };
    
    // 新业务逻辑：农户支付后状态为待接取(0)，飞手接取后状态直接变为待确认(3)
    return request('/spray-demand/accept-paid', 'POST', formattedParams);
  },

  /**
   * 查询所有用户发布的需求列表（飞手端）
   * @param {Object} params - 参数对象
   * @param {number} params.pageNum - 页码
   * @param {number} params.pageSize - 每页条数
   * @param {string} [params.skillLevel] - 技能等级筛选（可选）
   * @param {string} [params.location] - 位置筛选（模糊匹配，可选）
   * @param {string} [params.statusStr] - 状态筛选（字符串格式的数字，可选，默认查询待接取状态）
   * @returns {Promise<Page<DemandVO>>} 分页需求列表
   */
  getAllFarmersDemandsForFlyer: (params) => {
    return request('/demand/spray/flyer/all-demands', 'GET', params);
  },

  /**
   * 只查询喷洒需求的接口（飞手端）
   * @param {Object} params - 参数对象
   * @param {number} params.pageNum - 页码
   * @param {number} params.pageSize - 每页条数
   * @param {string} [params.skillLevel] - 技能等级筛选（可选）
   * @param {string} [params.location] - 位置筛选（模糊匹配，可选）
   * @param {string} [params.status] - 状态筛选（可选）
   * @returns {Promise<Page<DemandVO>>} 分页喷洒需求列表
   */
  getSprayOnlyDemands: (params) => {
    return request('/demand/spray/flyer/spray-only', 'GET', params);
  },

  /**
   * 只查询巡检需求的接口（飞手端）
   * @param {Object} params - 参数对象
   * @param {number} params.pageNum - 页码
   * @param {number} params.pageSize - 每页条数
   * @param {string} [params.skillLevel] - 技能等级筛选（可选）
   * @param {string} [params.location] - 位置筛选（模糊匹配，可选）
   * @param {string} [params.status] - 状态筛选（可选）
   * @returns {Promise<Page<DemandVO>>} 分页巡检需求列表
   */
  getInspectionOnlyDemands: (params) => {
    return request('/demand/spray/flyer/inspection-only', 'GET', params);
  },

  
  /**
   * 查询当前飞手已接单需求（新接口）
   * @param {Object} params - 参数对象
   * @param {number} params.pageNum - 页码，默认为1
   * @param {number} params.pageSize - 每页数量，默认为10
   * @param {string} [params.statusStr] - 状态筛选参数（可选）
   * @returns {Promise<Object>} 分页数据，包含total、pages、current、size、records等字段
   */
  getMyDemands: (params) => {
    return request('/demand/spray/flyer/my-demands', 'GET', params);
  },

  /**
   * 查看单个需求（新接口）
   * 使用路径参数格式：GET /demand/{demandId}
   * @param {Object} params - 参数对象
   * @param {number} params.demandId - 需求ID
   * @param {number} params.userId - 当前用户ID
   * @param {string} params.userRole - 当前用户角色（飞手为"flyer"）
   * @returns {Promise<DemandVO>} 需求详情(DemandVO对象)
   */
  getDemandById: (params) => {
    // 验证参数
    if (!params || !params.demandId && params.demandId !== 0) {
      return Promise.reject(new Error('需求ID不能为空'));
    }
    
    // 确保demandId转换为字符串，符合后端接口要求
    const demandId = String(params.demandId).trim();
    
    // 构建查询参数，包含userId和userRole
    const queryParams = {};
    if (params.userId) {
      queryParams.userId = params.userId;
    }
    if (params.userRole) {
      queryParams.userRole = params.userRole;
    }
    
    // 根据新接口规范，使用路径参数传递demandId，并包含查询参数
    return request(`/demand/${demandId}`, 'GET', queryParams);
  },

  /**
   * 开始作业
   * @param {Object} params - 参数对象
   * @param {number|string} params.demandId - 需求ID
   * @param {number|string} params.flyerId - 飞手ID
   * @param {number|string} params.userId - 用户ID（可选，通常与flyerId相同）
   * @param {string} params.userRole - 用户角色（可选，应为'flyer'）
   * @returns {Promise<boolean>} 是否开始成功
   */
  startWork: (params) => {
    // 检查并验证必要参数
    if (!params || !params.demandId && params.demandId !== 0) {
      console.error('开始作业失败：demandId不能为空');
      return Promise.reject(new Error('开始作业失败：需求ID不能为空'));
    }
    
    if (!params.flyerId && params.flyerId !== 0) {
      console.error('开始作业失败：flyerId不能为空');
      return Promise.reject(new Error('开始作业失败：飞手ID不能为空'));
    }
    
    // 确保参数转换为数字类型，符合后端接口要求
    const formattedParams = {
      ...params,
      demandId: Number(params.demandId),
      flyerId: Number(params.flyerId),
      // 确保用户角色信息存在，默认为'flyer'
      userRole: params.userRole || 'flyer'
    };
    
    console.log('调用startWork API，参数:', formattedParams);
    
    try {
      return request('/demands/start-work', 'POST', formattedParams);
    } catch (error) {
      console.error('startWork API调用失败:', error.message || error);
      throw error;
    }
  },

  /**
   * 完成作业
   * 飞手确认需求订单后，状态从待确认(3)直接变为已完成(4)
   * 已完成后平台抽成10%，飞手获得90%的需求金额
   * @param {Object} params - 参数对象
   * @param {number|string} params.demandId - 需求ID
   * @param {number} params.flyerId - 飞手ID
   * @returns {Promise<boolean>} 是否完成成功
   */
  completeWork: async (params) => {
    // 检查并验证必要参数
    if (!params || !params.demandId && params.demandId !== 0) {
      console.error('完成作业失败：demandId不能为空');
      return Promise.reject(new Error('完成作业失败：需求ID不能为空'));
    }
    
    if (!params.flyerId && params.flyerId !== 0) {
      console.error('完成作业失败：flyerId不能为空');
      return Promise.reject(new Error('完成作业失败：飞手ID不能为空'));
    }
    
    // 确保参数转换为数字类型，符合后端接口要求
    const formattedParams = {
      ...params,
      demandId: Number(params.demandId),
      flyerId: Number(params.flyerId),
      // 添加用户角色信息，确保后端能正确识别当前操作用户类型
      userRole: 'flyer'
    };
    
    console.log('调用completeWork API，参数:', formattedParams);
    
    try {
      // request.js已经处理了错误情况和状态码检查
      // 它会在code不是200时直接抛出错误，错误信息包含后端返回的msg
      // 完成后状态从待确认(3)变为已完成(4)，平台抽成10%，飞手获得90%
      const result = await request('/demand/complete-work', 'POST', formattedParams);
      
      console.log('completeWork API调用成功，返回结果:', result);
      return result;
    } catch (error) {
      // 捕获request.js抛出的错误，可能包含后端返回的具体错误信息
      console.error('completeWork API调用失败:', error.message || error);
      
      // 重新抛出错误，确保保留原始错误信息
      if (error.message && error.message !== '请求失败') {
        throw error;
      } else {
        // 否则，尝试从error.response中提取更多信息
        if (error.response && error.response.data && error.response.data.message) {
          throw new Error(error.response.data.message);
        } else if (error.response && error.response.data && error.response.data.msg) {
          throw new Error(error.response.data.msg);
        } else {
          throw error;
        }
      }
    }
  },

  /**
   * 取消需求
   * @param {Object} params - 参数对象
   * @param {number|string} params.demandId - 需求ID
   * @param {number} params.operatorId - 操作人ID
   * @param {string} params.reason - 取消原因
   * @returns {Promise<boolean>} 是否取消成功
   */
  cancelDemand: (params) => {
    // 检查并验证demandId参数
    const { demandId, operatorId, reason } = params;
    
    // 确保demandId不为空且为有效类型
    if (!demandId && demandId !== 0) {
      console.error('取消需求失败：demandId不能为空');
      return Promise.reject(new Error('取消需求失败：需求ID不能为空'));
    }
    
    // 转换demandId为字符串类型，确保能正确传递给后端
    const formattedDemandId = String(demandId).trim();
    
    // 重新构建参数对象
    const requestParams = {
      demandId: formattedDemandId,
      operatorId,
      reason
    };
    
    return request('/spray-demand/cancel', 'POST', requestParams);
  },
  /**
   * 获取飞手个人信息
   * @returns {Promise<UserFlyerVO>} 飞手个人信息
   */
  getFlyerInfo: () => {
    return request('/flyer/info', 'GET', {}, true);
  },
  
  /**
   * 支付需求（新方法）
   * 农户支付需求金额后，需求状态变为待接取(0)
   * @param {Object} params - 参数对象
   * @param {number|string} params.demandId - 需求ID
   * @param {number} params.amount - 支付金额
   * @returns {Promise<Object>} 支付结果
   */
  payDemand: (params) => {
    // 验证参数
    if (!params || !params.demandId && params.demandId !== 0) {
      return Promise.reject(new Error('需求ID不能为空'));
    }
    
    if (!params.amount || params.amount <= 0) {
      return Promise.reject(new Error('支付金额必须大于0'));
    }
    
    // 确保参数格式正确
    const formattedParams = {
      ...params,
      demandId: String(params.demandId).trim(),
      amount: Number(params.amount)
    };
    
    // 调用支付接口，支付成功后需求状态变为待接取(0)
    return request('/spray-demand/pay', 'POST', formattedParams);
  },

  /**
   * 获取飞手接单控制状态
   * @returns {Promise<boolean>} 是否禁止接单
   */
  getFlyerOrderStatus: () => {
    return request('/demand/system/flyer-order-status', 'GET');
  },

  /**
   * 获取农户列表（飞手端）
   * @param {Object} params - 参数对象
   * @param {number} params.pageNum - 页码
   * @param {number} params.pageSize - 每页条数
   * @returns {Promise<Array>} 农户列表
   */
  getFarmersList: (params) => {
    return request('/farmer/flyer/getFlyerList', 'GET', params);
  }
};

module.exports = sprayDemandAPI;