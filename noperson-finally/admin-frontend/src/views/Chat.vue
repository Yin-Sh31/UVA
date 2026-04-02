<template>
  <div class="chat-container">
    <h2>聊天反馈管理</h2>
    
    <!-- 左侧聊天列表 -->
    <div class="chat-sidebar">
      <el-input v-model="searchKeyword" placeholder="搜索聊天" clearable class="search-input"></el-input>
      <el-list>
        <el-list-item 
          v-for="chat in chatList" 
          :key="chat.id"
          :class="{ active: selectedChatId === chat.id }"
          @click="selectChat(chat)"
        >
          <div class="chat-item">
            <div class="chat-avatar">{{ chat.name.charAt(0) }}</div>
            <div class="chat-info">
              <div class="chat-name">{{ chat.name }}</div>
              <div class="chat-last-message">{{ chat.lastMessage }}</div>
              <div class="chat-time">{{ chat.time }}</div>
            </div>
            <div v-if="chat.unread" class="unread-badge">{{ chat.unread }}</div>
          </div>
        </el-list-item>
      </el-list>
    </div>
    
    <!-- 右侧聊天内容 -->
    <div class="chat-main" v-if="selectedChat">
      <div class="chat-header">
        <div class="chat-title">{{ selectedChat.name }}</div>
        <div class="chat-subtitle">{{ selectedChat.subtitle }}</div>
      </div>
      <div class="chat-messages" ref="chatMessages">
        <div 
          v-for="message in selectedChat.messages" 
          :key="message.id"
          :class="['message-item', message.type]">
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>
      <div class="chat-input-area">
        <el-input
          v-model="messageInput"
          type="textarea"
          placeholder="输入消息..."
          :rows="3"
        ></el-input>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </div>
    </div>
    <div class="chat-empty" v-else>
      <el-empty description="请选择一个聊天"></el-empty>
    </div>
  </div>
</template>

<script>
import axios from '../utils/axios'

export default {
  data() {
    return {
      chatList: [],
      selectedChatId: null,
      selectedChat: null,
      messageInput: '',
      searchKeyword: ''
    }
  },
  mounted() {
    this.getChatList()
  },
  methods: {
    async getChatList() {
      try {
        // 调用后端API获取聊天列表
        const response = await axios.get('/admin/messages/conversations', {
          params: {
            page: 1,
            pageSize: 20
          }
        })
        
        if (response.code === 200) {
          this.chatList = response.data.map(chat => ({
            id: chat.conversationId,
            name: chat.otherUserName,
            otherUserId: chat.otherUserId,
            otherUserType: chat.otherUserType,
            relatedOrderId: chat.relatedOrderId,
            subtitle: this.getUserTypeText(chat.otherUserType) + (chat.relatedOrderId ? ` - 订单 #${chat.relatedOrderId}` : ''),
            lastMessage: chat.lastMessageContent,
            time: this.formatTime(chat.lastMessageTime),
            unread: chat.unreadCount
          }))
        }
      } catch (error) {
        console.error('获取聊天列表失败:', error)
      }
    },
    async selectChat(chat) {
      this.selectedChatId = chat.id
      try {
        // 调用后端API获取聊天详情
        const response = await axios.get(`/admin/messages/${chat.id}`, {
          params: {
            page: 1,
            pageSize: 100
          }
        })
        
        if (response.code === 200) {
          this.selectedChat = {
            ...chat,
            messages: response.data.map(msg => ({
              id: msg.messageId,
              type: msg.senderId === 1 ? 'sent' : 'received', // 假设管理员ID为1
              content: msg.content,
              time: this.formatTime(msg.createTime)
            }))
          }
          
          // 标记已读
          await this.markAsRead(chat.id)
          chat.unread = 0
          
          // 滚动到底部
          this.$nextTick(() => {
            const chatMessages = this.$refs.chatMessages
            if (chatMessages) {
              chatMessages.scrollTop = chatMessages.scrollHeight
            }
          })
        }
      } catch (error) {
        console.error('获取聊天详情失败:', error)
      }
    },
    async sendMessage() {
      if (!this.messageInput.trim()) return
      
      try {
        // 调用后端API发送消息
        const response = await axios.post('/admin/messages/send', {
          receiverId: this.selectedChat.otherUserId,
          content: this.messageInput,
          messageType: 1,
          relatedOrderId: this.selectedChat.relatedOrderId
        })
        
        if (response.code === 200) {
          // 添加新消息
          const newMessage = {
            id: response.data.messageId,
            type: 'sent',
            content: this.messageInput,
            time: this.formatTime(new Date())
          }
          this.selectedChat.messages.push(newMessage)
          
          // 清空输入框
          this.messageInput = ''
          
          // 更新聊天列表中的最后一条消息
          this.chatList.forEach(item => {
            if (item.id === this.selectedChatId) {
              item.lastMessage = newMessage.content
              item.time = newMessage.time
            }
          })
          
          // 滚动到底部
          this.$nextTick(() => {
            const chatMessages = this.$refs.chatMessages
            if (chatMessages) {
              chatMessages.scrollTop = chatMessages.scrollHeight
            }
          })
        }
      } catch (error) {
        console.error('发送消息失败:', error)
      }
    },
    async markAsRead(conversationId) {
      try {
        await axios.put(`/admin/messages/${conversationId}/read`)
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    },
    getUserTypeText(userType) {
      const typeMap = {
        1: '农户',
        2: '飞手',
        3: '机主',
        4: '管理员'
      }
      return typeMap[userType] || '用户'
    },
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) { // 24小时内
        return `${Math.floor(diff / 3600000)}小时前`
      } else if (diff < 604800000) { // 7天内
        return `${Math.floor(diff / 86400000)}天前`
      } else {
        return date.toLocaleDateString()
      }
    }
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 80px);
  padding: 20px;
}

.chat-sidebar {
  width: 300px;
  border-right: 1px solid #e4e7ed;
  padding-right: 10px;
}

.search-input {
  margin-bottom: 20px;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
}

.chat-item:hover {
  background-color: #f5f7fa;
}

.chat-item.active {
  background-color: #ecf5ff;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name {
  font-weight: bold;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-last-message {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

.unread-badge {
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 8px;
  font-size: 12px;
  min-width: 20px;
  text-align: center;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 20px;
}

.chat-header {
  padding: 10px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.chat-title {
  font-weight: bold;
  font-size: 16px;
}

.chat-subtitle {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0 10px;
}

.message-item {
  margin-bottom: 15px;
  display: flex;
}

.message-item.received {
  justify-content: flex-start;
}

.message-item.sent {
  justify-content: flex-end;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 18px;
  word-break: break-word;
}

.message-item.received .message-content {
  background-color: #f5f7fa;
  border-bottom-left-radius: 4px;
}

.message-item.sent .message-content {
  background-color: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-text {
  margin-bottom: 5px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.message-item.sent .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.chat-input-area {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.chat-input-area .el-input {
  margin-bottom: 10px;
}

.chat-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  border-radius: 4px;
  margin-left: 20px;
}
</style>