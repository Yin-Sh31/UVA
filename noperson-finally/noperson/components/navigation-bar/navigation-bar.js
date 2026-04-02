Component({
  options: {
    multipleSlots: true // 在组件定义时的选项中启用多slot支持
  },
  /**
   * 组件的属性列表
   */
  properties: {
    extClass: {
      type: String,
      value: ''
    },
    title: {
      type: String,
      value: ''
    },
    background: {
      type: String,
      value: ''
    },
    color: {
      type: String,
      value: ''
    },
    back: {
      type: Boolean,
      value: true
    },
    loading: {
      type: Boolean,
      value: false
    },
    homeButton: {
      type: Boolean,
      value: false,
    },
    showMessageIcon: {
      type: Boolean,
      value: false
    },
    animated: {
      // 显示隐藏的时候opacity动画效果
      type: Boolean,
      value: true
    },
    show: {
      // 显示隐藏导航，隐藏的时候navigation-bar的高度占位还在
      type: Boolean,
      value: true,
      observer: '_showChange'
    },
    // back为true的时候，返回的页面深度
    delta: {
      type: Number,
      value: 1
    },
  },
  /**
   * 组件的初始数据
   */
  data: {
    displayStyle: ''
  },
  lifetimes: {
    attached() {
      const rect = wx.getMenuButtonBoundingClientRect()
      const platform = (wx.getDeviceInfo() || wx.getSystemInfoSync()).platform
      const isAndroid = platform === 'android'
      const isDevtools = platform === 'devtools'
      const { windowWidth, safeArea: { top = 0, bottom = 0 } = {} } = wx.getWindowInfo() || wx.getSystemInfoSync()
      this.setData({
        ios: !isAndroid,
        innerPaddingRight: `padding-right: ${windowWidth - rect.left}px`,
        leftWidth: `width: ${windowWidth - rect.left}px`,
        safeAreaTop: isDevtools || isAndroid ? `height: calc(var(--height) + ${top}px); padding-top: ${top}px` : ``
      })
    },
  },
  /**
   * 组件的方法列表
   */
  methods: {
    _showChange(show) {
      const animated = this.data.animated
      let displayStyle = ''
      if (animated) {
        displayStyle = `opacity: ${show ? '1' : '0'
          };transition:opacity 0.5s;`
      } else {
        displayStyle = `display: ${show ? '' : 'none'}`
      }
      this.setData({
        displayStyle
      })
    },
    back() {
      const data = this.data;
      const delta = data.delta || 1;
      
      // 确保执行导航返回操作
      console.log('执行返回操作，返回层级:', delta);
      
      // 尝试使用wx.navigateBack
      wx.navigateBack({
        delta: delta,
        fail: function(err) {
          console.error('wx.navigateBack失败:', err);
          // 如果navigateBack失败，尝试使用getCurrentPages来获取页面栈并直接跳转
          const pages = getCurrentPages();
          console.log('当前页面栈长度:', pages.length);
          
          // 如果页面栈长度大于1，尝试返回上一页
          if (pages.length > 1) {
            console.log('尝试使用其他导航方法返回');
            // 获取上一页的路径并使用navigateTo或redirectTo
            const prevPage = pages[pages.length - 2];
            if (prevPage && prevPage.route) {
              wx.redirectTo({
                url: '/' + prevPage.route
              });
            }
          } else {
            // 当页面栈长度为1时，根据当前页面路径决定跳转目标
          console.log('页面栈长度为1，需要确定合适的跳转页面');
          const currentPage = pages[0];
          let redirectUrl = '/pages/index/index'; // 默认跳转到首页
          
          // 如果是农户相关页面，跳转到农户首页
          if (currentPage && currentPage.route && currentPage.route.includes('farmer/')) {
            redirectUrl = '/pages/farmer/index';
          }
          // 如果是飞手相关页面，跳转到飞手首页
          else if (currentPage && currentPage.route && currentPage.route.includes('flyer/')) {
            redirectUrl = '/pages/flyer/index';
          }
          // 如果是管理员相关页面，跳转到管理员首页
          else if (currentPage && currentPage.route && currentPage.route.includes('admin/')) {
            redirectUrl = '/pages/admin/index';
          }
          
          console.log('当前页面路径:', currentPage?.route, '，跳转到:', redirectUrl);
          wx.redirectTo({
            url: redirectUrl
          });
          }
        }
      });
      
      this.triggerEvent('back', { delta: delta }, {});
    },
    onMessageTap() {
      this.triggerEvent('messageTap', {}, {})
    },
    home() {
      // 定义tabBar页面列表
      const tabBarPages = [
        'pages/flyer/index',
        'pages/flyer/accept-order/index',
        'pages/flyer/message-center/index',
        'pages/flyer/profile/index'
      ];
      
      const targetPage = '/pages/index/index';
      
      // 检查目标页面是否为tabBar页面
      if (tabBarPages.includes(targetPage.replace(/^\//, ''))) {
        // 对于tabBar页面，使用wx.switchTab
        wx.switchTab({
          url: targetPage,
          fail: (err) => {
            console.error('使用switchTab跳转首页失败:', err);
            // 如果switchTab失败，尝试使用redirectTo
            wx.redirectTo({
              url: targetPage
            })
          }
        });
      } else {
        // 对于非tabBar页面，使用wx.navigateTo
        wx.navigateTo({
          url: targetPage,
          fail: (err) => {
            console.error('使用navigateTo跳转首页失败:', err);
            // 如果navigateTo失败，尝试使用redirectTo
            wx.redirectTo({
              url: targetPage
            })
          }
        });
      }
    }
  },
})
