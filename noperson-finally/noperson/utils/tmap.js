// 腾讯地图API工具类
const TMAP_KEY = 'HMHBZ-T3CCL-AHTPR-MJSGM-MWDY3-7XF7Y'; // 用户提供的腾讯地图API Key

// 检查API Key是否正确配置
const checkApiKey = () => {
  if (!TMAP_KEY || TMAP_KEY === 'your_api_key_here') {
    console.error('腾讯地图API Key未配置，请在tmap.js中设置正确的API Key');
    return false;
  }
  return true;
};

// 处理API错误
const handleApiError = (res, reject) => {
  let errorMsg = '未知错误';
  if (res.data.message) {
    errorMsg = res.data.message;
  } else if (res.errMsg) {
    errorMsg = res.errMsg;
  }
  reject(new Error(errorMsg));
};

/**
 * 地址解析 - 根据地址获取经纬度
 * @param {string} address - 地址
 * @param {string} region - 城市
 * @returns {Promise} - 返回经纬度信息
 */
const geocode = (address, region = '') => {
  return new Promise((resolve, reject) => {
    if (!checkApiKey()) {
      reject(new Error('API Key未配置'));
      return;
    }
    
    wx.request({
      url: 'https://apis.map.qq.com/ws/geocoder/v1/',
      data: {
        key: TMAP_KEY,
        address: address,
        region: region
      },
      success: (res) => {
        if (res.data.status === 0 && res.data.result) {
          const location = res.data.result.location;
          resolve({
            longitude: location.lng,
            latitude: location.lat,
            formattedAddress: res.data.result.address
          });
        } else {
          handleApiError(res, reject);
        }
      },
      fail: (error) => {
        reject(error);
      }
    });
  });
};

/**
 * 逆地理编码 - 根据经纬度获取地址
 * @param {number} longitude - 经度
 * @param {number} latitude - 纬度
 * @returns {Promise} - 返回地址信息
 */
const regeocode = (longitude, latitude) => {
  return new Promise((resolve, reject) => {
    if (!checkApiKey()) {
      reject(new Error('API Key未配置'));
      return;
    }
    
    console.log('【腾讯地图】开始逆地理编码，经度:', longitude, '纬度:', latitude);
    
    wx.request({
      url: 'https://apis.map.qq.com/ws/geocoder/v1/',
      data: {
        key: TMAP_KEY,
        location: `${latitude},${longitude}`,
        get_poi: 1
      },
      success: (res) => {
        console.log('【腾讯地图】逆地理编码API返回:', res);
        
        if (res.data.status === 0 && res.data.result) {
          const addressComponent = res.data.result.address_component;
          resolve({
            address: res.data.result.address,
            province: addressComponent.province || '',
            city: addressComponent.city || '',
            district: addressComponent.district || '',
            town: addressComponent.town || '',
            street: addressComponent.street || '',
            number: addressComponent.street_number || ''
          });
        } else {
          console.error('【腾讯地图】逆地理编码失败，状态码:', res.data.status, '信息:', res.data.message);
          handleApiError(res, reject);
        }
      },
      fail: (error) => {
        console.error('【腾讯地图】逆地理编码请求失败:', error);
        reject(error);
      }
    });
  });
};

/**
 * 搜索地点
 * @param {string} keywords - 搜索关键词
 * @param {string} region - 城市
 * @param {string} location - 中心点经纬度，格式：纬度,经度
 * @param {number} radius - 搜索半径，单位：米
 * @returns {Promise} - 返回搜索结果
 */
const searchPOI = (keywords, region = '', location = '', radius = 1000) => {
  return new Promise((resolve, reject) => {
    if (!checkApiKey()) {
      reject(new Error('API Key未配置'));
      return;
    }
    
    wx.request({
      url: 'https://apis.map.qq.com/ws/place/v1/search',
      data: {
        key: TMAP_KEY,
        keyword: keywords,
        region: region,
        location: location,
        radius: radius,
        page_size: 20,
        page_index: 1
      },
      success: (res) => {
        if (res.data.status === 0) {
          resolve(res.data.data || []);
        } else {
          handleApiError(res, reject);
        }
      },
      fail: (error) => {
        reject(error);
      }
    });
  });
};

