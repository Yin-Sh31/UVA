// 高德地图API工具类
const AMAP_KEY = 'ec7edb45e635f6023bbae90ca65ba6cf'; // 用户提供的API Key

// 检查API Key是否正确配置
const checkApiKey = () => {
  if (!AMAP_KEY || AMAP_KEY === 'your_api_key_here') {
    console.error('高德地图API Key未配置，请在amap.js中设置正确的API Key');
    return false;
  }
  return true;
};

// 处理API错误
const handleApiError = (res, reject) => {
  let errorMsg = '未知错误';
  if (res.data.info) {
    errorMsg = res.data.info;
    // 根据不同的错误码提供具体的解决建议
    if (errorMsg.includes('USERKEY_PLAT_NOMATCH')) {
      errorMsg += '。请确保API Key已与小程序AppID绑定，并在小程序管理后台配置了相应的域名白名单。';
    }
  }
  reject(new Error(errorMsg));
};

// 行政区域查询
const getDistrictList = (keywords = '', level = 'district') => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: 'https://restapi.amap.com/v3/config/district',
      data: {
        key: AMAP_KEY,
        keywords: keywords,
        level: level,
        subdistrict: 1
      },
      success: (res) => {
        if (res.data.status === '1' && res.data.districts && res.data.districts.length > 0) {
          resolve(res.data.districts);
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

// 地址解析 - 根据地址获取经纬度
const geocode = (address, city = '') => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: 'https://restapi.amap.com/v3/geocode/geo',
      data: {
        key: AMAP_KEY,
        address: address,
        city: city
      },
      success: (res) => {
        if (res.data.status === '1' && res.data.geocodes && res.data.geocodes.length > 0) {
          const location = res.data.geocodes[0].location; // 格式: "116.397428,39.90923"
          resolve({
            longitude: location.split(',')[0],
            latitude: location.split(',')[1],
            formattedAddress: res.data.geocodes[0].formatted_address
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

// 逆地理编码 - 根据经纬度获取地址
const regeocode = (longitude, latitude) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: 'https://restapi.amap.com/v3/geocode/regeo',
      data: {
        key: AMAP_KEY,
        location: `${longitude},${latitude}`
      },
      success: (res) => {
        if (res.data.status === '1' && res.data.regeocode) {
          resolve({
            address: res.data.regeocode.formatted_address,
            province: res.data.regeocode.addressComponent.province || '',
            city: res.data.regeocode.addressComponent.city || '',
            district: res.data.regeocode.addressComponent.district || '',
            town: res.data.regeocode.addressComponent.township || '',
            street: res.data.regeocode.addressComponent.street || '',
            number: res.data.regeocode.addressComponent.streetNumber?.number || ''
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

// 搜索地点
const searchPOI = (keywords, city = '', location = '', radius = 1000) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: 'https://restapi.amap.com/v3/place/text',
      data: {
        key: AMAP_KEY,
        keywords: keywords,
        city: city,
        location: location,
        radius: radius,
        offset: 20
      },
      success: (res) => {
        if (res.data.status === '1') {
          resolve(res.data.pois || []);
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

module.exports = {
  getDistrictList,
  geocode,
  regeocode,
  searchPOI
};