// banner-api.js
const request = require('../request').request;

// 服务器基础地址，用于拼接完整图片URL
const BASE_URL = 'http://localhost:8082';

/**
 * 轮播图相关API
 */
const bannerAPI = {
  /**
   * 获取轮播图列表
   * @param {string} type - 用户类型：farmer(农户) 或 flyer(飞手)
   * @returns {Promise} - 返回Promise对象，包含处理后的轮播图数据
   */
  async getBanners(type) {
    console.log(`===== 开始获取轮播图，类型: ${type} =====`);
    
    try {
      console.log('准备请求轮播图API，参数:', { type });
      const response = await request('/api/banners', 'GET', {
        type: type
      }, false); // 设置needToken为false，轮播图接口无需登录权限
      
      console.log('轮播图API返回原始响应:', JSON.stringify(response));
      
      // 处理返回数据
      if (response && response.code === 200) {
        console.log('API调用成功，code=200');
        
        // 处理数据字段
        if (Array.isArray(response.data)) {
          console.log('轮播图数据为数组，共', response.data.length, '条');
          
          const processedBanners = response.data.map((item, index) => {
            console.log(`处理第${index+1}个轮播图原始数据:`, item);
            
            // 确定图片URL - 在小程序中使用相对路径更可靠
            // 如果imageUrl已经是完整URL格式，则直接使用；否则进行处理
            let finalImageUrl = '/assets/images/1.jpg'; // 默认图片
            if (item && item.imageUrl) {
              if (item.imageUrl.startsWith('http://') || item.imageUrl.startsWith('https://')) {
                // 如果已经是完整URL，则直接使用
                finalImageUrl = item.imageUrl;
              } else if (item.imageUrl.startsWith('/')) {
                // 如果是相对路径，直接使用（小程序会通过代理处理）
                finalImageUrl = item.imageUrl;
              } else {
                // 其他情况，加上根路径
                finalImageUrl = `/${item.imageUrl}`;
              }
            }
            
            console.log(`处理后的图片URL:`, finalImageUrl);
            
            return {
              id: item?.id || `banner_${index}`,
              title: item?.title || `轮播图${index+1}`,
              // 设置url和imageUrl字段，确保两种访问方式都可用
              url: finalImageUrl,
              imageUrl: finalImageUrl,
              type: item?.type || type,
              sort: item?.sort || index,
              status: item?.status || 1,
              createTime: item?.createTime,
              updateTime: item?.updateTime,
              // 点击跳转链接
              link: item?.targetUrl || ''
            };
          });
          
          console.log('处理完成，共生成', processedBanners.length, '个轮播图对象');
          
          // 返回处理后的数据，保持原有结构
          const result = {
            ...response,
            data: processedBanners
          };
          console.log('返回处理后的数据:', JSON.stringify(result));
          return result;
        } else {
          console.warn('轮播图data不是数组:', response.data);
        }
      } else {
        console.warn('轮播图API返回非成功状态:', response?.code);
      }
      
      console.log('返回原始响应');
      return response;
    } catch (error) {
      console.error('获取轮播图异常:', error);
      console.error('错误类型:', error.name);
      console.error('错误信息:', error.message);
      
      // 发生异常时，返回包含默认轮播图的数据，确保前端始终有内容显示
      console.log('返回默认轮播图数据');
      return {
        code: 200,
        data: [
          { 
            id: 'default_1',
            title: '默认轮播图1', 
            url: '/assets/images/1.jpg', 
            imageUrl: '/assets/images/1.jpg',
            type: type,
            link: ''
          },
          { 
            id: 'default_2',
            title: '默认轮播图2', 
            url: '/assets/images/2.jpg', 
            imageUrl: '/assets/images/2.jpg',
            type: type,
            link: ''
          }
        ],
        message: '使用默认轮播图数据'
      };
    } finally {
      console.log('===== 轮播图获取结束 =====');
    }
  },

  /**
   * 管理员获取所有轮播图
   * @returns {Promise} - 返回Promise对象
   */
  getAdminBanners() {
    return request('/api/admin/banners', 'GET');
  },

  /**
   * 管理员创建轮播图
   * @param {Object} data - 轮播图数据
   * @param {string} data.title - 轮播图标题
   * @param {string} data.imageUrl - 图片相对URL路径
   * @param {string} data.type - 用户类型：farmer 或 flyer
   * @param {number} data.sort - 排序值，数字越小越靠前
   * @param {number} data.status - 状态：1-启用，0-禁用
   * @param {string} data.targetUrl - 跳转链接（可选）
   * @returns {Promise} - 返回Promise对象
   */
  createBanner(data) {
    return request('/api/admin/banners', 'POST', data);
  },

  /**
   * 管理员更新轮播图
   * @param {string} id - 轮播图ID
   * @param {Object} data - 更新的数据
   * @returns {Promise} - 返回Promise对象
   */
  updateBanner(id, data) {
    return request(`/api/admin/banners/${id}`, 'PUT', data);
  },

  /**
   * 管理员删除轮播图
   * @param {string} id - 轮播图ID
   * @returns {Promise} - 返回Promise对象
   */
  deleteBanner(id) {
    return request(`/api/admin/banners/${id}`, 'DELETE');
  }
};

module.exports = bannerAPI;