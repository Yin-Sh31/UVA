package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.MessageDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.vo.ConversationVO;
import com.cty.nopersonfinally.pojo.vo.MessageVO;
import com.cty.nopersonfinally.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.hamcrest.Matchers.containsString;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageController的测试用例
 */
@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    // 测试用户ID
    private final Long senderId = 1L;
    private final Long receiverId = 2L;

    // 测试数据
    private MessageDTO messageDTO;
    private MessageVO messageVO;
    private ConversationVO conversationVO;

    @BeforeEach
    void setUp() {
        // 初始化消息DTO
        messageDTO = new MessageDTO();
        messageDTO.setReceiverId(receiverId);
        messageDTO.setMessageType(1);
        messageDTO.setContent("测试消息内容");
        messageDTO.setRelatedOrderId(100L);

        // 初始化消息VO
        messageVO = new MessageVO();
        messageVO.setMessageId(1L);
        messageVO.setConversationId("conv_test123");
        messageVO.setSenderId(senderId);
        messageVO.setReceiverId(receiverId);
        messageVO.setContent("测试消息内容");
        messageVO.setFromMySelf(true);

        // 初始化会话VO
        conversationVO = new ConversationVO();
        conversationVO.setConversationId("conv_test123");
        conversationVO.setOtherUserId(receiverId);
        conversationVO.setOtherUserName("测试用户");
        conversationVO.setLastMessageContent("测试消息内容");
        conversationVO.setUnreadCount(1);
    }

    /**
     * 测试发送消息接口
     */
    @Test
    void testSendMessage() throws Exception {
        // 模拟Service层返回结果
        Mockito.when(messageService.sendMessage(senderId, messageDTO)).thenReturn(messageVO);

        // 执行请求
        mockMvc.perform(MockMvcRequestBuilders.post("/api/message/send")
                        .param("userId", String.valueOf(senderId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("测试消息内容"))
                .andDo(MockMvcResultHandlers.print());

        System.out.println("发送消息接口测试成功");
    }

    /**
     * 测试获取会话列表接口
     */
    @Test
    void testGetConversations() throws Exception {
        // 准备模拟数据
        List<ConversationVO> conversations = new ArrayList<>();
        conversations.add(conversationVO);

        // 模拟Service层返回结果
        Mockito.when(messageService.getConversations(senderId, 1, 10)).thenReturn(conversations);

        // 执行请求
        mockMvc.perform(MockMvcRequestBuilders.get("/api/message/conversations")
                        .param("userId", String.valueOf(senderId))
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1))
                .andDo(MockMvcResultHandlers.print());

        System.out.println("获取会话列表接口测试成功");
    }

    /**
     * 测试获取会话消息列表接口
     */
    @Test
    void testGetMessages() throws Exception {
        // 准备模拟数据
        List<MessageVO> messages = new ArrayList<>();
        messages.add(messageVO);

        // 模拟Service层返回结果
        Mockito.when(messageService.getMessages("conv_test123", senderId, 1, 20)).thenReturn(messages);
        Mockito.doNothing().when(messageService).markMessagesAsRead("conv_test123", senderId);

        // 执行请求
        mockMvc.perform(MockMvcRequestBuilders.get("/api/message/messages/conv_test123")
                        .param("userId", String.valueOf(senderId))
                        .param("page", "1")
                        .param("pageSize", "20"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1))
                .andDo(MockMvcResultHandlers.print());

        System.out.println("获取会话消息列表接口测试成功");
    }

    /**
     * 测试标记消息为已读接口
     */
    @Test
    void testMarkAsRead() throws Exception {
        // 模拟Service层返回结果
        Mockito.when(messageService.markMessagesAsRead("conv_test123", senderId)).thenReturn(1);

        // 执行请求
        mockMvc.perform(MockMvcRequestBuilders.put("/api/message/messages/conv_test123/read")
                        .param("userId", String.valueOf(senderId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(1))
                .andDo(MockMvcResultHandlers.print());

        System.out.println("标记消息为已读接口测试成功");
    }

    /**
     * 测试获取未读消息数量接口
     */
    @Test
    void testGetUnreadCount() throws Exception {
        // 模拟Service层返回结果
        Mockito.when(messageService.getUnreadMessageCount(senderId)).thenReturn(5);

        // 执行请求
        mockMvc.perform(MockMvcRequestBuilders.get("/api/message/unread/count")
                        .param("userId", String.valueOf(senderId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").value(5))
                .andDo(MockMvcResultHandlers.print());

        System.out.println("获取未读消息数量接口测试成功");
    }

    /**
     * 测试获取与特定用户的会话接口
     */
    @Test
    void testGetConversationWithUser() throws Exception {
        // 模拟Service层返回结果
        Mockito.when(messageService.getConversationBetweenUsers(senderId, receiverId)).thenReturn(conversationVO);

        // 执行请求
        mockMvc.perform(MockMvcRequestBuilders.get("/api/message/conversation")
                        .param("userId", String.valueOf(senderId))
                        .param("otherUserId", String.valueOf(receiverId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.conversationId").value("conv_test123"))
                .andDo(MockMvcResultHandlers.print());

        System.out.println("获取与特定用户的会话接口测试成功");
    }

    /**
     * 测试异常情况处理
     */
    @Test
    void testExceptionHandling() throws Exception {
        // 模拟Service层抛出异常
        Mockito.when(messageService.sendMessage(senderId, messageDTO)).thenThrow(new RuntimeException("测试异常"));

        // 执行请求并验证异常处理
        mockMvc.perform(MockMvcRequestBuilders.post("/api/message/send")
                        .param("userId", String.valueOf(senderId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(500))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(containsString("发送消息失败")))
                .andDo(MockMvcResultHandlers.print());

        System.out.println("异常处理测试成功");
    }
}