// 角色配置文件
const roleConfig = {
  // 角色类型定义
  types: {
    FARMER: 'farmer',
    FLYER: 'flyer',
    ADMIN: 'admin'
  },
  
  // 角色标题配置
  getTitle(role) {
    const titles = {
      farmer: '农翼通 - 农户版',
      flyer: '农翼通 - 飞手版',
      admin: '农翼通 - 管理版'
    };
    return titles[role] || '农翼通';
  },
  
  // 角色TabBar配置
  getTabBar(role) {
    let commonTabs = [
      {
        pagePath: 'pages/index/index',
        text: '首页',
        iconPath: 'images/home-icon.png',
        selectedIconPath: 'images/home-icon.png'
      },
      {
        pagePath: role === 'admin' ? 'pages/admin/weather-management' : 'pages/farmer/my-demand-list',
        text: role === 'admin' ? '地图+天气' : '订单',
        iconPath: role === 'admin' ? 'images/system-icon.svg' : 'images/order-icon.png',
        selectedIconPath: role === 'admin' ? 'images/system-icon.svg' : 'images/order-icon.png'
      },
      {        
        pagePath: role === 'admin' ? 'pages/admin/user-management' : 
                 role === 'farmer' ? 'pages/farmer/publish-spray-demand' :
                 'pages/flyer/index',
        text: role === 'admin' ? '用户' : 
              '需求',
        iconPath: role === 'admin' ? 'images/user-icon.png' :
                  'images/demand-icon.png',
        selectedIconPath: role === 'admin' ? 'images/user-icon.png' :
                          'images/demand-icon.png'
      },
      {        
        pagePath: role === 'admin' ? 'pages/admin/index' : 
                 role === 'farmer' ? 'pages/farmer/profile' :
                 'pages/flyer/index',
        text: '我的',
        iconPath: 'images/profile-icon.png',
        selectedIconPath: 'images/profile-icon.png'
      }
    ];
    
    return {
      color: '#999999',
      selectedColor: '#1AAD19',
      backgroundColor: '#ffffff',
      list: commonTabs
    };
  }
};

module.exports = roleConfig;