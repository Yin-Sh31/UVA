// flyer/chat/list.js
const { api } = require('../../../utils/request');

Page({
  data: {
    conversationList: [],
    loading: false,
    refreshing: false,
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    userId: null
  },

  onLoad: function() {
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      userId: userInfo?.id || null
    });
    this.loadConversations();
  },

  onShow: function() {
    this.loadConversations();
  },

  onPullDownRefresh: function() {
    this.setData({
      conversationList: [],
      pageNum: 1,
      hasMore: true,
      refreshing: true
    });
    this.loadConversations();
  },

  onReachBottom: function() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadConversations();
    }
  },

  loadConversations: async function() {
    if (!this.data.hasMore || this.data.loading) {
      return;
    }

    this.setData({ loading: true });

    try {
      const userId = this.data.userId;
      if (!userId) {
        wx.showToast({
          title: '请先登录',
          icon: 'none'
        });
        this.setData({ loading: false });
        return;
      }

      console.log('加载会话列表，userId:', userId);

      // 先加载与管理员的会话
      const adminConversation = await api.checkAdminConversation(userId);
      console.log('管理员会话:', adminConversation);
      
      // 再加载其他会话
      const result = await api.getConversations(userId, this.data.pageNum, this.data.pageSize);
      console.log('获取会话列表结果:', result);
      const conversations = result.data || [];
      console.log('会话列表:', conversations);

      let allConversations = [];
      
      // 无论如何都添加管理员会话到列表开头
      if (adminConversation.data) {
        allConversations.push(adminConversation.data);
      } else {
        // 如果API调用失败，添加默认的管理员会话
        const defaultAdminConversation = {
          conversationId: `admin_${userId}`,
          otherUserId: 1,
          otherUserName: '管理员',
          otherUserAvatar: '',
          otherUserType: 4,
          lastMessageContent: '您可以在这里与管理员联系',
          lastMessageTime: new Date().toISOString(),
          unreadCount: 0,
          relatedOrderId: null,
          formattedTime: this.formatTime(new Date())
        };
        allConversations.push(defaultAdminConversation);
      }
      
      // 添加其他会话，排除管理员会话
      const otherConversations = conversations.filter(conv => conv.otherUserType !== 4);
      allConversations = [...allConversations, ...otherConversations];

      const formattedConversations = allConversations.map(conv => {
        // 处理头像 URL：移除空格和反引号
        let avatar = conv.otherUserAvatar || '';
        avatar = avatar.trim().replace(/`/g, '');
        
        // 如果是临时路径，则不显示头像
        let validAvatar = '';
        if (avatar && avatar.indexOf('tmp') === -1) {
          // 直接使用原始头像URL，不再通过代理接口
          validAvatar = avatar;
        }
        
        const result = {
          conversationId: conv.conversationId,
          otherUserId: conv.otherUserId,
          otherUserName: conv.otherUserName,
          otherUserAvatar: conv.otherUserAvatar || '',
          validAvatar: validAvatar,  // 添加处理后的有效头像 URL
          otherUserType: conv.otherUserType,
          lastMessageContent: conv.lastMessageContent || '',
          lastMessageTime: conv.lastMessageTime,
          unreadCount: conv.unreadCount || 0,
          relatedOrderId: conv.relatedOrderId,
          formattedTime: this.formatTime(conv.lastMessageTime)
        };
        console.log('单个会话数据:', result);
        return result;
      });

      console.log('格式化后的会话列表:', formattedConversations);

      this.setData({
        conversationList: this.data.pageNum === 1 ? formattedConversations : [...this.data.conversationList, ...formattedConversations],
        hasMore: conversations.length === this.data.pageSize,
        pageNum: this.data.pageNum + 1,
        loading: false,
        refreshing: false
      });

      console.log('更新后的会话列表长度:', this.data.conversationList.length);

      wx.stopPullDownRefresh();
    } catch (error) {
      console.error('获取会话列表失败', error);
      
      // 即使API调用失败，也添加默认的管理员会话
      const userId = this.data.userId;
      if (userId) {
        const defaultAdminConversation = {
          conversationId: `admin_${userId}`,
          otherUserId: 1,
          otherUserName: '管理员',
          otherUserAvatar: '',
          otherUserType: 4,
          lastMessageContent: '您可以在这里与管理员联系',
          lastMessageTime: new Date().toISOString(),
          unreadCount: 0,
          relatedOrderId: null,
          formattedTime: this.formatTime(new Date())
        };
        
        this.setData({
          conversationList: [defaultAdminConversation],
          loading: false,
          refreshing: false
        });
        console.log('添加默认管理员会话');
      } else {
        this.setData({
          loading: false,
          refreshing: false
        });
      }
      
      wx.stopPullDownRefresh();
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    }
  },

  formatTime: function(dateTime) {
    if (!dateTime) return '';

    const now = new Date();
    const date = new Date(dateTime);
    const diffMs = now - date;
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    const timeStr = `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;

    if (diffDays === 0) {
      return timeStr;
    } else if (diffDays === 1) {
      return '昨天 ' + timeStr;
    } else {
      return `${date.getMonth() + 1}月${date.getDate()}日`;
    }
  },

  goToChat: function(e) {
    console.log('goToChat e:', e);
    console.log('goToChat dataset:', e.currentTarget.dataset);
    const conversationId = e.currentTarget.dataset.conversationid;
    const otherUserId = e.currentTarget.dataset.otheruserid;
    const otherUserName = e.currentTarget.dataset.otherusername;
    const otherUserType = e.currentTarget.dataset.otherusertype;
    const otherUserAvatar = e.currentTarget.dataset.otheruseravatar;

    console.log('goToChat params:', { conversationId, otherUserId, otherUserName, otherUserType, otherUserAvatar });

    wx.navigateTo({
      url: `/pages/flyer/chat/detail?conversationId=${conversationId}&otherUserId=${otherUserId}&otherUserName=${encodeURIComponent(otherUserName)}&otherUserType=${otherUserType}&otherUserAvatar=${encodeURIComponent(otherUserAvatar || '')}`
    });
  },

  getUserTypeText: function(userType) {
    const typeMap = {
      1: '农户',
      2: '飞手',
      3: '机主',
      4: '管理员'
    };
    return typeMap[userType] || '用户';
  }
});
