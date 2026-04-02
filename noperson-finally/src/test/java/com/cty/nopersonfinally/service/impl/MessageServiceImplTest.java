package com.cty.nopersonfinally.service.impl;

import com.cty.nopersonfinally.pojo.dto.MessageDTO;
import com.cty.nopersonfinally.pojo.vo.ConversationVO;
import com.cty.nopersonfinally.pojo.vo.MessageVO;
import com.cty.nopersonfinally.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MessageService的测试用例
 */
@SpringBootTest
@Transactional
@Rollback
class MessageServiceImplTest {

    @Autowired
    private MessageService messageService;

    // 测试用户ID
    private final Long senderId = 1L; // 假设ID为1的用户
    private final Long receiverId = 2L; // 假设ID为2的用户

    // 测试消息数据
    private MessageDTO messageDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        messageDTO = new MessageDTO();
        messageDTO.setReceiverId(receiverId);
        messageDTO.setMessageType(1); // 文本消息
        messageDTO.setContent("测试消息内容");
        messageDTO.setRelatedOrderId(100L);
    }

    /**
     * 测试发送消息功能
     */
    @Test
    void testSendMessage() {
        // 执行发送消息
        MessageVO messageVO = messageService.sendMessage(senderId, messageDTO);

        // 验证结果
        assertNotNull(messageVO, "消息发送失败，返回结果为null");
        assertEquals(senderId, messageVO.getSenderId(), "发送者ID不匹配");
        assertEquals(receiverId, messageVO.getReceiverId(), "接收者ID不匹配");
        assertEquals("测试消息内容", messageVO.getContent(), "消息内容不匹配");
        assertTrue(messageVO.getFromMySelf(), "消息应标记为自己发送的");
        System.out.println("发送消息测试成功: " + messageVO.getMessageId());
    }

    /**
     * 测试获取会话列表功能
     */
    @Test
    void testGetConversations() {
        // 先发送一条消息，确保有会话数据
        messageService.sendMessage(senderId, messageDTO);

        // 获取会话列表
        List<ConversationVO> conversations = messageService.getConversations(senderId, 1, 10);

        // 验证结果
        assertNotNull(conversations, "会话列表为空");
        assertFalse(conversations.isEmpty(), "会话列表不应为空");
        // 查找与receiverId相关的会话
        boolean found = false;
        for (ConversationVO conversation : conversations) {
            if (conversation.getOtherUserId().equals(receiverId)) {
                found = true;
                assertEquals("测试消息内容", conversation.getLastMessageContent(), "最后一条消息内容不匹配");
                break;
            }
        }
        assertTrue(found, "未找到与指定用户的会话");
        System.out.println("获取会话列表测试成功: 找到 " + conversations.size() + " 个会话");
    }

    /**
     * 测试获取会话消息列表功能
     */
    @Test
    void testGetMessages() {
        // 先发送一条消息
        MessageVO sentMessage = messageService.sendMessage(senderId, messageDTO);
        String conversationId = sentMessage.getConversationId();

        // 获取消息列表
        List<MessageVO> messages = messageService.getMessages(conversationId, senderId, 1, 20);

        // 验证结果
        assertNotNull(messages, "消息列表为空");
        assertFalse(messages.isEmpty(), "消息列表不应为空");
        assertEquals(1, messages.size(), "消息数量不匹配");
        assertEquals("测试消息内容", messages.get(0).getContent(), "消息内容不匹配");
        System.out.println("获取会话消息列表测试成功: 找到 " + messages.size() + " 条消息");
    }

    /**
     * 测试标记消息为已读功能
     */
    @Test
    void testMarkMessagesAsRead() {
        // 先发送一条消息
        MessageVO sentMessage = messageService.sendMessage(senderId, messageDTO);
        String conversationId = sentMessage.getConversationId();

        // 切换视角，模拟接收者查看消息并标记已读
        Integer result = messageService.markMessagesAsRead(conversationId, receiverId);

        // 验证结果
        assertEquals(1, result, "标记已读操作失败");
        System.out.println("标记消息为已读测试成功");
    }

    /**
     * 测试获取未读消息数量功能
     */
    @Test
    void testGetUnreadMessageCount() {
        // 发送一条消息给receiverId
        messageService.sendMessage(senderId, messageDTO);

        // 获取接收者的未读消息数量
        Integer unreadCount = messageService.getUnreadMessageCount(receiverId);

        // 验证结果
        assertNotNull(unreadCount, "未读消息数量为null");
        assertTrue(unreadCount >= 1, "未读消息数量不正确");
        System.out.println("获取未读消息数量测试成功: 未读消息数 = " + unreadCount);
    }

    /**
     * 测试获取两个用户之间的会话功能
     */
    @Test
    void testGetConversationBetweenUsers() {
        // 获取两个用户之间的会话
        ConversationVO conversation = messageService.getConversationBetweenUsers(senderId, receiverId);

        // 验证结果
        assertNotNull(conversation, "会话信息为空");
        assertNotNull(conversation.getConversationId(), "会话ID为空");
        assertEquals(receiverId, conversation.getOtherUserId(), "对方用户ID不匹配");
        System.out.println("获取两个用户之间的会话测试成功: conversationId = " + conversation.getConversationId());
    }

    /**
     * 测试完整的消息发送和接收流程
     */
    @Test
    void testCompleteMessageFlow() {
        // 1. 发送第一条消息
        MessageVO message1 = messageService.sendMessage(senderId, messageDTO);
        String conversationId = message1.getConversationId();
        assertNotNull(message1, "第一条消息发送失败");

        // 2. 发送第二条回复消息（接收者回复发送者）
        MessageDTO replyDTO = new MessageDTO();
        replyDTO.setReceiverId(senderId);
        replyDTO.setMessageType(1);
        replyDTO.setContent("回复消息内容");
        MessageVO message2 = messageService.sendMessage(receiverId, replyDTO);
        assertNotNull(message2, "回复消息发送失败");
        assertEquals(conversationId, message2.getConversationId(), "会话ID不一致");

        // 3. 获取会话消息列表
        List<MessageVO> messages = messageService.getMessages(conversationId, senderId, 1, 20);
        assertNotNull(messages, "消息列表为空");
        assertTrue(messages.size() >= 2, "消息数量不足");

        // 4. 标记消息为已读
        messageService.markMessagesAsRead(conversationId, senderId);

        // 5. 验证未读消息数量
        Integer unreadCount = messageService.getUnreadMessageCount(senderId);
        System.out.println("完整消息流程测试成功: 总消息数 = " + messages.size() + ", 未读消息数 = " + unreadCount);
    }
}