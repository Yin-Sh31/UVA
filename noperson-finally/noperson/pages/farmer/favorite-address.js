// favorite-address.js
// 农户常用地址管理页面
const AddressStorage = require('../../utils/address-storage');

Page({
  data: {
    sprayAddress: null, // 喷洒需求常用地址
    inspectionAddress: null, // 巡检需求常用地址
    loading: false
  },

  onLoad: function() {
    this.loadFavoriteAddresses();
  },

  // 加载常用地址
  loadFavoriteAddresses: async function() {
    this.setData({ loading: true });
    
    try {
      const sprayAddress = await AddressStorage.getAddressByType(1);
      const inspectionAddress = await AddressStorage.getAddressByType(2);
      
      this.setData({
        sprayAddress: sprayAddress,
        inspectionAddress: inspectionAddress
      });
    } catch (error) {
      console.error('加载常用地址失败:', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none',
        duration: 2000
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  // 删除常用地址
  deleteAddress: function(e) {
    const orderType = e.currentTarget.dataset.type;
    const addressName = orderType === 1 ? '喷洒需求地址' : '巡检需求地址';
    
    wx.showModal({
      title: '删除确认',
      content: `确定要删除${addressName}吗？`,
      success: async (res) => {
        if (res.confirm) {
          const success = await AddressStorage.deleteAddress(orderType);
          
          if (success) {
            wx.showToast({
              title: '删除成功',
              icon: 'success',
              duration: 1500
            });
            // 重新加载地址列表
            this.loadFavoriteAddresses();
          } else {
            wx.showToast({
              title: '删除失败，请重试',
              icon: 'none',
              duration: 2000
            });
          }
        }
      }
    });
  },

  // 返回上一页
  goBack: function() {
    wx.navigateBack();
  }
})
