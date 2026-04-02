// flyer/chat/detail.js
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
    pageNum:1,
    pageSize: 20,
    hasMore: true,
    userId: null,
    toView: ''
  },

  onLoad: async function(options) {
    console.log('onLoad options:', options);
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      conversationId: options.conversationId || '',
      otherUserId: options.otherUserId || '',
      otherUserName: decodeURIComponent(options.otherUserName || ''),
      otherUserType: options.otherUserType || '',
      otherUserAvatar: decodeURIComponent(options.otherUserAvatar || ''),
      userId: userInfo?.id || null
    });

    console.log('onLoad data:', this.data);

    if (this.data.conversationId) {
      // 检查是否是默认的管理员聊天
      if (this.data.conversationId.startsWith('admin_')) {
        this.handleAdminConversation();
      } else {
        this.loadMessages();
        this.markAsRead();
      }
    } else if (this.data.otherUserId && this.data.userId) {
      // 如果没有会话ID，根据用户ID查找或创建会话
      console.log('根据用户ID查找或创建会话');
      await this.findOrCreateConversation();
    } else {
      console.error('缺少必要参数');
      wx.showToast({
        title: '参数错误',
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

  // 根据用户ID查找或创建会话
  findOrCreateConversation: async function() {
    try {
      const { userId, otherUserId } = this.data;
      
      console.log('查找会话:', { userId, otherUserId });
      
      // 先查询是否已经存在会话
      const sessions = await api.getConversations(userId);
      console.log('查询到的会话:', sessions);
      
      // 查找与对方用户的会话（不管对方是发送者还是接收者）
      let conversationId = null;
      if (sessions.code === 200 && sessions.data && sessions.data.length > 0) {
        console.log('会话列表详情:', sessions.data);
        
        // 查找对方用户ID匹配的会话
        const existingSession = sessions.data.find(session => 
          session.otherUserId == otherUserId
        );
        
        if (existingSession) {
          conversationId = existingSession.conversationId;
          console.log('找到现有会话:', conversationId);
        } else {
          console.log('没有找到匹配的会话');
        }
      }
      
      if (conversationId) {
        // 使用现有会话，加载消息
        this.setData({ conversationId });
        this.loadMessages();
        this.markAsRead();
      } else {
        console.log('没有找到现有会话，尝试根据用户ID对查找会话');
        
        // 尝试查询message表中是否存在两个用户之间的消息记录
        try {
          // 这里可以调用一个专门的接口来查找两个用户之间的会话
          // 如果没有这样的接口，可以尝试查询聊天记录
          console.log('尝试查询两个用户之间的聊天记录');
          
          // 先设置一个临时的会话ID，用于加载消息
          const tempConversationId = `temp_${userId}_${otherUserId}`;
          this.setData({ conversationId: tempConversationId });
          
          // 尝试加载消息，后端会根据两个用户ID查找会话
          await this.loadMessages();
          
          // 如果加载到消息，说明会话存在
          if (this.data.messages.length > 0) {
            console.log('加载到消息，会话存在');
            this.markAsRead();
          } else {
            console.log('没有找到消息记录，等待用户发送第一条消息时创建会话');
          }
        } catch (loadError) {
          console.error('查询会话失败:', loadError);
          console.log('等待用户发送第一条消息时创建会话');
        }
      }
    } catch (error) {
      console.error('查找或创建会话失败:', error);
      wx.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      });
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
          senderAvatar: isFromSelf 
            ? '/assets/images/user-avatar.png' 
            : (this.data.otherUserAvatar || '/assets/images/user-avatar.png'),
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
        senderAvatar: newMessage.senderAvatar || '',
        receiverId: newMessage.receiverId,
        receiverName: newMessage.receiverName,
        messageType: newMessage.messageType || 1,
        content: newMessage.content,
        isRead: newMessage.isRead,
        relatedOrderId: newMessage.relatedOrderId,
        createTime: newMessage.createTime,
        fromMySelf: newMessage.fromMySelf,
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
    const value = e.detail.value;
    console.log('输入框内容变化:', value);
    this.setData({
      inputValue: value
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
