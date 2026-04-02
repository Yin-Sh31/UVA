package com.cty.nopersonfinally.service;

import com.cty.nopersonfinally.pojo.dto.MessageDTO;
import com.cty.nopersonfinally.pojo.vo.ConversationVO;
import com.cty.nopersonfinally.pojo.vo.MessageVO;
import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {
    
    /**
     * 发送消息
     * @param senderId 发送者ID
     * @param messageDTO 消息数据
     * @return 发送的消息对象
     */
    MessageVO sendMessage(Long senderId, MessageDTO messageDTO);
    
    /**
     * 获取用户的会话列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 会话列表
     */
    List<ConversationVO> getConversations(Long userId, Integer page, Integer pageSize);
    
    /**
     * 获取会话中的消息列表
     * @param conversationId 会话ID
     * @param userId 当前用户ID（用于验证权限）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 消息列表
     */
    List<MessageVO> getMessages(String conversationId, Long userId, Integer page, Integer pageSize);
    
    /**
     * 标记会话中的消息为已读
     * @param conversationId 会话ID
     * @param userId 当前用户ID
     * @return 影响的行数
     */
    Integer markMessagesAsRead(String conversationId, Long userId);
    
    /**
     * 获取未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Integer getUnreadMessageCount(Long userId);
    
    /**
     * 创建或获取会话ID
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话ID
     */
    String createOrGetConversationId(Long userId1, Long userId2);
    
    /**
     * 获取两个用户之间的会话
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话信息
     */
    ConversationVO getConversationBetweenUsers(Long userId1, Long userId2);
    
    /**
     * 获取所有会话列表（管理员）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 会话列表
     */
    List<ConversationVO> getAllConversations(Integer page, Integer pageSize);
}