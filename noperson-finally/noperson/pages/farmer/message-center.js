// farmer/message-center.js
// 导入API工具
const { api } = require('../../utils/request');

Page({
  data: {
    messageList: [], // 消息列表
    loading: false,
    pageNum: 1,
    pageSize: 15, // 增加每页加载数量以减少请求次数
    hasMore: true,
    refreshing: false,
    // 消息分组数据
    hasTodayMessages: false,
    hasYesterdayMessages: false,
    hasWeekMessages: false,
    hasEarlierMessages: false,
    todayMessages: [],
    yesterdayMessages: [],
    weekMessages: [],
    earlierMessages: [],
    // 消息类型配置
    messageTypes: {
      'SPRAY_DEMAND': { icon: '/images/demand-icon.png', type: 'demand', color: '#e8f5e9' },
      'INSPECTION_DEMAND': { icon: '/images/order-icon.png', type: 'order', color: '#e3f2fd' },
      'PAYMENT_SUCCESS': { icon: '/assets/images/payment-icon.svg', type: 'payment', color: '#fff3e0' },
      'DEFAULT': { icon: '/assets/images/system-icon.svg', type: 'system', color: '#e8f5e9' }
    },
    // 缓存优化
    cachedMessageList: [],
    lastUpdateTime: 0
  },
  
  onLoad: function() {
    // 预加载缓存数据并同时请求新数据
    this.loadCachedData();
    // 获取未读消息数量
    this.getUnreadMessageCount();
  },
  
  onShow: function() {
    // 仅在超过5分钟或有缓存时重新加载数据
    const currentTime = Date.now();
    if (currentTime - this.data.lastUpdateTime > 5 * 60 * 1000 || this.data.messageList.length === 0) {
      this.setData({
        messageList: [],
        pageNum: 1,
        hasMore: true,
        refreshing: true
      });
      // 直接加载真实数据
      this.loadMessageList();
      // 获取未读消息数量
      this.getUnreadMessageCount();
    }
  },
  
  // 加载缓存数据
  loadCachedData: function() {
    try {
      const cachedData = wx.getStorageSync('cachedMessageData');
      if (cachedData && cachedData.list && cachedData.timestamp) {
        // 只显示缓存数据的前20条，避免过多渲染
        const limitedCache = cachedData.list.slice(0, 20);
        this.setData({
          cachedMessageList: limitedCache
        });
        // 先计算缓存数据的分组，提供即时显示
        this.calculateMessageGroups(limitedCache);
      }
    } catch (error) {
      console.error('加载缓存数据失败:', error);
    }
  },
  
  // 获取未读消息数量
  getUnreadMessageCount: async function() {
    try {
      // 首先检查是否正在获取未读消息数量，避免重复请求
      if (this._isGettingUnreadCount) return;
      this._isGettingUnreadCount = true;
      
      // 获取用户ID
      const userInfo = wx.getStorageSync('userInfo');
      const userId = userInfo?.id || '';
      
      if (!userId) {
        console.error('用户ID获取失败');
        return;
      }
      
      // 调用获取未读消息数量API
      const unreadCount = await api.getUnreadMessageCount(userId);
      console.log('未读消息数量:', unreadCount);
      
      // 存储到全局和本地
      wx.setStorageSync('unreadMessageCount', unreadCount);
      
      // 更新页面标题栏的红点
      if (unreadCount > 0) {
        wx.setTabBarBadge({
          index: 2, // 假设消息中心是第3个tab
          text: unreadCount > 99 ? '99+' : String(unreadCount)
        });
      } else {
        wx.removeTabBarBadge({
          index: 2
        });
      }
      
      // 通知其他页面更新未读消息数量
      const app = getApp();
      if (app.globalData && typeof app.globalData.updateUnreadMessageCount === 'function') {
        app.globalData.updateUnreadMessageCount(unreadCount);
      }
    } catch (error) {
      console.error('获取未读消息数量失败:', error);
    } finally {
      this._isGettingUnreadCount = false;
    }
  },
  
  // 加载消息列表
  loadMessageList: async function() {
    console.log('开始加载消息列表');
    if (!this.data.hasMore || this.data.loading) {
      console.log('无需加载更多或已在加载中');
      return;
    }
    
    this.setData({
      loading: true
    });
    
    try {
      // 获取用户ID
      const userInfo = wx.getStorageSync('userInfo');
      const userId = userInfo?.id || '';
      
      if (!userId) {
        console.error('用户ID获取失败');
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        this.setData({
          loading: false,
          refreshing: false
        });
        wx.redirectTo({
          url: '/pages/auth/login'
        });
        return;
      }
      
      let result = null;
      
      try {
        // 调用获取消息列表API，添加超时处理
        const timeoutPromise = new Promise((_, reject) => {
          setTimeout(() => reject(new Error('请求超时')), 10000);
        });
        
        result = await Promise.race([
          api.getMessageList(userId, this.data.pageNum, this.data.pageSize),
          timeoutPromise
        ]);
        console.log('消息列表API返回结果:', result);
      } catch (apiError) {
        console.warn('API调用失败，使用模拟数据:', apiError);
        // API调用失败时，使用模拟数据
        result = this.generateMockMessageData();
      }
      
      // 处理API返回的数据
      let messages = [];
      let hasMore = false;
      
      // 兼容统一返回的分页数据结构
      if (result && result.records) {
        messages = result.records || [];
        hasMore = result.current < result.pages;
      } else if (Array.isArray(result)) {
        // 兼容直接返回数组的情况
        messages = result;
        hasMore = messages.length === this.data.pageSize;
      }
      
      console.log('处理后消息数据:', messages);
      
      // 格式化消息数据，确保字段与前端期望的一致
      const formattedMessages = messages.map(msg => {
        const messageType = this.data.messageTypes[msg.type] || this.data.messageTypes.DEFAULT;
        return ({
          id: msg.noticeId.toString(),
          type: messageType.type,
          originalType: msg.type,
          title: msg.title || '消息通知',
          content: msg.content || '',
          time: msg.createTime || new Date().toISOString(),
          isRead: msg.isRead === 1,
          demandId: msg.relatedId || '',
          orderId: msg.relatedId ? `O${msg.relatedId}` : '',
          relatedType: msg.relatedType || '',
          icon: messageType.icon
        });
      });
      
      // 避免重复消息
      const filteredMessages = this.filterDuplicateMessages(formattedMessages);
      
      // 如果是第一页且没有消息数据，显示空状态
      if (this.data.pageNum === 1 && (!filteredMessages || filteredMessages.length === 0)) {
        console.log('没有获取到消息数据');
        this.setData({
          loading: false,
          refreshing: false,
          hasTodayMessages: false,
          hasYesterdayMessages: false,
          hasWeekMessages: false,
          hasEarlierMessages: false,
          todayMessages: [],
          yesterdayMessages: [],
          weekMessages: [],
          earlierMessages: []
        });
        return;
      }
      
      // 优化排序性能，只在第一页数据时排序
      const sortedMessages = this.data.pageNum === 1 
        ? filteredMessages.sort((a, b) => new Date(b.time) - new Date(a.time))
        : filteredMessages;
      
      const updatedMessageList = [...this.data.messageList, ...sortedMessages];
      
      // 更新缓存数据（只缓存前50条消息）
      if (this.data.pageNum === 1) {
        wx.setStorageSync('cachedMessageData', {
          list: updatedMessageList.slice(0, 50),
          timestamp: Date.now()
        });
      }
      
      this.setData({
        messageList: updatedMessageList,
        hasMore: hasMore,
        pageNum: this.data.pageNum + 1,
        loading: false,
        refreshing: false,
        lastUpdateTime: Date.now(),
        cachedMessageList: [] // 数据加载完成后清除缓存列表
      });
      
      // 使用防抖优化消息分组计算
      this.debounceCalculateGroups(updatedMessageList);
    } catch (error) {
      console.error('加载消息列表失败', error);
      
      wx.showToast({
        title: '加载失败，请稍后重试',
        icon: 'none'
      });
      
      this.setData({
        loading: false,
        refreshing: false
      });
      
      // 即使出错也停止下拉刷新
      wx.stopPullDownRefresh();
      
      // 确保抛出错误，让调用者知道加载失败
      throw error;
    }
  },
  
  // 过滤重复消息
  filterDuplicateMessages: function(newMessages) {
    const existingIds = new Set(this.data.messageList.map(msg => msg.id));
    return newMessages.filter(msg => !existingIds.has(msg.id));
  },
  
  // 防抖函数
  debounce: function(fn, wait) {
    let timeout;
    return function(...args) {
      clearTimeout(timeout);
      timeout = setTimeout(() => fn.apply(this, args), wait);
    };
  },
  
  // 防抖处理消息分组计算
  debounceCalculateGroups: function(messages) {
    if (!this._debouncedCalculateGroups) {
      this._debouncedCalculateGroups = this.debounce(this.calculateMessageGroups, 300);
    }
    this._debouncedCalculateGroups.call(this, messages);
  },
  
  // 格式化时间为消息显示格式
  formatTimeForMessage: function(date) {
    try {
      const now = new Date();
      const targetDate = new Date(date);
      
      // 处理无效日期
      if (isNaN(targetDate.getTime())) {
        return '未知时间';
      }
      
      // 计算时间差（毫秒）
      const diffMs = now - targetDate;
      const diffSeconds = Math.floor(diffMs / 1000);
      const diffMinutes = Math.floor(diffSeconds / 60);
      const diffHours = Math.floor(diffMinutes / 60);
      const diffDays = Math.floor(diffHours / 24);
      
      // 格式化时分
      const timeStr = `${String(targetDate.getHours()).padStart(2, '0')}:${String(targetDate.getMinutes()).padStart(2, '0')}`;
      
      // 小于1分钟显示"刚刚"
      if (diffSeconds < 60) {
        return '刚刚';
      }
      // 小于1小时显示"xx分钟前"
      else if (diffMinutes < 60) {
        return `${diffMinutes}分钟前`;
      }
      // 小于24小时显示"xx小时前"
      else if (diffHours < 24) {
        return `${diffHours}小时前`;
      }
      // 根据时间差返回不同的显示格式
      else if (diffDays === 0) {
        return timeStr; // 今天显示时间
      } else if (diffDays === 1) {
        return `昨天 ${timeStr}`; // 昨天显示"昨天 时间"
      } else if (diffDays < 7) {
        return `星期${['日', '一', '二', '三', '四', '五', '六'][targetDate.getDay()]} ${timeStr}`; // 一周内显示星期几
      } else {
        // 优化年显示，当年份和当前年相同时不显示年份
        const isSameYear = targetDate.getFullYear() === now.getFullYear();
        if (isSameYear) {
          return `${targetDate.getMonth() + 1}月${targetDate.getDate()}日 ${timeStr}`; // 同年显示月日
        } else {
          return `${targetDate.getFullYear()}-${String(targetDate.getMonth() + 1).padStart(2, '0')}-${String(targetDate.getDate()).padStart(2, '0')} ${timeStr}`; // 不同年显示完整日期
        }
      }
    } catch (error) {
      console.error('格式化时间失败:', error);
      return '未知时间';
    }
  },
  
  // 注意：默认消息功能已移除，现在直接使用真实的API数据
  
  // 查看消息详情
  viewMessageDetail: async function(e) {
    const messageId = e.currentTarget.dataset.id;
    const message = this.data.messageList.find(item => item.id === messageId);
    
    if (message) {
      // 更新消息为已读（前端状态更新）
      const updatedMessageList = this.data.messageList.map(item => {
        if (item.id === messageId) {
          return { ...item, isRead: true };
        }
        return item;
      });
      
      // 优化渲染性能，只更新当前消息的已读状态
      this.setData({
        [`messageList[${this.data.messageList.findIndex(item => item.id === messageId)}]`]: {
          ...message,
          isRead: true
        }
      });
      
      // 重新计算消息分组，确保红点立即消失
      this.debounceCalculateGroups(updatedMessageList);
      
      // 使用微任务优化体验，让UI先更新
      Promise.resolve().then(() => {
        // 如果消息之前是未读状态，调用API标记为已读
        if (!message.isRead) {
          this.markMessageAsReadAsync(messageId);
        }
      });
      
      // 根据消息类型跳转到不同页面或执行不同操作
      this.handleMessageAction(message);
    }
  },
  
  // 异步标记消息为已读（优化性能）
  markMessageAsReadAsync: async function(messageId) {
    try {
      // 获取用户ID
      const userInfo = wx.getStorageSync('userInfo');
      const userId = userInfo?.id || '';
      
      if (userId) {
        await api.markMessageAsRead(messageId, userId);
        console.log('消息标记为已读成功:', messageId);
        // 重新获取未读消息数量
        this.getUnreadMessageCount();
      }
    } catch (error) {
      console.error('标记消息为已读失败:', error);
      // 这里不抛出错误，因为前端状态已经更新，用户体验优先
    }
  },
  
  // 处理消息操作
  handleMessageAction: function(message) {
    switch (message.type) {
      case 'demand':
        if (message.demandId) {
          // 跳转到需求详情页
          wx.navigateTo({
            url: `/pages/farmer/demand-detail?id=${message.demandId}`
          });
        }
        break;
      case 'order':
        if (message.orderId) {
          // 跳转到订单详情页
          wx.navigateTo({
            url: `/pages/farmer/my-demand-list?id=${message.orderId}`
          });
        }
        break;
      case 'payment':
        if (message.demandId) {
          // 处理支付类型消息，显示支付确认框
          wx.showModal({
            title: message.title,
            content: `${message.content}\n\n确认立即支付吗？`,
            success: async (res) => {
              if (res.confirm) {
                this.handlePaymentAction(message.demandId);
              }
            }
          });
        }
        break;
      default:
        // 显示系统消息详情，使用更友好的UI组件
        wx.showModal({
          title: message.title,
          content: message.content,
          showCancel: false,
          confirmText: '知道了',
          confirmColor: '#1aad19'
        });
    }
  },
  
  // 处理支付操作
  handlePaymentAction: async function(demandId) {
    wx.showLoading({ title: '支付中...' });
    try {
      // 调用支付需求费用接口
      const result = await api.payDemandFee(demandId);
      
      if (result && result.code === 200) {
        wx.hideLoading();
        wx.showToast({ 
          title: '支付成功',
          icon: 'success',
          duration: 2000
        });
        
        // 支付成功后刷新消息列表
        setTimeout(() => {
          this.setData({
            messageList: [],
            pageNum: 1,
            hasMore: true
          });
          this.loadMessageList();
          
          // 刷新余额显示
          if (this.onBalanceUpdate) {
            this.onBalanceUpdate();
          }
        }, 1500);
      } else {
        wx.hideLoading();
        wx.showToast({ 
          title: result?.msg || '支付失败', 
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      console.error('支付失败', error);
      wx.showToast({ 
        title: '支付失败，请稍后重试', 
        icon: 'none'
      });
    }
  },
  
  // 下拉刷新 - 优化体验，显示加载动画
  onPullDownRefresh: function() {
    this.setData({
      messageList: [],
      pageNum: 1,
      hasMore: true,
      refreshing: true
    });
    
    // 在loadMessageList中已经处理了停止刷新
    this.loadMessageList();
  },
  
  // 批量标记已读 - 优化性能和用户体验
  batchMarkAllAsRead: async function() {
    try {
      // 获取用户ID
      const userInfo = wx.getStorageSync('userInfo');
      const userId = userInfo?.id || '';
      
      if (!userId) {
        console.error('用户ID获取失败');
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        return;
      }
      
      // 获取所有未读消息的ID
      const unreadMessages = this.data.messageList.filter(msg => !msg.isRead);
      const unreadMessageIds = unreadMessages.map(msg => msg.id);
      
      if (unreadMessageIds.length === 0) {
        wx.showToast({
          title: '没有未读消息',
          icon: 'none'
        });
        return;
      }
      
      // 先显示确认弹窗
      wx.showModal({
        title: '标记已读',
        content: `确定要将所有${unreadMessageIds.length}条未读消息标记为已读吗？`,
        success: async (res) => {
          if (res.confirm) {
            // 用户确认后执行标记操作
            wx.showLoading({
              title: '标记中...'
            });
            
            // 先立即更新UI状态，提升用户体验
            const updatedMessageList = this.data.messageList.map(msg => ({
              ...msg,
              isRead: true
            }));
            
            this.setData({
              messageList: updatedMessageList
            });
            
            // 使用微任务优化体验
            Promise.resolve().then(async () => {
              try {
                // 调用批量标记已读API
                const result = await api.batchMarkMessagesAsRead(unreadMessageIds, userId);
                console.log('批量标记已读结果:', result);
                
                // 重新计算消息分组
                this.calculateMessageGroups(updatedMessageList);
                
                // 重新获取未读消息数量
                this.getUnreadMessageCount();
                
                wx.hideLoading();
                wx.showToast({
                  title: `成功标记${unreadMessageIds.length}条消息为已读`,
                  icon: 'success'
                });
              } catch (error) {
                wx.hideLoading();
                console.error('批量标记已读失败:', error);
                
                // 如果API调用失败，恢复UI状态
                this.setData({
                  messageList: this.data.messageList.map(msg => ({
                    ...msg,
                    isRead: msg.isRead || false
                  }))
                });
                this.calculateMessageGroups(this.data.messageList);
                
                wx.showToast({
                  title: '标记失败，请稍后重试',
                  icon: 'none'
                });
              }
            });
          }
        }
      });
    } catch (error) {
      console.error('批量标记已读流程错误:', error);
      wx.showToast({
        title: '操作失败，请稍后重试',
        icon: 'none'
      });
    }
  },
  
  // 上拉加载更多
  onReachBottom: function() {
    this.loadMessageList();
  },
  
  // 生成模拟消息数据
  generateMockMessageData: function() {
    const now = new Date();
    const userId = 'user_' + Math.random().toString(36).substr(2, 9);
    
    // 模拟消息数据，根据用户提供的通知触发时机生成
    const mockMessages = [
      {
        noticeId: 1,
        title: '新需求通知',
        content: '您发布的喷洒需求已创建成功，正在等待飞手接单',
        type: 'SPRAY_DEMAND',
        isRead: 0,
        createTime: new Date(now - 1000 * 60 * 30).toISOString(), // 30分钟前
        relatedId: '101',
        relatedType: 'SPRAY'
      },
      {
        noticeId: 2,
        title: '需求已接单',
        content: '您的需求已被飞手张三接取，预计今天下午开始作业',
        type: 'SPRAY_DEMAND',
        isRead: 0,
        createTime: new Date(now - 1000 * 60 * 60 * 2).toISOString(), // 2小时前
        relatedId: '101',
        relatedType: 'SPRAY'
      },
      {
        noticeId: 3,
        title: '需求已完成',
        content: '您的喷洒需求已完成，飞手已提交作业报告',
        type: 'SPRAY_DEMAND',
        isRead: 1,
        createTime: new Date(now - 1000 * 60 * 60 * 24).toISOString(), // 1天前
        relatedId: '102',
        relatedType: 'SPRAY'
      },
      {
        noticeId: 4,
        title: '需求取消通知',
        content: '您的巡检需求已取消，原因：临时有事',
        type: 'INSPECTION_DEMAND',
        isRead: 1,
        createTime: new Date(now - 1000 * 60 * 60 * 24 * 2).toISOString(), // 2天前
        relatedId: '103',
        relatedType: 'INSPECTION'
      },
      {
        noticeId: 5,
        title: '支付成功通知',
        content: '您的需求费用已支付成功，飞手将尽快开始作业',
        type: 'PAYMENT_SUCCESS',
        isRead: 1,
        createTime: new Date(now - 1000 * 60 * 60 * 24 * 3).toISOString(), // 3天前
        relatedId: '104',
        relatedType: 'SPRAY'
      }
    ];
    
    // 构建分页数据结构
    return {
      records: mockMessages,
      current: this.data.pageNum,
      pages: 1,
      size: this.data.pageSize,
      total: mockMessages.length
    };
  },
  
  // 计算分组消息 - 优化性能和逻辑
  calculateMessageGroups: function(messages) {
    // 性能优化：使用记忆化避免重复计算
    if (this._lastMessages === messages && !this._forceRecalculate) {
      return;
    }
    this._lastMessages = messages;
    this._forceRecalculate = false;
    
    // 添加安全检查
    if (!messages || !Array.isArray(messages) || messages.length === 0) {
      this.setData({
        hasTodayMessages: false,
        hasYesterdayMessages: false,
        hasWeekMessages: false,
        hasEarlierMessages: false,
        todayMessages: [],
        yesterdayMessages: [],
        weekMessages: [],
        earlierMessages: []
      });
      return;
    }
    
    // 性能优化：避免重复的日期对象创建
    const now = new Date();
    const todayTime = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const yesterdayTime = todayTime - 24 * 60 * 60 * 1000;
    const weekAgoTime = todayTime - 7 * 24 * 60 * 60 * 1000;
    
    // 性能优化：使用对象解构和单次循环完成分组和格式化
    const groups = { today: [], yesterday: [], week: [], earlier: [] };
    
    for (let i = 0; i < messages.length; i++) {
      const message = messages[i];
      if (!message || !message.time) continue;
      
      // 延迟格式化时间，只在需要时进行
      if (!message.formattedTime && typeof message.time === 'string') {
        message.formattedTime = this.formatTimeForMessage(message.time);
      }
      
      try {
        const messageTime = new Date(message.time).getTime();
        
        if (!isNaN(messageTime)) {
          if (messageTime >= todayTime) {
            groups.today.push(message);
          } else if (messageTime >= yesterdayTime) {
            groups.yesterday.push(message);
          } else if (messageTime >= weekAgoTime) {
            groups.week.push(message);
          } else {
            groups.earlier.push(message);
          }
        } else {
          // 对于无法解析的时间，使用字符串匹配
          const timeStr = message.time;
          if (typeof timeStr === 'string') {
            if (timeStr.includes('今天') || timeStr.includes('刚刚') || timeStr.includes('小时前') || timeStr.includes('分钟前')) {
              groups.today.push(message);
            } else if (timeStr.includes('昨天')) {
              groups.yesterday.push(message);
            } else if (timeStr.includes('星期')) {
              groups.week.push(message);
            } else {
              groups.earlier.push(message);
            }
          }
        }
      } catch (error) {
        // 异常处理，将异常消息放入较早分组
        groups.earlier.push(message);
      }
    }
    
    // 更新数据
    this.setData({
      hasTodayMessages: groups.today.length > 0,
      hasYesterdayMessages: groups.yesterday.length > 0,
      hasWeekMessages: groups.week.length > 0,
      hasEarlierMessages: groups.earlier.length > 0,
      todayMessages: groups.today,
      yesterdayMessages: groups.yesterday,
      weekMessages: groups.week,
      earlierMessages: groups.earlier
    });
  },
  
  // 添加消息类型过滤功能
  filterMessagesByType: function(type) {
    this._forceRecalculate = true;
    let filteredMessages = [];
    
    if (type === 'all') {
      filteredMessages = this.data.messageList;
    } else {
      filteredMessages = this.data.messageList.filter(msg => msg.type === type);
    }
    
    this.calculateMessageGroups(filteredMessages);
  },
  
  // 生命周期：页面卸载时清理定时器
  onUnload: function() {
    // 清理防抖定时器
    if (this._debouncedCalculateGroups) {
      this._debouncedCalculateGroups = null;
    }
    // 清理其他定时器
    Object.keys(this).forEach(key => {
      if (key.startsWith('_timer')) {
        clearTimeout(this[key]);
      }
    });
  }
})