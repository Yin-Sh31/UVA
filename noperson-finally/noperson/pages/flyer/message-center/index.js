// 飞手的消息中心页面 - 会话列表
Page({
  data: {
    conversationList: [],
    loading: false,
    pageNum: 1,
    pageSize: 10,
    userId: '', // 当前用户ID
    hasMore: true
  },

  onLoad: function() {
    // 获取用户ID（从存储中获取）
    const flyerUserInfo = wx.getStorageSync('flyerUserInfo');
    const userId = flyerUserInfo?.id || flyerUserInfo?.userId || '123'; // 支持多种用户ID字段
    this.setData({ userId: userId });
    this.loadConversations();
  },

  // 加载会话列表
  loadConversations: function() {
    if (this.data.loading || !this.data.hasMore) {
      return;
    }

    this.setData({ loading: true });

    // 调用获取会话列表接口
    const { api } = require('../../../utils/request');
    api.getConversations(this.data.userId, this.data.pageNum, this.data.pageSize).then(res => {
      console.log('加载会话成功:', res);
      const conversations = res.data || res || [];
      
      if (conversations.length > 0) {
        // 处理头像URL
        const processedConversations = conversations.map(conv => {
          let avatar = conv.otherUserAvatar || '';
          avatar = avatar.trim().replace(/`/g, '');
          
          // 如果是临时路径，则不显示头像
          let validAvatar = '';
          if (avatar && avatar.indexOf('tmp') === -1) {
            // 如果是阿里云OSS的URL，使用代理接口访问
            if (avatar.includes('aliyuncs.com')) {
              validAvatar = `http://localhost:8082/file/proxy/${conv.otherUserId}/avatar`;
            } else {
              validAvatar = avatar;
            }
          }
          
          return {
            ...conv,
            validAvatar: validAvatar
          };
        });
        
        const newConversationList = [...this.data.conversationList, ...processedConversations];
        this.setData({
          conversationList: newConversationList,
          pageNum: this.data.pageNum + 1,
          hasMore: conversations.length === this.data.pageSize
        });
      } else {
        this.setData({ hasMore: false });
      }
    }).catch(err => {
      console.error('加载会话列表失败:', err);
      // 加载模拟数据
      this.loadMockConversations();
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  // 加载模拟会话数据
  loadMockConversations: function() {
    const mockConversations = [];
    const userTypes = [0, 1]; // 0-农户 1-飞手
    const userNames = ['张三', '李四', '王五', '赵六', '钱七'];
    const lastMessages = [
      '你好，请问能帮我处理一下农场的喷洒工作吗？',
      '好的，我明天就可以开始工作。',
      '具体位置在哪里？',
      '订单已经完成，谢谢合作！',
      '请查看一下需求详情'
    ];
    
    const baseTime = Date.now();
    
    for (let i = 0; i < 5; i++) {
      const randomIndex = (this.data.pageNum - 1) * 5 + i;
      const userType = userTypes[Math.floor(Math.random() * userTypes.length)];
      const userName = userNames[randomIndex % userNames.length];
      const lastMessage = lastMessages[i % lastMessages.length];
      const unreadCount = Math.floor(Math.random() * 5);
      const timeOffset = randomIndex * 60 * 60 * 1000; // 每小时递增
      
      mockConversations.push({
        conversationId: `conv_${this.data.userId}_${1000 + randomIndex}`,
        otherUserId: `${1000 + randomIndex}`,
        otherUserName: userName,
        otherUserAvatar: `/assets/images/user-avatar.png`,
        otherUserType: userType,
        lastMessageContent: lastMessage,
        lastMessageTime: new Date(baseTime - timeOffset).toISOString(),
        unreadCount: unreadCount,
        relatedOrderId: Math.random() > 0.3 ? `order_${Math.floor(Math.random() * 1000)}` : null
      });
    }
    
    const newConversationList = [...this.data.conversationList, ...mockConversations];
    this.setData({
      conversationList: newConversationList,
      pageNum: this.data.pageNum + 1,
      hasMore: this.data.pageNum < 3
    });
  },

  // 打开会话详情
  openConversation: function(e) {
    const conversation = e.currentTarget.dataset.conversation;
    
    wx.navigateTo({
      url: `/pages/flyer/conversation-detail/index?conversationId=${conversation.conversationId}&otherUserId=${conversation.otherUserId}&otherUserName=${encodeURIComponent(conversation.otherUserName)}&otherUserAvatar=${encodeURIComponent(conversation.otherUserAvatar)}&otherUserType=${conversation.otherUserType}`
    });
  },

  // 格式化时间
  formatTime: function(timeStr) {
    // 确保时间字符串格式兼容iOS
    let formattedTimeStr = timeStr;
    if (typeof timeStr === 'string') {
      // 替换空格为T，并添加Z以确保ISO格式
      formattedTimeStr = timeStr.replace(' ', 'T') + 'Z';
    }
    
    const date = new Date(formattedTimeStr);
    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.error('无效的时间格式:', timeStr);
      return '';
    }
    
    const now = new Date();
    const timeStrFormatted = `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    
    // 同一天显示时间
    if (date.toDateString() === now.toDateString()) {
      return timeStrFormatted;
    }
    
    // 昨天显示"昨天 时间"
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.toDateString() === yesterday.toDateString()) {
      return '昨天 ' + timeStrFormatted;
    }
    
    // 其他显示日期
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  },

  // 获取用户类型文本
  getUserTypeText: function(userType) {
    return userType === 0 ? '农户' : '飞手';
  },

  // 上拉加载更多
  onReachBottom: function() {
    this.loadConversations();
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    console.log('开始下拉刷新');
    this.setData({
      conversationList: [],
      pageNum: 1,
      hasMore: true
    });
    
    // 直接调用loadConversations
    this.loadConversations().then(() => {
      console.log('下拉刷新完成');
      wx.stopPullDownRefresh();
    }).catch(() => {
      console.log('下拉刷新失败，停止刷新');
      wx.stopPullDownRefresh();
    });
  },

  // 获取未读消息总数
  getUnreadCount: function() {
    let total = 0;
    this.data.conversationList.forEach(conv => {
      total += conv.unreadCount;
    });
    return total;
  }
});