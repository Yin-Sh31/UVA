// 飞手的会话详情页面
Page({
  data: {
    conversationId: '',
    otherUserId: '',
    otherUserName: '',
    otherUserAvatar: '',
    otherUserType: 0,
    messageList: [],
    inputContent: '',
    scrollTop: 0,
    loading: false,
    pageNum: 1,
    pageSize: 20,
    userId: '', // 当前用户ID
    hasMore: true
  },
  
  // 组件引用
  usingComponents: {
    'message-item': '../../../components/message-item/message-item'
  },

  onLoad: function(options) {
    // 获取用户ID（从存储中获取）
    const flyerUserInfo = wx.getStorageSync('flyerUserInfo');
    const userId = flyerUserInfo?.id || flyerUserInfo?.userId || '123'; // 支持多种用户ID字段
    
    this.setData({
      conversationId: options.conversationId || '',
      otherUserId: options.otherUserId || '',
      otherUserName: options.otherUserName || '用户',
      otherUserAvatar: options.otherUserAvatar || '/assets/images/user-avatar.png',
      otherUserType: parseInt(options.otherUserType) || 0,
      userId: userId
    });
    
    console.log('飞手聊天详情加载:', {
      userId,
      conversationId: this.data.conversationId,
      otherUserId: this.data.otherUserId,
      otherUserName: this.data.otherUserName
    });
    
    // 设置导航栏标题
    wx.setNavigationBarTitle({
      title: this.data.otherUserName
    });
    
    // 加载消息列表
    this.loadMessages();
    
    // 标记消息为已读
    this.markAsRead();
  },

  // 加载消息列表
  loadMessages: function() {
    if (this.data.loading || !this.data.hasMore) {
      return;
    }
    
    this.setData({ loading: true });
    
    // 调用获取会话消息列表接口
    const { api } = require('../../../utils/request');
    api.getConversationMessages(
      this.data.conversationId,
      this.data.userId,
      this.data.pageNum,
      this.data.pageSize
    ).then(res => {
      console.log('获取消息成功:', res);
      const messages = res.data || res || [];
      
      if (messages.length > 0) {
        // 处理消息数据，确保头像正确
        const processedMessages = messages.map(msg => {
          const isFromSelf = msg.senderId === this.data.userId;
          return {
            ...msg,
            senderAvatar: isFromSelf 
              ? '/assets/images/user-avatar.png' 
              : (this.data.otherUserAvatar || '/assets/images/user-avatar.png'),
            isFromSelf: isFromSelf
          };
        });
        
        // 新消息添加到前面（历史消息）
        const newMessageList = [...processedMessages.reverse(), ...this.data.messageList];
        this.setData({
          messageList: newMessageList,
          pageNum: this.data.pageNum + 1,
          hasMore: messages.length === this.data.pageSize
        });
        
        // 如果是第一页，滚动到底部
        if (this.data.pageNum === 2) {
          this.scrollToBottom();
        }
      } else {
        this.setData({ hasMore: false });
      }
    }).catch(err => {
      console.error('加载消息失败:', err);
      // 模拟数据
      this.loadMockMessages();
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  // 加载模拟消息数据
  loadMockMessages: function() {
    const mockMessages = [];
    const messageCount = this.data.pageNum === 1 ? 10 : 5;
    
    for (let i = 0; i < messageCount; i++) {
      const isFromSelf = Math.random() > 0.5;
      const type = Math.floor(Math.random() * 3) + 1; // 1-文本 2-图片 3-订单相关
      const timeOffset = (this.data.pageNum - 1) * 10 + i;
      const messageDate = new Date(Date.now() - timeOffset * 60000 * (Math.random() * 60 + 1));
      
      mockMessages.push({
        messageId: `msg_${Date.now()}_${i}`,
        conversationId: this.data.conversationId,
        senderId: isFromSelf ? this.data.userId : this.data.otherUserId,
        senderName: isFromSelf ? '我' : this.data.otherUserName,
        senderAvatar: isFromSelf ? '/assets/images/user-avatar.png' : this.data.otherUserAvatar,
        receiverId: isFromSelf ? this.data.otherUserId : this.data.userId,
        receiverName: isFromSelf ? this.data.otherUserName : '我',
        messageType: type,
        content: isFromSelf 
          ? ['你好，请问有什么可以帮助你的？', '我已经准备好了，可以随时开始工作。', '请确认一下服务时间和地点。'][i % 3]
          : ['你好，我需要一项农业服务', '具体位置在XX农场', '大约什么时候可以开始？'][i % 3],
        isRead: true,
        relatedOrderId: type === 3 ? `order_${Math.floor(Math.random() * 1000)}` : null,
        createTime: messageDate.toISOString(),
        isFromSelf: isFromSelf
      });
    }
    
    const newMessageList = [...mockMessages.reverse(), ...this.data.messageList];
    this.setData({
      messageList: newMessageList,
      pageNum: this.data.pageNum + 1,
      hasMore: this.data.pageNum < 3
    });
    
    if (this.data.pageNum === 2) {
      this.scrollToBottom();
    }
  },

  // 发送消息
  sendMessage: function() {
    const content = this.data.inputContent.trim();
    if (!content) {
      return;
    }
    
    const tempMessage = {
      messageId: `temp_${Date.now()}`,
      conversationId: this.data.conversationId,
      senderId: this.data.userId,
      senderName: '我',
      senderAvatar: '/assets/images/user-avatar.png',
      receiverId: this.data.otherUserId,
      receiverName: this.data.otherUserName,
      messageType: 1,
      content: content,
      isRead: false,
      createTime: new Date().toISOString(),
      isFromSelf: true
    };
    
    // 先更新UI
    const newMessageList = [...this.data.messageList, tempMessage];
    this.setData({
      messageList: newMessageList,
      inputContent: ''
    });
    
    this.scrollToBottom();
    
    // 调用发送消息接口
    const { api } = require('../../../utils/request');
    api.sendMessage({
      userId: this.data.userId,
      receiverId: parseInt(this.data.otherUserId),
      content: content,
      messageType: 1
    }).then(res => {
      if (res && res.code === 200 && res.data) {
        // 更新临时消息为真实消息
        const updatedMessageList = this.data.messageList.map(msg => {
          if (msg.messageId === tempMessage.messageId) {
            return {
              ...res.data,
              isFromSelf: true
            };
          }
          return msg;
        });
        this.setData({ messageList: updatedMessageList });
      }
    }).catch(err => {
      console.error('发送消息失败:', err);
      wx.showToast({
        title: '发送失败，请重试',
        icon: 'none'
      });
      // 可以在这里移除临时消息或显示发送失败状态
    });
  },

  // 标记消息为已读
  markAsRead: function() {
    const { api } = require('../../../utils/request');
    api.markMessagesAsRead(this.data.conversationId, this.data.userId).then(res => {
      console.log('标记消息为已读成功:', res);
    }).catch(err => {
      console.error('标记已读失败:', err);
    });
  },

  // 滚动到底部
  scrollToBottom: function() {
    setTimeout(() => {
      const query = wx.createSelectorQuery();
      query.select('#message-list').boundingClientRect();
      query.exec((res) => {
        if (res && res[0]) {
          this.setData({
            scrollTop: res[0].height
          });
        }
      });
    }, 100);
  },

  // 输入框内容变化
  onInputChange: function(e) {
    this.setData({
      inputContent: e.detail.value
    });
  },

  // 上拉加载更多
  onReachTop: function() {
    this.loadMessages();
  },

  // 格式化时间
  formatTime: function(timeStr) {
    // 确保日期解析兼容iOS
    const date = new Date(timeStr.replace(' ', 'T') + 'Z');
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) {
      return '刚刚';
    } else if (diff < 3600000) {
      return Math.floor(diff / 60000) + '分钟前';
    } else if (diff < 86400000) {
      return Math.floor(diff / 3600000) + '小时前';
    } else {
      return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    }
  },
  
  // 处理头像点击
  onAvatarTap: function(e) {
    const userId = e.detail.userId;
    wx.navigateTo({
      url: `/pages/flyer/user-profile/index?id=${userId}`
    });
  },
  
  // 处理图片点击
  onImageTap: function(e) {
    const src = e.detail.src;
    wx.previewImage({
      urls: [src]
    });
  },
  
  // 处理订单点击
  onOrderTap: function(e) {
    const orderId = e.detail.orderId;
    wx.navigateTo({
      url: `/pages/flyer/order-detail/index?id=${orderId}`
    });
  }
});