/**
 * 获取当前位置
 * @returns {Promise} - 返回位置信息
 */
const getCurrentLocation = () => {
  return new Promise((resolve, reject) => {
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        const { longitude, latitude } = res;
        console.log('【腾讯地图】获取GPS位置成功，经度:', longitude, '纬度:', latitude);
        
        // 调用逆地理编码获取详细地址
        regeocode(longitude, latitude)
          .then(addressInfo => {
            console.log('【腾讯地图】逆地理编码成功，地址信息:', addressInfo);
            resolve({
              longitude,
              latitude,
              ...addressInfo
            });
          })
          .catch(error => {
            console.error('【腾讯地图】逆地理编码失败，使用默认地址:', error);
            // 如果逆地理编码失败，仍然返回经纬度
            resolve({
              longitude,
              latitude,
              address: '未知位置'
            });
          });
      },
      fail: (error) => {
        console.error('【腾讯地图】获取GPS位置失败:', error);
        reject(error);
      }
    });
  });
};

/**
 * 计算两个经纬度之间的距离（单位：公里）
 * 使用Haversine公式计算球面距离
 * @param {number} lat1 - 第一个点的纬度
 * @param {number} lon1 - 第一个点的经度
 * @param {number} lat2 - 第二个点的纬度
 * @param {number} lon2 - 第二个点的经度
 * @returns {number} - 距离（公里）
 */
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // 地球半径，单位：公里
  
  // 将角度转换为弧度
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c;
  
  return distance;
};

/**
 * 将角度转换为弧度
 * @param {number} value - 角度值
 * @returns {number} - 弧度值
 */
const toRad = (value) => {
  return value * Math.PI / 180;
};

/**
 * 格式化距离显示
 * @param {number} distance - 距离（公里）
 * @returns {string} - 格式化后的距离字符串
 */
const formatDistance = (distance) => {
  if (!distance || distance < 0) {
    return '未知距离';
  }
  
  if (distance < 1) {
    return Math.round(distance * 1000) + 'm';
  } else if (distance < 10) {
    return distance.toFixed(1) + 'km';
  } else {
    return Math.round(distance) + 'km';
  }
};

/**
 * 计算批量距离
 * @param {Object} currentLocation - 当前位置 {longitude, latitude}
 * @param {Array} locations - 位置数组 [{longitude, latitude}, ...]
 * @returns {Array} - 距离数组
 */
const calculateBatchDistances = (currentLocation, locations) => {
  if (!currentLocation || !currentLocation.longitude || !currentLocation.latitude) {
    return locations.map(() => null);
  }
  
  return locations.map(location => {
    if (!location || !location.longitude || !location.latitude) {
      return null;
    }
    
    return calculateDistance(
      currentLocation.latitude,
      currentLocation.longitude,
      location.latitude,
      location.longitude
    );
  });
};

/**
 * 获取静态地图图片URL
 * @param {string} center - 中心点经纬度，格式：纬度,经度
 * @param {number} zoom - 缩放级别，范围：3-19
 * @param {string} size - 地图尺寸，格式：宽x高，如640x480
 * @param {Array} markers - 标记点数组，格式：[{lat,lng,label,color}]
 * @returns {string} - 静态地图URL
 */
const getStaticMap = (center = '39.908823,116.397391', zoom = 4, size = '640x480', markers = []) => {
  let url = `https://apis.map.qq.com/ws/staticmap/v2/?key=${TMAP_KEY}&center=${center}&zoom=${zoom}&size=${size}&scale=2&maptype=roadmap`;
  
  // 添加标记点
  if (markers && markers.length > 0) {
    markers.forEach((marker, index) => {
      const color = marker.color || 'red';
      const label = marker.label || '';
      url += `&markers=size:small|color:${color}|label:${label}|${marker.lat},${marker.lng}`;
    });
  }
  
  return url;
};

// 导出模块
module.exports = {
  geocode,
  regeocode,
  searchPOI,
  getCurrentLocation,
  calculateDistance,
  formatDistance,
  calculateBatchDistances,
  getStaticMap
};