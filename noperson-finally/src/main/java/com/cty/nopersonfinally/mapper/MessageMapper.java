package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.Message;
import com.cty.nopersonfinally.pojo.vo.ConversationVO;
import com.cty.nopersonfinally.pojo.vo.MessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    /**
     * 获取用户的会话列表
     * @param userId 用户ID
     * @param limit 分页限制
     * @param offset 分页偏移量
     * @return 会话列表
     */
    List<ConversationVO> getConversationsByUserId(@Param("userId") Long userId, 
                                                  @Param("limit") Integer limit, 
                                                  @Param("offset") Integer offset);
    
    /**
     * 获取会话中的消息列表
     * @param conversationId 会话ID
     * @param limit 分页限制
     * @param offset 分页偏移量
     * @return 消息列表
     */
    List<MessageVO> getMessagesByConversationId(@Param("conversationId") String conversationId, 
                                               @Param("limit") Integer limit, 
                                               @Param("offset") Integer offset);
    
    /**
     * 标记会话中的消息为已读
     * @param conversationId 会话ID
     * @param userId 用户ID（接收者）
     */
    void markMessagesAsRead(@Param("conversationId") String conversationId, @Param("userId") Long userId);
    
    /**
     * 获取未读消息数量
     * @param userId 用户ID（接收者）
     * @return 未读消息数量
     */
    Integer getUnreadMessageCount(@Param("userId") Long userId);
    
    /**
     * 生成或获取会话ID
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 会话ID
     */
    String getConversationId(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 获取特定用户之间的最新消息
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 最新消息
     */
    MessageVO getLatestMessageBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 获取所有会话列表（管理员）
     * @param limit 分页限制
     * @param offset 分页偏移量
     * @return 会话列表
     */
    List<ConversationVO> getAllConversations(@Param("limit") Integer limit, @Param("offset") Integer offset);
}