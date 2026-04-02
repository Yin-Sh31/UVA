// farmer/chat/detail.js
const { api } = require('../../../utils/request');

Page({
  data: {
    conversationId: '',
    otherUserId: '',
    otherUserName: '',
    otherUserType: '',
    otherUserAvatar: '',
    messages: [],
    inputValue: '',
    loading: false,
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    userId: null,
    toView: ''
  },

  onLoad: function(options) {
    console.log('onLoad options:', options);
    const userInfo = wx.getStorageSync('userInfo');
    const otherUserAvatar = decodeURIComponent(options.otherUserAvatar || '');
    
    this.setData({
      conversationId: options.conversationId || '',
      otherUserId: options.otherUserId || '',
      otherUserName: decodeURIComponent(options.otherUserName || ''),
      otherUserType: options.otherUserType || '',
      otherUserAvatar: otherUserAvatar,
      userId: userInfo?.id || null
    });

    console.log('onLoad data:', this.data);
    console.log('头像详细信息:', {
      raw: options.otherUserAvatar,
      decoded: otherUserAvatar,
      type: typeof otherUserAvatar,
      length: otherUserAvatar.length,
      isEmpty: otherUserAvatar === '',
      hasContent: otherUserAvatar && otherUserAvatar.length > 0
    });

    if (this.data.conversationId) {
      // 检查是否是默认的管理员聊天
      if (this.data.conversationId.startsWith('admin_')) {
        this.handleAdminConversation();
      } else {
        this.loadMessages();
        this.markAsRead();
      }
    } else {
      console.error('没有会话ID');
      wx.showToast({
        title: '会话信息错误',
        icon: 'none'
      });
    }
  },

  handleAdminConversation: async function() {
    try {
      const userId = this.data.userId;
      if (!userId) return;

      // 尝试创建与管理员的会话
      const result = await api.sendMessage({
        userId: userId,
        receiverId: 1, // 管理员ID
        content: '',
        messageType: 1
      });

      if (result.code === 200) {
        // 更新会话ID为真实的会话ID
        this.setData({
          conversationId: result.data.conversationId
        });

        // 加载消息
        this.loadMessages();
        this.markAsRead();
      }
    } catch (error) {
      console.error('创建管理员会话失败', error);
      // 即使创建失败，也显示聊天界面
      this.loadMessages();
    }
  },

  onShow: function() {
    if (this.data.conversationId) {
      this.loadMessages();
    }
  },

  onPullDownRefresh: function() {
    this.setData({
      messages: [],
      pageNum: 1,
      hasMore: true
    });
    this.loadMessages();
  },

  onReachBottom: function() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadMessages();
    }
  },

  loadMessages: async function() {
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

      console.log('调用getConversationMessages:', {
        conversationId: this.data.conversationId,
        userId: userId,
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      });

      const result = await api.getConversationMessages(
        this.data.conversationId,
        userId,
        this.data.pageNum,
        this.data.pageSize
      );

      console.log('getConversationMessages result:', result);

      const messages = result.data || [];
      console.log('获取到的消息:', messages);
      
      const formattedMessages = messages.map((msg, index) => {
        const isFromSelf = msg.senderId === userId;
        return {
          messageId: msg.messageId,
          conversationId: msg.conversationId,
          senderId: msg.senderId,
          senderName: msg.senderName,
          senderAvatar: msg.senderAvatar || '',
          receiverId: msg.receiverId,
          receiverName: msg.receiverName,
          messageType: msg.messageType || 1,
          content: msg.content,
          isRead: msg.isRead,
          relatedOrderId: msg.relatedOrderId,
          createTime: msg.createTime,
          fromMySelf: isFromSelf,
          formattedTime: this.formatTime(msg.createTime),
          showTime: index === 0
        };
      });

      // 处理时间显示逻辑：两条消息间隔5分钟才显示时间
      for (let i = 1; i < formattedMessages.length; i++) {
        const currentTime = new Date(formattedMessages[i].createTime).getTime();
        const prevTime = new Date(formattedMessages[i - 1].createTime).getTime();
        const timeDiff = currentTime - prevTime;
        formattedMessages[i].showTime = timeDiff >= 5 * 60 * 1000;
      }

      console.log('格式化后的消息:', formattedMessages);

      const allMessages = this.data.pageNum === 1 ? formattedMessages : [...formattedMessages, ...this.data.messages];

      this.setData({
        messages: allMessages,
        hasMore: messages.length === this.data.pageSize,
        pageNum: this.data.pageNum + 1,
        loading: false,
        toView: allMessages.length > 0 ? 'msg-' + allMessages[allMessages.length - 1].messageId : ''
      });

      console.log('更新后的数据:', { messages: this.data.messages.length, hasMore: this.data.hasMore });

      wx.stopPullDownRefresh();
    } catch (error) {
      console.error('获取消息列表失败', error);
      this.setData({ loading: false });
      wx.stopPullDownRefresh();
      wx.showToast({
        title: '加载消息失败',
        icon: 'none'
      });
    }
  },

  markAsRead: async function() {
    try {
      const userId = this.data.userId;
      if (!userId) return;

      await api.markMessagesAsRead(this.data.conversationId, userId);
    } catch (error) {
      console.error('标记已读失败', error);
    }
  },

  sendMessage: async function() {
    const content = this.data.inputValue.trim();
    if (!content) {
      wx.showToast({
        title: '请输入消息内容',
        icon: 'none'
      });
      return;
    }

    const userId = this.data.userId;
    if (!userId) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    try {
      const result = await api.sendMessage({
        userId: userId,
        receiverId: parseInt(this.data.otherUserId),
        content: content,
        messageType: 1
      });

      const newMessage = result.data;

      const formattedMessage = {
        messageId: newMessage.messageId,
        conversationId: newMessage.conversationId,
        senderId: newMessage.senderId,
        senderName: newMessage.senderName,
        senderAvatar: '/assets/images/user-avatar.png',
        receiverId: newMessage.receiverId,
        receiverName: newMessage.receiverName,
        messageType: newMessage.messageType || 1,
        content: newMessage.content,
        isRead: newMessage.isRead,
        relatedOrderId: newMessage.relatedOrderId,
        createTime: newMessage.createTime,
        fromMySelf: true,
        formattedTime: this.formatTime(newMessage.createTime)
      };

      this.setData({
        messages: [...this.data.messages, formattedMessage],
        inputValue: '',
        toView: 'msg-' + formattedMessage.messageId
      });

      wx.pageScrollTo({
        scrollTop: 9999,
        duration: 300
      });
    } catch (error) {
      console.error('发送消息失败', error);
      wx.showToast({
        title: '发送失败，请重试',
        icon: 'none'
      });
    }
  },

  onInputChange: function(e) {
    this.setData({
      inputValue: e.detail.value
    });
  },

  formatTime: function(dateTime) {
    if (!dateTime) return '';

    const date = new Date(dateTime);
    const now = new Date();
    const dateStr = date.toDateString();
    const nowDateStr = now.toDateString();
    
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const timeStr = `${hours}:${minutes}`;

    // 今天
    if (dateStr === nowDateStr) {
      return timeStr;
    }

    // 昨天
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (dateStr === yesterday.toDateString()) {
      return `昨天 ${timeStr}`;
    }

    // 前天及更早
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}月${day}日 ${timeStr}`;
  },

  goBack: function() {
    wx.navigateBack();
  }
});
