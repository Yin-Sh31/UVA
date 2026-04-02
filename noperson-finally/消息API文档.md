# 消息API文档

## 概述

本文档描述了消息系统的API接口，用于支持农户、飞手、机主和管理员之间的聊天功能。

## 接口列表

### 1. 发送消息

**接口路径**：`POST /message/send`

**功能**：发送消息给指定用户

**请求参数**：
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| userId | Long | 是 | 当前用户ID |
| receiverId | Long | 是 | 接收者ID |
| content | String | 是 | 消息内容 |
| messageType | Integer | 否 | 消息类型，默认1 |
| relatedOrderId | Long | 否 | 关联订单ID |

**请求示例**：
```json
POST /message/send?userId=1
Content-Type: application/json

{
  "receiverId": 2,
  "content": "您好，我的订单什么时候能完成？",
  "messageType": 1,
  "relatedOrderId": 26
}
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "messageId": 1,
    "conversationId": "conv_1234567890",
    "senderId": 1,
    "senderName": "管理员",
    "senderAvatar": "",
    "receiverId": 2,
    "receiverName": "张三",
    "messageType": 1,
    "content": "您好，我的订单什么时候能完成？",
    "isRead": 0,
    "relatedOrderId": 26,
    "createTime": "2026-03-06T10:30:00",
    "fromMySelf": true
  }
}
```

### 2. 获取会话列表

**接口路径**：`GET /message/conversations`

**功能**：获取用户的会话列表

**请求参数**：
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| userId | Long | 是 | 当前用户ID |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认10 |

**请求示例**：
```
GET /message/conversations?userId=1&page=1&pageSize=10
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "conversationId": "conv_1234567890",
      "otherUserId": 2,
      "otherUserName": "张三",
      "otherUserAvatar": "",
      "otherUserType": 1,
      "lastMessageContent": "您好，我的订单什么时候能完成？",
      "lastMessageTime": "2026-03-06T10:30:00",
      "unreadCount": 0,
      "relatedOrderId": 26
    },
    {
      "conversationId": "conv_0987654321",
      "otherUserId": 3,
      "otherUserName": "李四",
      "otherUserAvatar": "",
      "otherUserType": 2,
      "lastMessageContent": "任务已完成，请确认",
      "lastMessageTime": "2026-03-06T09:15:00",
      "unreadCount": 1,
      "relatedOrderId": 25
    }
  ]
}
```

### 3. 获取会话消息列表

**接口路径**：`GET /message/messages/{conversationId}`

**功能**：获取指定会话的消息列表

**请求参数**：
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| conversationId | String | 是 | 会话ID |
| userId | Long | 是 | 当前用户ID |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |

**请求示例**：
```
GET /message/messages/conv_1234567890?userId=1&page=1&pageSize=20
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "messageId": 1,
      "conversationId": "conv_1234567890",
      "senderId": 2,
      "senderName": "张三",
      "senderAvatar": "",
      "receiverId": 1,
      "receiverName": "管理员",
      "messageType": 1,
      "content": "您好，我的订单什么时候能完成？",
      "isRead": 0,
      "relatedOrderId": 26,
      "createTime": "2026-03-06T10:25:00",
      "fromMySelf": false
    },
    {
      "messageId": 2,
      "conversationId": "conv_1234567890",
      "senderId": 1,
      "senderName": "管理员",
      "senderAvatar": "",
      "receiverId": 2,
      "receiverName": "张三",
      "messageType": 1,
      "content": "您好，我们正在处理您的订单，预计今天下午完成。",
      "isRead": 1,
      "relatedOrderId": 26,
      "createTime": "2026-03-06T10:28:00",
      "fromMySelf": true
    }
  ]
}
```

### 4. 标记消息为已读

**接口路径**：`PUT /message/messages/{conversationId}/read`

**功能**：标记指定会话的消息为已读

**请求参数**：
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| conversationId | String | 是 | 会话ID |
| userId | Long | 是 | 当前用户ID |

**请求示例**：
```
PUT /message/messages/conv_1234567890/read?userId=1
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

### 5. 获取未读消息数量

**接口路径**：`GET /message/unread/count`

**功能**：获取用户的未读消息数量

**请求参数**：
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| userId | Long | 是 | 当前用户ID |

**请求示例**：
```
GET /message/unread/count?userId=1
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 2
}
```

### 6. 获取与特定用户的会话

**接口路径**：`GET /message/conversation`

**功能**：获取与特定用户的会话信息

**请求参数**：
| 参数名 | 类型 | 必填 | 描述 |
|-------|------|------|------|
| userId | Long | 是 | 当前用户ID |
| otherUserId | Long | 是 | 对方用户ID |

**请求示例**：
```
GET /message/conversation?userId=1&otherUserId=2
```

**响应示例**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "conversationId": "conv_1234567890",
    "otherUserId": 2,
    "otherUserName": "张三",
    "otherUserAvatar": "",
    "otherUserType": 1,
    "lastMessageContent": "您好，我的订单什么时候能完成？",
    "lastMessageTime": "2026-03-06T10:30:00",
    "unreadCount": 0,
    "relatedOrderId": 26
  }
}
```

## 微信小程序实现建议

### 1. 目录结构

```
pages/
  chat/
    index.vue        # 聊天列表页面
    detail.vue       # 聊天详情页面
components/
  chat-message.vue  # 消息组件
  chat-input.vue    # 输入组件
utils/
  api.js            # API请求封装
  websocket.js      # WebSocket连接管理（可选）
```

### 2. API请求封装

