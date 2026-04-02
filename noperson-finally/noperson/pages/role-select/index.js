// index.js
Page({
  data: {
    roles: [
      {
        id: 'farmer',
        name: '农户',
        icon: '/assets/images/farmer-icon.png',
        description: '发布农业需求，查看巡检报告'
      },
      {
        id: 'flyer',
        name: '飞手',
        icon: '/assets/images/feishou.jpg',
        description: '承接作业任务，上传巡检数据'
      },
      {
        id: 'admin',
        name: '管理员',
        icon: '/assets/images/admin-icon.png',
        description: '管理用户和订单，查看统计数据'
      }
    ],
    selectedRole: ''
  },
  
  onLoad: function() {
    // 获取当前登录用户的角色
    const app = getApp();
    if (app.globalData.userRole) {
      this.setData({
        selectedRole: app.globalData.userRole
      });
    }
  },
  
  // 选择角色
  selectRole: function(e) {
    const roleId = e.currentTarget.dataset.roleid;
    this.setData({
      selectedRole: roleId
    });
  },
  
  // 进入角色首页
  enterRoleHome: function() {
    const { selectedRole } = this.data;
    
    if (!selectedRole) {
      wx.showToast({
        title: '请选择一个角色',
        icon: 'none'
      });
      return;
    }
    
    // 更新全局角色信息
    const app = getApp();
    app.globalData.userRole = selectedRole;
    wx.setStorageSync('userRole', selectedRole);
    
    // 跳转到对应角色的首页
    switch(selectedRole) {
      case 'farmer':
        wx.redirectTo({
          url: '/pages/farmer/index'
        });
        break;
      case 'flyer':
        wx.redirectTo({
          url: '/pages/flyer/index'
        });
        break;
      case 'admin':
        // 管理员页面不存在，显示提示
        wx.showToast({
          title: '管理员功能开发中',
          icon: 'none'
        });
        break;
    }
  },
  
  // 退出登录
  logout: function() {
    const app = getApp();
    app.logout();
  }
})