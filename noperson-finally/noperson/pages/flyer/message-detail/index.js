// 飞手的消息详情页面
Page({
  data: {
    messageId: '',
    messageInfo: {
      id: '',
      type: 'order',
      title: '',
      content: '',
      time: '',
      isRead: true,
      orderName: ''
    },
    messageContent: '', // 格式化后的消息内容
    showActionButtons: false,
    loading: true
  },

  onLoad: function(options) {
    // 从URL参数中获取消息ID
    if (options.id) {
      this.setData({
        messageId: options.id
      });
      
      // 加载消息详情
      this.loadMessageDetail();
    } else {
      // 如果没有消息ID，显示错误提示
      wx.showToast({
        title: '消息ID不存在',
        icon: 'error',
        duration: 2000
      });
      
      this.setData({
        loading: false
      });
    }
  },

  // 加载消息详情
  loadMessageDetail: function() {
    this.setData({ loading: true });
    
    // 模拟从服务器加载消息详情
    setTimeout(() => {
      // 这里应该调用API获取真实消息详情
      // 暂时使用模拟数据
      const mockMessageInfo = this.generateMockMessageDetail();
      
      // 格式化消息内容，添加换行
      const formattedContent = this.formatMessageContent(mockMessageInfo.content);
      
      // 判断是否显示操作按钮
      const showActionButtons = this.shouldShowActionButtons(mockMessageInfo);
      
      this.setData({
        messageInfo: mockMessageInfo,
        messageContent: formattedContent,
        showActionButtons: showActionButtons,
        loading: false
      });
    }, 1000);
  },

  // 生成模拟消息详情数据
  generateMockMessageDetail: function() {
    // 根据传入的消息ID生成模拟数据
    const messageId = this.data.messageId;
    const types = ['order', 'demand', 'payment', 'system'];
    
    // 简单的哈希函数，根据消息ID生成相对稳定的随机数
    const hashId = messageId.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
    const randomSeed = hashId % 1000;
    
    const type = types[randomSeed % types.length];
    
    // 根据消息类型生成不同的标题和内容
    let title = '';
    let content = '';
    let orderName = '';
    
    if (type === 'order') {
      title = '订单状态更新通知';
      content = `尊敬的飞手用户，您好！\n\n您的订单${'ORD' + (100000 + randomSeed % 1000)}状态已更新为服务中。请您及时准备设备，按照订单要求的时间和地点提供服务。\n\n如有任何问题，请联系客服。`;
      orderName = '订单' + (100000 + randomSeed % 1000);
    } else if (type === 'demand') {
      title = '附近新需求通知';
      content = `尊敬的飞手用户，您好！\n\n您附近有${randomSeed % 10 + 1}个新的农业服务需求，其中${randomSeed % 5 + 1}个是喷洒需求，${randomSeed % 5 + 1}个是巡检需求。请查看需求详情，及时接单。\n\n点击下方按钮，立即查看需求详情。`;
    } else if (type === 'payment') {
      title = '订单结算通知';
      content = `尊敬的飞手用户，您好！\n\n您的订单${'ORD' + (100000 + randomSeed % 1000)}已完成结算，结算金额为¥${(randomSeed * 10 + 100).toFixed(2)}。该金额已存入您的账户余额，请查收。\n\n感谢您的优质服务！`;
      orderName = '订单' + (100000 + randomSeed % 1000);
    } else {
      title = '系统公告';
      content = `尊敬的飞手用户，您好！\n\n农翼通平台将于${new Date(Date.now() + 86400000 * (randomSeed % 7 + 1)).toLocaleDateString()}进行系统升级维护，维护期间可能会出现短暂的服务中断。请您提前做好准备，给您带来的不便敬请谅解。\n\n如有任何问题，请联系客服。`;
    }
    
    // 生成时间
    const now = new Date();
    const messageDate = new Date(now.getTime() - randomSeed % 7 * 86400000);
    const formattedDate = `${messageDate.getFullYear()}-${String(messageDate.getMonth() + 1).padStart(2, '0')}-${String(messageDate.getDate()).padStart(2, '0')}`;
    const formattedTime = `${String(messageDate.getHours()).padStart(2, '0')}:${String(messageDate.getMinutes()).padStart(2, '0')}`;
    
    return {
      id: messageId,
      type: type,
      title: title,
      content: content,
      time: `${formattedDate} ${formattedTime}`,
      isRead: true,
      orderName: orderName
    };
  },

  // 格式化消息内容，将\n替换为实际的换行
  formatMessageContent: function(content) {
    // 小程序的text组件会自动解析\n为换行符
    return content;
  },

  // 判断是否显示操作按钮
  shouldShowActionButtons: function(messageInfo) {
    // 对于订单和需求类型的消息，可以显示操作按钮
    return messageInfo.type === 'order' || messageInfo.type === 'demand';
  },

  // 查看相关订单
  viewRelatedOrder: function() {
    if (this.data.messageInfo.orderName) {
      // 这里应该获取订单ID，然后跳转到订单详情页
      const orderId = 'ORD' + this.data.messageInfo.orderName.replace('订单', '');
      wx.navigateTo({
        url: `/pages/flyer/order-detail/index?id=${orderId}`
      });
    }
  },

  // 处理操作
  handleAction: function() {
    if (this.data.messageInfo.type === 'order') {
      // 订单类型的操作
      if (this.data.messageInfo.orderName) {
        const orderId = 'ORD' + this.data.messageInfo.orderName.replace('订单', '');
        wx.navigateTo({
          url: `/pages/flyer/order-detail/index?id=${orderId}`
        });
      }
    } else if (this.data.messageInfo.type === 'demand') {
      // 需求类型的操作
      wx.navigateTo({
        url: '/pages/flyer/index'
      });
    }
  }
});