// utils/services/banner.js
// 兼容小程序和Node.js环境
let request;
try {
  const requestModule = require('../request');
  request = requestModule.request || requestModule.default || requestModule;
} catch (error) {
  throw new Error('【Banner服务】未能加载request模块');
}

/**
 * 获取轮播图数据
 * @param {string} type - 轮播图类型，可选值：'farmer'（农户端）, 'flyer'（飞手端）
 * @returns {Promise<Array>} 轮播图数据数组
 */
const getBanners = async (type) => {
  // 验证参数
  if (!type || !['farmer', 'flyer'].includes(type)) {
    throw new Error(`【Banner服务】错误: 无效的轮播图类型: ${type}`);
  }
  
  // 检查request模块是否可用
  if (!request) {
    throw new Error('【Banner服务】错误: request模块不可用');
  }
  
  try {
    // 准备请求参数
    const requestData = { type };
    
    // 使用request模块调用API
    const response = await request('/api/banners', 'GET', requestData, false);
    
    // 从响应中提取轮播图数据
    let bannerList = [];
    if (response && response.data && Array.isArray(response.data.data)) {
      bannerList = response.data.data;
    }
    else if (response && response.data && Array.isArray(response.data)) {
      bannerList = response.data;
    }
    else if (Array.isArray(response)) {
      bannerList = response;
    }
    
    // 过滤并标准化轮播图数据
    const validBanners = bannerList.filter(banner => banner && banner.imageUrl && banner.imageUrl.trim() !== '')
      .map((banner, index) => {
        let imageUrl = banner.imageUrl || '';
        
        // 如果图片URL是相对路径(/images/开头)，则添加后端服务基础URL
        if (imageUrl && typeof imageUrl === 'string' && 
            imageUrl.startsWith('/images/') && !imageUrl.startsWith('http')) {
          imageUrl = 'http://localhost:8082' + imageUrl;
        }
        
        // 确保所有必需字段都存在
        return {
          id: banner.id || `banner_${type}_${index}`,
          title: banner.title || '',
          imageUrl: imageUrl,
          targetUrl: banner.targetUrl || banner.link || '',
          link: banner.targetUrl || banner.link || '',
          __valid: true
        };
      });
    
    return validBanners || [];
  } catch (error) {
    throw new Error(`轮播图API调用失败: ${error.message || '未知错误'}`);
  }
};

module.exports = { getBanners };

module.exports = { getBanners };