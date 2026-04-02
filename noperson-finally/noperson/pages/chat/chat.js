// pages/chat/chat.js
const { api } = require('../../utils/request');

Page({
  data: {
    conversationId: null,
    targetUserId: null,
    targetUserName: '',
    targetUserAvatar: '',
    messages: [],
    inputMessage: '',
    hasContent: false,
    scrollToMessage: '',
    loading: false,
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    userInfo: null,
    isOnline: true,
    pollingTimer: null,
    lastMessageId: null,
    isTyping: false,
    showEmojiPanel: false,
    emojiList: ['😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂', '🙂', '🙃', '😉', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗', '😚', '😙', '🥲', '😋', '😛', '😜', '🤪', '😝', '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐', '😑', '😶', '😶‍🌫️', '😏', '😒', '🙄', '😬', '😮‍💨', '🤥', '😌', '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢', '🤮', '🤧', '🥵', '🥶', '🥴', '😵', '😵‍💫', '🤯', '🤠', '🥳', '🥸', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️', '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨', '😰', '😥', '😢', '😭', '😱', '😖', '😣', '😞', '😓', '😩', '😫', '🥱', '😤', '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹', '👺', '👻', '👽️', '👾', '🤖', '😺', '😸', '😹', '😻', '😼', '😽', '🙀', '😿', '😾', '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔', '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '👍', '👎', '👏', '🙌', '🤝', '👊', '✊', '🤛', '🤜', '🤞', '✌️', '🤟', '🤘', '👌', '🤌', '🤏', '👈', '👉', '👆', '👇', '☝️', '✋', '🤚', '🖐️', '🖖', '👋', '🤙', '💪', '🦾', '🖕', '✍️', '🙏', '🦶', '🦵', '🦿', '👂', '🦻', '👃', '🧠', '🫀', '🫁', '🦷', '🦴', '👀', '👁️', '👅', '👄', '💋', '🩸', '👶', '👧', '🧒', '👦', '👩', '🧑', '👨', '👩‍🦱', '🧑‍🦱', '👨‍🦱', '👩‍🦰', '🧑‍🦰', '👨‍🦰', '👱‍♀️', '👱', '👱‍♂️', '👩‍🦳', '🧑‍🦳', '👨‍🦳', '👩‍🦲', '🧑‍🦲', '👨‍🦲', '🧔‍♀️', '🧔', '🧔‍♂️', '👵', '🧓', '👴', '👲', '👳‍♀️', '👳', '👳‍♂️', '🧕', '👮‍♀️', '👮', '👮‍♂️', '👷‍♀️', '👷', '👷‍♂️', '💂‍♀️', '💂', '💂‍♂️', '🕵️‍♀️', '🕵️', '🕵️‍♂️', '👩‍⚕️', '🧑‍⚕️', '👨‍⚕️', '👩‍🌾', '🧑‍🌾', '👨‍🌾', '👩‍🍳', '🧑‍🍳', '👨‍🍳', '👩‍🎓', '🧑‍🎓', '👨‍🎓', '👩‍🎤', '🧑‍🎤', '👨‍🎤', '👩‍🏫', '🧑‍🏫', '👨‍🏫', '👩‍🏭', '🧑‍🏭', '👨‍🏭', '👩‍💻', '🧑‍💻', '👨‍💻', '👩‍💼', '🧑‍💼', '👨‍💼', '👩‍🔧', '🧑‍🔧', '👨‍🔧', '👩‍🔬', '🧑‍🔬', '👨‍🔬', '👩‍🎨', '🧑‍🎨', '👨‍🎨', '👩‍🚒', '🧑‍🚒', '👨‍🚒', '👩‍✈️', '🧑‍✈️', '👨‍✈️', '👩‍🚀', '🧑‍🚀', '👨‍🚀', '👩‍⚖️', '🧑‍⚖️', '👨‍⚖️', '👰‍♀️', '👰', '👰‍♂️', '🤵‍♀️', '🤵', '🤵‍♂️', '👸', '🤴', '🥷', '🦸‍♀️', '🦸', '🦸‍♂️', '🦹‍♀️', '🦹', '🦹‍♂️', '🤶', '🧑‍🎄', '🎅', '🧙‍♀️', '🧙', '🧙‍♂️', '🧝‍♀️', '🧝', '🧝‍♂️', '🧛‍♀️', '🧛', '🧛‍♂️', '🧟‍♀️', '🧟', '🧟‍♂️', '🧞‍♀️', '🧞', '🧞‍♂️', '🧜‍♀️', '🧜', '🧜‍♂️', '🧚‍♀️', '🧚', '🧚‍♂️', '👼', '🤰', '🤱', '👩‍🍼', '🧑‍🍼', '👨‍🍼', '🙇‍♀️', '🙇', '🙇‍♂️', '💁‍♀️', '💁', '💁‍♂️', '🙅‍♀️', '🙅', '🙅‍♂️', '🙆‍♀️', '🙆', '🙆‍♂️', '🙋‍♀️', '🙋', '🙋‍♂️', '🧏‍♀️', '🧏', '🧏‍♂️', '🤦‍♀️', '🤦', '🤦‍♂️', '🤷‍♀️', '🤷', '🤷‍♂️', '🙎‍♀️', '🙎', '🙎‍♂️', '🙍‍♀️', '🙍', '🙍‍♂️', '💇‍♀️', '💇', '💇‍♂️', '💆‍♀️', '💆', '💆‍♂️', '🧖‍♀️', '🧖', '🧖‍♂️', '💅', '🤳', '💃', '🕺', '👯‍♀️', '👯', '👯‍♂️', '🕴️', '👩‍🦽', '🧑‍🦽', '👨‍🦽', '👩‍🦼', '🧑‍🦼', '👨‍🦼', '🚶‍♀️', '🚶', '🚶‍♂️', '👩‍🦯', '🧑‍🦯', '👨‍🦯', '🧎‍♀️', '🧎', '🧎‍♂️', '🏃‍♀️', '🏃', '🏃‍♂️', '🧍‍♀️', '🧍', '🧍‍♂️', '👫', '👭', '👬', '👩‍❤️‍👨', '👩‍❤️‍👩', '👨‍❤️‍👨', '👩‍❤️‍💋‍👨', '👩‍❤️‍💋‍👩', '👨‍❤️‍💋‍👨', '👨‍👩‍👦', '👨‍👩‍👧', '👨‍👩‍👧‍👦', '👨‍👩‍👦‍👦', '👨‍👩‍👧‍👧', '👩‍👩‍👦', '👩‍👩‍👧', '👩‍👩‍👧‍👦', '👩‍👩‍👦‍👦', '👩‍👩‍👧‍👧', '👨‍👨‍👦', '👨‍👨‍👧', '👨‍👨‍👧‍👦', '👨‍👨‍👦‍👦', '👨‍👨‍👧‍👧', '👩‍👦', '👩‍👧', '👩‍👧‍👦', '👩‍👦‍👦', '👩‍👧‍👧', '👨‍👦', '👨‍👧', '👨‍👧‍👦', '👨‍👦‍👦', '👨‍👧‍👧', '🧶', '🧵', '🧥', '👚', '👕', '👖', '🩳', '👔', '👗', '👙', '🩱', '👘', '🥻', '🩲', '🥿', '👠', '👡', '👢', '👞', '👟', '🥾', '🥿', '👢', '👑', '👒', '🎩', '🎓', '🧢', '⛑️', '📿', '💄', '💍', '💎', '🔇', '🔈', '🔉', '🔊', '📢', '📣', '📯', '🔔', '🔕', '🎼', '🎵', '🎶', '🎙️', '🎚️', '🎛️', '🎤', '🎧', '📻', '🎷', '🎸', '🎹', '🎺', '🎻', '🪕', '🥁', '🪘', '📱', '📲', '☎️', '📞', '📟', '📠', '🔋', '🔌', '💻', '🖥️', '🖨️', '⌨️', '🖱️', '🖲️', '💽', '💾', '💿', '📀', '🧮', '🎥', '🎞️', '📽️', '🎬', '📺', '📷', '📸', '📹', '📼', '🔍', '🔎', '🕯️', '💡', '🔦', '🏮', '🪔', '📔', '📕', '📖', '📗', '📘', '📙', '📚', '📓', '📒', '📃', '📜', '📄', '📰', '🗞️', '📑', '🔖', '🏷️', '💰', '🪙', '💴', '💵', '💶', '💷', '💸', '💳', '🧾', '💹', '✉️', '📧', '📨', '📩', '📤', '📥', '📦', '📫', '📪', '📬', '📭', '📮', '🗳️', '✏️', '✒️', '🖋️', '🖊️', '🖌️', '🖍️', '📝', '💼', '📁', '📂', '🗂️', '📅', '📆', '🗒️', '🗓️', '📇', '📈', '📉', '📊', '📋', '📌', '📍', '📎', '🖇️', '📏', '📐', '✂️', '🗃️', '🗄️', '🗑️', '🔒', '🔓', '🔏', '🔐', '🔑', '🗝️', '🔨', '🪓', '⛏️', '⚒️', '🛠️', '🗡️', '⚔️', '🔫', '🪃', '🏹', '🛡️', '🪚', '🔧', '🪛', '🔩', '⚙️', '🗜️', '⚖️', '🦯', '🔗', '⛓️', '🪝', '🧰', '🧲', '🪜', '⚗️', '🧪', '🧫', '🧬', '🔬', '🔭', '📡', '💉', '🩸', '💊', '🩹', '🩺', '🌡️', '🏷️', '🚽', '🚰', '🚿', '🛁', '🛀', '🧴', '🧷', '🧹', '🧺', '🧻', '🧼', '🧽', '🧯', '🛒', '🚬', '⚰️', '⚱️', '🗿', '🪦', '🧿', '🪬', '💎', '🔮', '🪄', '🧿', '🪬', '🧸', '🪆', '🖼️', '🧵', '🪡', '🧶', '🛍️', '📿', '💎', '📿', '🎐', '🪅', '🎀', '🎁', '🎗️', '🎟️', '🎫', '🎖️', '🏆', '🏅', '🥇', '🥈', '🥉', '⚽', '⚾', '🥎', '🏀', '🏐', '🏈', '🏉', '🎾', '🥏', '🎳', '🏏', '🏑', '🏒', '🥍', '🏓', '🏸', '🥊', '🥋', '🥅', '⛳', '⛸️', '🎣', '🤿', '🎽', '🎿', '🛷', '🥌', '🎯', '🪀', '🪁', '🎱', '🔮', '🪄', '🎮', '🕹️', '🎰', '🎲', '🧩', '🧸', '🪆', '🖼️', '🎨', '🧵', '🪡', '🧶', '👓', '🕶️', '🥽', '🥼', '🦺', '👔', '👕', '👖', '🧣', '🧤', '🧥', '🧦', '👗', '👘', '🥻', '🩱', '🩲', '🩳', '👙', '👚', '👛', '👜', '👝', '🛍️', '🎒', '🩴', '👞', '👟', '🥾', '🥿', '👠', '👡', '🩰', '👢', '👑', '👒', '🎩', '🎓', '🧢', '🪖', '⛑️', '📿', '💄', '💍', '💎', '🔇', '🔈', '🔉', '🔊', '📢', '📣', '📯', '🔔', '🔕', '🎼', '🎵', '🎶', '🎙️', '🎚️', '🎛️', '🎤', '🎧', '📻', '🎷', '🎸', '🎹', '🎺', '🎻', '🪕', '🥁', '🪘', '📱', '📲', '☎️', '📞', '📟', '📠', '🔋', '🪫', '🔌', '💻', '🖥️', '🖨️', '⌨️', '🖱️', '🖲️', '💽', '💾', '💿', '📀', '🧮', '🎥', '🎞️', '📽️', '🎬', '📺', '📷', '📸', '📹', '📼', '🔍', '🔎', '🕯️', '💡', '🔦', '🏮', '🪔', '📔', '📕', '📖', '📗', '📘', '📙', '📚', '📓', '📒', '📃', '📜', '📄', '📰', '🗞️', '📑', '🔖', '🏷️', '💰', '🪙', '💴', '💵', '💶', '💷', '💸', '💳', '🧾', '💹', '✉️', '📧', '📨', '📩', '📤', '📥', '📦', '📫', '📪', '📬', '📭', '📮', '🗳️', '✏️', '✒️', '🖋️', '🖊️', '🖌️', '🖍️', '📝', '💼', '📁', '📂', '🗂️', '📅', '📆', '🗒️', '🗓️', '📇', '📈', '📉', '📊', '📋', '📌', '📍', '📎', '🖇️', '📏', '📐', '✂️', '🗃️', '🗄️', '🗑️', '🔒', '🔓', '🔏', '🔐', '🔑', '🗝️', '🔨', '🪓', '⛏️', '⚒️', '🛠️', '🗡️', '⚔️', '🔫', '🪃', '🏹', '🛡️', '🪚', '🔧', '🪛', '🔩', '⚙️', '🗜️', '⚖️', '🦯', '🔗', '⛓️', '🪝', '🧰', '🧲', '🪜', '⚗️', '🧪', '🧫', '🧬', '🔬', '🔭', '📡', '💉', '🩸', '💊', '🩹', '🩺', '🌡️', '🏷️', '🚽', '🚰', '🚿', '🛁', '🛀', '🧴', '🧷', '🧹', '🧺', '🧻', '🧼', '🧽', '🧯', '🛒', '🚬', '⚰️', '⚱️', '🗿', '🪦', '🧿', '🪬']
  },

  onLoad: function(options) {
    const { conversationId, targetUserId, targetUserName, targetUserAvatar } = options;
    
    this.setData({
      conversationId: conversationId || null,
      targetUserId: targetUserId || null,
      targetUserName: targetUserName || '聊天',
      targetUserAvatar: targetUserAvatar || '/assets/images/user-avatar.png'
    });

    // 获取当前用户信息
    this.loadUserInfo();
    
    // 加载聊天记录
    this.loadMessages();
    
    // 如果是新对话，创建会话
    if (!conversationId && targetUserId) {
      this.createConversation();
    }
    
    // 开始消息轮询
    this.startMessagePolling();
  },

  onShow: function() {
    // 页面显示时刷新消息
    if (this.data.conversationId) {
      this.loadNewMessages();
    }
  },
  
  onHide: function() {
    // 页面隐藏时停止轮询
    this.stopMessagePolling();
  },
  
  onUnload: function() {
    // 页面卸载时停止轮询
    this.stopMessagePolling();
  },

  // 加载用户信息
  loadUserInfo: function() {
    const userInfo = wx.getStorageSync('userInfo');
    this.setData({
      userInfo: userInfo
    });
  },

  // 创建会话
  createConversation: async function() {
    try {
      const res = await api.createConversation({
        targetUserId: this.data.targetUserId
      });
      
      if (res.data && res.data.conversationId) {
        this.setData({
          conversationId: res.data.conversationId
        });
        // 创建会话后开始轮询
        this.startMessagePolling();
      }
    } catch (error) {
      console.error('创建会话失败:', error);
    }
  },

  // 加载聊天记录
  loadMessages: async function() {
    if (!this.data.conversationId || this.data.loading) {
      return;
    }

    this.setData({ loading: true });

    try {
      const res = await api.getChatMessages({
        conversationId: this.data.conversationId,
        pageNum: this.data.pageNum,
        pageSize: this.data.pageSize
      });

      if (res.data && res.data.records) {
        const messages = res.data.records.reverse();
        
        // 处理消息，添加时间显示标记
        const processedMessages = this.processMessages(messages);
        
        this.setData({
          messages: this.data.pageNum === 1 ? processedMessages : [...processedMessages, ...this.data.messages],
          hasMore: messages.length === this.data.pageSize,
          loading: false,
          refreshing: false,
          lastMessageId: messages.length > 0 ? messages[messages.length - 1].id : null
        });

        // 停止下拉刷新
        wx.stopPullDownRefresh();

        // 滚动到底部
        if (this.data.pageNum === 1 && messages.length > 0) {
          setTimeout(() => {
            this.scrollToBottom();
          }, 100);
        }
      } else {
        this.setData({
          loading: false,
          refreshing: false
        });
        wx.stopPullDownRefresh();
      }
    } catch (error) {
      console.error('加载消息失败:', error);
      this.setData({ 
        loading: false,
        refreshing: false
      });
      wx.stopPullDownRefresh();
    }
  },
  
  // 处理消息，添加时间显示标记
  processMessages: function(messages) {
    const result = [];
    let lastTime = null;
    
    messages.forEach((msg, index) => {
      const msgTime = new Date(msg.createTime);
      const showTime = this.shouldShowTime(msgTime, lastTime);
      
      result.push({
        ...msg,
        isSelf: msg.senderId === this.data.userInfo?.id,
        showTime: showTime,
        formattedTime: this.formatMessageTime(msgTime)
      });
      
      if (showTime) {
        lastTime = msgTime;
      }
    });
    
    return result;
  },
  
  // 判断是否应该显示时间
  shouldShowTime: function(currentTime, lastTime) {
    if (!lastTime) return true;
    const diff = currentTime.getTime() - lastTime.getTime();
    return diff > 5 * 60 * 1000; // 超过5分钟显示时间
  },
  
  // 格式化消息时间
  formatMessageTime: function(date) {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const msgDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const diffDays = Math.floor((today - msgDate) / (1000 * 60 * 60 * 24));
    
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const timeStr = `${hours}:${minutes}`;
    
    if (diffDays === 0) {
      return timeStr;
    } else if (diffDays === 1) {
      return `昨天 ${timeStr}`;
    } else if (diffDays < 7) {
      const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
      return `${weekdays[date.getDay()]} ${timeStr}`;
    } else {
      return `${date.getMonth() + 1}月${date.getDate()}日 ${timeStr}`;
    }
  },

  // 加载更多消息
  loadMoreMessages: function() {
    if (!this.data.hasMore || this.data.loading) {
      return;
    }
    
    this.setData({
      pageNum: this.data.pageNum + 1
    }, () => {
      this.loadMessages();
    });
  },
  
  // 加载新消息（轮询使用）
  loadNewMessages: async function() {
    if (!this.data.conversationId) return;
    
    try {
      const res = await api.getChatMessages({
        conversationId: this.data.conversationId,
        pageNum: 1,
        pageSize: 50,
        lastMessageId: this.data.lastMessageId
      });

      if (res.data && res.data.records && res.data.records.length > 0) {
        const newMessages = res.data.records.reverse();
        const processedNewMessages = this.processMessages(newMessages);
        
        // 过滤掉已存在的消息
        const existingIds = new Set(this.data.messages.map(m => m.id));
        const uniqueNewMessages = processedNewMessages.filter(m => !existingIds.has(m.id));
        
        if (uniqueNewMessages.length > 0) {
          const allMessages = [...this.data.messages, ...uniqueNewMessages];
          this.setData({
            messages: allMessages,
            lastMessageId: allMessages[allMessages.length - 1].id
          });
          
          // 滚动到底部
          this.scrollToBottom();
          
          // 播放提示音（可选）
          this.playMessageSound();
        }
      }
    } catch (error) {
      console.error('加载新消息失败:', error);
    }
  },
  
  // 开始消息轮询
  startMessagePolling: function() {
    // 先停止之前的轮询
    this.stopMessagePolling();
    
    // 每5秒轮询一次
    const timer = setInterval(() => {
      this.loadNewMessages();
    }, 5000);
    
    this.setData({
      pollingTimer: timer
    });
  },
  
  // 停止消息轮询
  stopMessagePolling: function() {
    if (this.data.pollingTimer) {
      clearInterval(this.data.pollingTimer);
      this.setData({
        pollingTimer: null
      });
    }
  },
  
  // 播放消息提示音
  playMessageSound: function() {
    // 使用微信小程序的音频API播放提示音
    const innerAudioContext = wx.createInnerAudioContext();
    innerAudioContext.src = '/assets/sounds/message.mp3';
    innerAudioContext.play();
  },

  // 输入消息
  onInputMessage: function(e) {
    const value = e.detail.value;
    const hasContent = value && value.trim();
    console.log('Input value:', value);
    console.log('Has content:', hasContent);
    this.setData({
      inputMessage: value,
      hasContent: hasContent
    }, () => {
      console.log('inputMessage updated:', this.data.inputMessage);
      console.log('hasContent updated:', this.data.hasContent);
    });
  },

  // 发送消息
  sendMessage: async function() {
    const content = this.data.inputMessage ? this.data.inputMessage.trim() : '';
    if (!content) {
      return;
    }

    if (!this.data.conversationId) {
      wx.showToast({
        title: '会话未建立',
        icon: 'none'
      });
      return;
    }

    // 清空输入框
    this.setData({
      inputMessage: ''
    });

    // 添加本地消息（乐观更新）
    const tempId = Date.now();
    const tempMessage = {
      id: tempId,
      content: content,
      senderId: this.data.userInfo?.id,
      createTime: new Date().toISOString(),
      isSelf: true,
      status: 'sending',
      showTime: this.shouldShowTime(new Date(), this.getLastMessageTime()),
      formattedTime: this.formatMessageTime(new Date())
    };

    this.setData({
      messages: [...this.data.messages, tempMessage]
    });
    
    this.scrollToBottom();

    try {
      console.log('Sending message with:', {
        userId: this.data.userInfo?.id,
        receiverId: this.data.targetUserId,
        content: content,
        messageType: 1
      });
      const res = await api.sendMessage({
        userId: this.data.userInfo?.id,
        receiverId: this.data.targetUserId,
        content: content,
        messageType: 1
      });

      if (res.data) {
        // 更新消息状态
        const messages = this.data.messages.map(msg => {
          if (msg.id === tempId) {
            return { 
              ...res.data, 
              isSelf: true, 
              status: 'sent',
              showTime: msg.showTime,
              formattedTime: msg.formattedTime
            };
          }
          return msg;
        });

        this.setData({ 
          messages,
          lastMessageId: res.data.id
        });
      }
    } catch (error) {
      console.error('发送消息失败:', error);
      
      // 标记消息发送失败
      const messages = this.data.messages.map(msg => {
        if (msg.id === tempId) {
          return { ...msg, status: 'failed' };
        }
        return msg;
      });

      this.setData({ messages });
      
      wx.showToast({
        title: '发送失败',
        icon: 'none'
      });
    }
  },
  
  // 获取最后一条消息的时间
  getLastMessageTime: function() {
    const messages = this.data.messages;
    if (messages.length === 0) return null;
    
    for (let i = messages.length - 1; i >= 0; i--) {
      if (messages[i].showTime) {
        return new Date(messages[i].createTime);
      }
    }
    return null;
  },

  // 滚动到底部
  scrollToBottom: function() {
    const messages = this.data.messages;
    if (messages.length > 0) {
      this.setData({
        scrollToMessage: `msg-${messages[messages.length - 1].id}`
      });
    }
  },

  // 选择图片
  chooseImage: function() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.uploadImage(res.tempFilePaths[0]);
      }
    });
  },

  // 上传图片
  uploadImage: async function(filePath) {
    wx.showLoading({ title: '发送中...' });

    try {
      const res = await api.uploadChatImage(filePath);
      
      if (res.data && res.data.url) {
        // 发送图片消息
        await api.sendMessage({
          userId: this.data.userInfo?.id,
          receiverId: this.data.targetUserId,
          content: res.data.url,
          messageType: 2
        });

        // 刷新消息列表
        this.loadNewMessages();
      }
    } catch (error) {
      console.error('发送图片失败:', error);
      wx.showToast({
        title: '发送失败',
        icon: 'none'
      });
    } finally {
      wx.hideLoading();
    }
  },

  // 预览图片
  previewImage: function(e) {
    const url = e.currentTarget.dataset.url;
    const images = this.data.messages
      .filter(msg => msg.messageType === 'image')
      .map(msg => msg.content);
    
    wx.previewImage({
      current: url,
      urls: images
    });
  },

  // 重发失败的消息
  resendMessage: function(e) {
    const messageId = e.currentTarget.dataset.id;
    const message = this.data.messages.find(msg => msg.id === messageId);
    
    if (message && message.status === 'failed') {
      // 移除失败的消息
      const messages = this.data.messages.filter(msg => msg.id !== messageId);
      this.setData({
        messages,
        inputMessage: message.content
      });
    }
  },

  // 下拉刷新
  onRefresh: function() {
    this.setData({
      pageNum: 1,
      hasMore: true,
      refreshing: true
    }, () => {
      this.loadMessages();
    });
  },
  
  // 页面下拉刷新触发（系统自带）
  onPullDownRefresh: function() {
    this.onRefresh();
  },
  
  // 显示表情面板
  toggleEmojiPanel: function() {
    this.setData({
      showEmojiPanel: !this.data.showEmojiPanel
    });
  },
  
  // 选择表情
  selectEmoji: function(e) {
    const emoji = e.currentTarget.dataset.emoji;
    const newMessage = (this.data.inputMessage || '') + emoji;
    this.setData({
      inputMessage: newMessage
    });
  },
  
  // 隐藏表情面板
  hideEmojiPanel: function() {
    this.setData({
      showEmojiPanel: false
    });
  }
});
