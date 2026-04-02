// address-storage.js
// 农户常用地址管理工具类
const { api } = require('./request');

class AddressStorage {
  // 获取用户的常用地址列表
  static async getFavoriteAddresses() {
    try {
      const res = await api.getFarmerFavoriteAddresses();
      if (res.code === 200 && res.data) {
        return res.data;
      }
      return [];
    } catch (error) {
      console.error('获取常用地址列表失败:', error);
      return [];
    }
  }

  // 根据需求类型获取常用地址
  static async getAddressByType(orderType) {
    try {
      const res = await api.getFarmerFavoriteAddressByType(orderType);
      if (res.code === 200 && res.data) {
        return res.data;
      }
      return null;
    } catch (error) {
      console.error(`获取类型为${orderType}的常用地址失败:`, error);
      return null;
    }
  }

  // 保存常用地址
  static async saveAddress(orderType, address) {
    try {
      const addressData = {
        orderType: orderType,
        landName: address.landName,
        landBoundary: address.landBoundary,
        cropType: address.cropType,
        landLocation: address.landLocation,
        landArea: parseInt(address.landArea),
        pestType: address.pestType,
        inspectionPurpose: address.inspectionPurpose,
        expectedResolution: address.expectedResolution
      };
      
      const res = await api.saveFarmerFavoriteAddress(addressData);
      if (res.code === 200) {
        return true;
      }
      return false;
    } catch (error) {
      console.error('保存常用地址失败:', error);
      return false;
    }
  }

  // 删除常用地址
  static async deleteAddress(orderType) {
    try {
      const res = await api.deleteFarmerFavoriteAddress(orderType);
      if (res.code === 200) {
        return true;
      }
      return false;
    } catch (error) {
      console.error(`删除类型为${orderType}的常用地址失败:`, error);
      return false;
    }
  }

  // 检查是否有常用地址
  static async hasAddress(orderType) {
    const address = await this.getAddressByType(orderType);
    return address !== null;
  }
}

module.exports = AddressStorage;