```javascript
// utils/api.js
const baseURL = 'https://your-api-domain.com';

export function sendMessage(userId, messageDTO) {
  return wx.request({
    url: `${baseURL}/message/send`,
    method: 'POST',
    data: messageDTO,
    header: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${wx.getStorageSync('token')}`
    },
    dataType: 'json',
  });
}

export function getConversations(userId, page = 1, pageSize = 10) {
  return wx.request({
    url: `${baseURL}/message/conversations`,
    method: 'GET',
    data: {
      userId,
      page,
      pageSize
    },
    header: {
      'Authorization': `Bearer ${wx.getStorageSync('token')}`
    },
    dataType: 'json',
  });
}

// 其他API方法...
```

### 3. 聊天列表页面

```vue
<template>
  <view class="chat-list">
    <view class="search-bar">
      <input type="text" placeholder="搜索聊天" v-model="searchKeyword" />
    </view>
    <view class="list">
      <view 
        v-for="chat in chatList" 
        :key="chat.conversationId"
        class="chat-item"
        @tap="navigateToDetail(chat)"
      >
        <view class="avatar">{{ chat.otherUserName.charAt(0) }}</view>
        <view class="info">
          <view class="name">{{ chat.otherUserName }}</view>
          <view class="last-message">{{ chat.lastMessageContent }}</view>
        </view>
        <view class="right">
          <view class="time">{{ formatTime(chat.lastMessageTime) }}</view>
          <view v-if="chat.unreadCount > 0" class="unread">{{ chat.unreadCount }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getConversations } from '../../utils/api';

export default {
  data() {
    return {
      chatList: [],
      searchKeyword: '',
      userId: wx.getStorageSync('userId')
    };
  },
  onLoad() {
    this.loadConversations();
  },
  methods: {
    loadConversations() {
      getConversations(this.userId).then(res => {
        if (res.data.code === 200) {
          this.chatList = res.data.data;
        }
      });
    },
    navigateToDetail(chat) {
      wx.navigateTo({
        url: `/pages/chat/detail?conversationId=${chat.conversationId}&otherUserId=${chat.otherUserId}&otherUserName=${chat.otherUserName}`
      });
    },
    formatTime(time) {
      // 时间格式化逻辑
    }
  }
};
</script>
```

### 4. 聊天详情页面

```vue
<template>
  <view class="chat-detail">
    <view class="header">
      <view class="back" @tap="goBack">返回</view>
      <view class="title">{{ otherUserName }}</view>
    </view>
    <view class="messages">
      <view 
        v-for="msg in messages" 
        :key="msg.messageId"
        :class="['message', msg.fromMySelf ? 'sent' : 'received']"
      >
        <view class="content">{{ msg.content }}</view>
        <view class="time">{{ formatTime(msg.createTime) }}</view>
      </view>
    </view>
    <view class="input-area">
      <input type="text" placeholder="输入消息..." v-model="inputValue" />
      <button @tap="sendMessage">发送</button>
    </view>
  </view>
</template>

<script>
import { getMessages, sendMessage as apiSendMessage } from '../../utils/api';

export default {
  data() {
    return {
      conversationId: '',
      otherUserId: '',
      otherUserName: '',
      messages: [],
      inputValue: '',
      userId: wx.getStorageSync('userId')
    };
  },
  onLoad(options) {
    this.conversationId = options.conversationId;
    this.otherUserId = options.otherUserId;
    this.otherUserName = options.otherUserName;
    this.loadMessages();
  },
  methods: {
    loadMessages() {
      getMessages(this.conversationId, this.userId).then(res => {
        if (res.data.code === 200) {
          this.messages = res.data.data;
        }
      });
    },
    sendMessage() {
      if (!this.inputValue.trim()) return;
      
      apiSendMessage(this.userId, {
        receiverId: this.otherUserId,
        content: this.inputValue
      }).then(res => {
        if (res.data.code === 200) {
          this.messages.push(res.data.data);
          this.inputValue = '';
        }
      });
    },
    goBack() {
      wx.navigateBack();
    },
    formatTime(time) {
      // 时间格式化逻辑
    }
  }
};
</script>
```

## 注意事项

1. **认证**：所有接口都需要在请求头中携带JWT token进行认证
2. **用户类型**：otherUserType字段表示对方用户的类型，1-农户，2-飞手，3-机主，4-管理员
3. **消息类型**：messageType字段表示消息类型，默认1为普通消息
4. **关联订单**：relatedOrderId字段用于关联订单，可以在聊天中显示订单相关信息
5. **实时消息**：建议使用WebSocket实现实时消息推送，提升用户体验
6. **消息存储**：小程序端建议使用本地存储缓存消息历史，减少网络请求

## 错误处理

| 错误码 | 描述 | 处理建议 |
|-------|------|----------|
| 401 | 未授权 | 跳转到登录页面 |
| 403 | 权限不足 | 提示用户无权限操作 |
| 500 | 服务器错误 | 提示用户稍后重试 |

## 测试建议

1. 首先测试发送消息功能，确保消息能够正确存储
2. 测试获取会话列表，确保能够看到所有会话
3. 测试获取消息详情，确保能够看到完整的聊天记录
4. 测试标记已读功能，确保未读消息数量能够正确更新
5. 测试不同用户类型之间的聊天，确保能够正确显示对方信息

## 总结

本API文档提供了完整的消息系统接口，支持农户、飞手、机主和管理员之间的聊天功能。前端开发人员可以根据文档实现微信小程序的聊天功能，为用户提供便捷的沟通渠道。