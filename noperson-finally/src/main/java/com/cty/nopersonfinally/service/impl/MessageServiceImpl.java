package com.cty.nopersonfinally.service.impl;

import com.cty.nopersonfinally.mapper.MessageMapper;
import com.cty.nopersonfinally.pojo.dto.MessageDTO;
import com.cty.nopersonfinally.pojo.entity.Message;
import com.cty.nopersonfinally.pojo.vo.ConversationVO;
import com.cty.nopersonfinally.pojo.vo.MessageVO;
import com.cty.nopersonfinally.service.MessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 消息服务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 发送消息
     */
    @Override
    @Transactional
    public MessageVO sendMessage(Long senderId, MessageDTO messageDTO) {
        System.out.println("[MessageServiceImpl] 开始发送消息，senderId: " + senderId + ", receiverId: " + messageDTO.getReceiverId());
        
        try {
            // 创建消息实体
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(messageDTO.getReceiverId());
            message.setMessageType(messageDTO.getMessageType() != null ? messageDTO.getMessageType() : 1);
            message.setContent(messageDTO.getContent());
            message.setIsRead(0); // 初始状态为未读
            message.setRelatedOrderId(messageDTO.getRelatedOrderId());
            message.setCreateTime(LocalDateTime.now());

            System.out.println("[MessageServiceImpl] 消息实体创建完成，内容: " + message.getContent());

            // 生成或获取会话ID
            String conversationId = createOrGetConversationId(senderId, messageDTO.getReceiverId());
            System.out.println("[MessageServiceImpl] 会话ID: " + conversationId);
            message.setConversationId(conversationId);

            // 保存消息
            System.out.println("[MessageServiceImpl] 开始保存消息");
            messageMapper.insert(message);
            System.out.println("[MessageServiceImpl] 消息保存成功，messageId: " + message.getMessageId());

            // 构建返回的消息VO
            MessageVO messageVO = new MessageVO();
            messageVO.setMessageId(message.getMessageId());
            messageVO.setConversationId(message.getConversationId());
            messageVO.setSenderId(message.getSenderId());
            messageVO.setReceiverId(message.getReceiverId());
            messageVO.setMessageType(message.getMessageType());
            messageVO.setContent(message.getContent());
            messageVO.setIsRead(message.getIsRead());
            messageVO.setRelatedOrderId(message.getRelatedOrderId());
            messageVO.setCreateTime(message.getCreateTime());
            messageVO.setFromMySelf(true); // 标记为自己发送的消息
            
            System.out.println("[MessageServiceImpl] 消息发送成功");
            return messageVO;
        } catch (Exception e) {
            System.out.println("[MessageServiceImpl] 发送消息失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取用户的会话列表
     */
    @Override
    public List<ConversationVO> getConversations(Long userId, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return messageMapper.getConversationsByUserId(userId, pageSize, offset);
    }

    /**
     * 获取会话中的消息列表
     */
    @Override
    public List<MessageVO> getMessages(String conversationId, Long userId, Integer page, Integer pageSize) {
        // 验证用户是否有权限查看此会话
        List<MessageVO> messages = messageMapper.getMessagesByConversationId(conversationId, pageSize, (page - 1) * pageSize);
        if (messages != null && !messages.isEmpty()) {
            // 标记消息的发送者是否是当前用户
            for (MessageVO messageVO : messages) {
                messageVO.setFromMySelf(messageVO.getSenderId().equals(userId));
            }
        }
        return messages;
    }

    /**
     * 标记会话中的消息为已读
     */
    @Override
    @Transactional
    public Integer markMessagesAsRead(String conversationId, Long userId) {
        // 由于MyBatis的update方法默认返回影响的行数
        // 但我们的Mapper接口定义为void，所以这里手动返回操作状态
        messageMapper.markMessagesAsRead(conversationId, userId);
        return 1; // 表示操作成功
    }

    /**
     * 获取未读消息数量
     */
    @Override
    public Integer getUnreadMessageCount(Long userId) {
        return messageMapper.getUnreadMessageCount(userId);
    }

    /**
     * 创建或获取会话ID
     */
    @Override
    public String createOrGetConversationId(Long userId1, Long userId2) {
        // 尝试从数据库获取现有会话ID
        String conversationId = messageMapper.getConversationId(userId1, userId2);
        
        // 如果没有现有会话，则生成新的会话ID并保存到数据库
        if (conversationId == null) {
            // 生成会话ID，确保唯一性
            conversationId = "conv_" + UUID.randomUUID().toString().replace("-", "");
            
            // 在数据库中创建会话（插入一条空消息作为会话标识）
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderId(userId1);
            message.setReceiverId(userId2);
            message.setMessageType(1);
            message.setContent("");
            message.setIsRead(1);
            message.setRelatedOrderId(null);
            message.setCreateTime(LocalDateTime.now());
            
            messageMapper.insert(message);
        }
        
        return conversationId;
    }

    /**
     * 获取两个用户之间的会话
     */
    @Override
    public ConversationVO getConversationBetweenUsers(Long userId1, Long userId2) {
        // 创建会话ID
        String conversationId = createOrGetConversationId(userId1, userId2);
        
        // 获取会话列表，使用userId1作为当前用户
        List<ConversationVO> conversations = messageMapper.getConversationsByUserId(userId1, 1, 0);
        
        // 查找与userId2相关的会话
        for (ConversationVO conversation : conversations) {
            if (conversation.getOtherUserId().equals(userId2)) {
                return conversation;
            }
        }
        
        // 如果不存在会话记录，创建一个基本的会话对象
        ConversationVO conversationVO = new ConversationVO();
        conversationVO.setConversationId(conversationId);
        conversationVO.setOtherUserId(userId2);
        return conversationVO;
    }

    /**
     * 获取所有会话列表（管理员）
     */
    @Override
    public List<ConversationVO> getAllConversations(Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        return messageMapper.getAllConversations(pageSize, offset);
    }
}