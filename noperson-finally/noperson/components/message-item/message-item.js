Component({
  properties: {
    message: {
      type: Object,
      value: {}
    },
    currentUserId: {
      type: String,
      value: ''
    }
  },
  data: {
    formattedTime: '',
    isFromSelf: false
  },
  observers: {
    'message': function(message) {
      if (message) {
        // 计算是否是自己发送的消息
        const isFromSelf = message.isFromSelf || String(message.senderId) === String(this.data.currentUserId);
        // 格式化时间
        const formattedTime = this.formatMessageTime(message.createTime);
        
        this.setData({
          isFromSelf,
          formattedTime
        });
      }
    }
  },
  methods: {
    // 格式化消息时间
    formatMessageTime(timeString) {
      if (!timeString) return '';
      
      // 确保日期解析兼容iOS
      const date = new Date(timeString.replace(' ', 'T') + 'Z');
      const now = new Date();
      const diff = now - date;
      
      // 计算时间差（分钟）
      const minutesDiff = Math.floor(diff / (1000 * 60));
      
      // 小于1分钟
      if (minutesDiff < 1) {
        return '刚刚';
      }
      // 小于1小时
      else if (minutesDiff < 60) {
        return `${minutesDiff}分钟前`;
      }
      // 当天
      else if (date.toDateString() === now.toDateString()) {
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
      }
      // 昨天
      else {
        const yesterday = new Date(now);
        yesterday.setDate(now.getDate() - 1);
        if (date.toDateString() === yesterday.toDateString()) {
          return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        }
        // 本年
        else if (date.getFullYear() === now.getFullYear()) {
          return `${date.getMonth() + 1}月${date.getDate()}日 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
        }
        // 其他
        else {
          return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
        }
      }
    },
    
    // 点击消息头像
    onAvatarTap() {
      const { message, isFromSelf } = this.data;
      if (!isFromSelf && message.senderId) {
        this.triggerEvent('avatarTap', { userId: message.senderId });
      }
    },
    
    // 点击图片消息
    onImageTap(e) {
      const { src } = e.currentTarget.dataset;
      this.triggerEvent('imageTap', { src });
    },
    
    // 点击订单相关消息
    onOrderTap() {
      const { message } = this.data;
      if (message.relatedOrderId) {
        this.triggerEvent('orderTap', { orderId: message.relatedOrderId });
      }
    }
  }
});