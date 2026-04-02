package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.dto.MessageDTO;
import com.cty.nopersonfinally.pojo.vo.ConversationVO;
import com.cty.nopersonfinally.pojo.vo.MessageVO;
import com.cty.nopersonfinally.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 消息控制器
 * 处理农户和飞手之间的对话功能
 */
@RestController
@RequestMapping({"/message", "/api/message"})
@Api(tags = "消息管理")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     * @param messageDTO 消息数据
     * @param request HTTP请求
     * @return 发送结果
     */
    @PostMapping("/send")
    @ApiOperation("发送消息")
    public Result<?> sendMessage(
            @RequestBody @ApiParam("消息数据") MessageDTO messageDTO,
            @RequestParam @ApiParam("用户ID") Long userId) {
        try {
            // 发送消息
            MessageVO messageVO = messageService.sendMessage(userId, messageDTO);
            return Result.ok(messageVO);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param request HTTP请求
     * @return 会话列表
     */
    @GetMapping("/conversations")
    @ApiOperation("获取会话列表")
    public Result<?> getConversations(
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer page,
            @RequestParam(defaultValue = "10") @ApiParam("每页大小") Integer pageSize,
            @RequestParam @ApiParam("用户ID") Long userId) {
        try {
            // 获取会话列表
            List<ConversationVO> conversations = messageService.getConversations(userId, page, pageSize);
            return Result.ok(conversations);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表
     * @param conversationId 会话ID
     * @param page 页码
     * @param pageSize 每页大小
     * @param request HTTP请求
     * @return 消息列表
     */
    @GetMapping("/messages/{conversationId}")
    @ApiOperation("获取会话消息列表")
    public Result<?> getMessages(
            @PathVariable @ApiParam("会话ID") String conversationId,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer page,
            @RequestParam(defaultValue = "20") @ApiParam("每页大小") Integer pageSize,
            @RequestParam @ApiParam("用户ID") Long userId) {
        try {
            // 获取消息列表
            List<MessageVO> messages = messageService.getMessages(conversationId, userId, page, pageSize);
            
            // 标记消息为已读
            messageService.markMessagesAsRead(conversationId, userId);
            
            return Result.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     * @param conversationId 会话ID
     * @param request HTTP请求
     * @return 操作结果
     */
    @PutMapping("/messages/{conversationId}/read")
    @ApiOperation("标记消息为已读")
    public Result<?> markAsRead(
            @PathVariable @ApiParam("会话ID") String conversationId,
            @RequestParam @ApiParam("用户ID") Long userId) {
        try {
            // 标记已读
            Integer count = messageService.markMessagesAsRead(conversationId, userId);
            return Result.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     * @param request HTTP请求
     * @return 未读消息数量
     */
    @GetMapping("/unread/count")
    @ApiOperation("获取未读消息数量")
    public Result<?> getUnreadCount(@RequestParam @ApiParam("用户ID") Long userId) {
        try {
            // 获取未读消息数量
            Integer count = messageService.getUnreadMessageCount(userId);
            return Result.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取未读消息数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取与特定用户的会话
     * @param otherUserId 对方用户ID
     * @param request HTTP请求
     * @return 会话信息
     */
    @GetMapping("/conversation")
    @ApiOperation("获取与特定用户的会话")
    public Result<?> getConversationWithUser(
            @RequestParam @ApiParam("当前用户ID") Long userId,
            @RequestParam @ApiParam("对方用户ID") Long otherUserId) {
        try {
            // 获取会话
            ConversationVO conversation = messageService.getConversationBetweenUsers(userId, otherUserId);
            return Result.ok(conversation);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查并创建与管理员的会话
     */
    @PostMapping("/check-admin-conversation")
    @ApiOperation("检查并创建与管理员的会话")
    public Result<?> checkAndCreateAdminConversation(
            @RequestParam @ApiParam("用户ID") Long userId) {
        try {
            // 管理员ID固定为1
            Long adminId = 1L;
            
            // 检查是否存在会话
            ConversationVO conversation = messageService.getConversationBetweenUsers(userId, adminId);
            
            if (conversation != null) {
                return Result.ok(conversation);
            }
            
            // 直接创建会话，不发送消息
            String conversationId = messageService.createOrGetConversationId(userId, adminId);
            
            // 构建返回的会话对象
            conversation = new ConversationVO();
            conversation.setConversationId(conversationId);
            conversation.setOtherUserId(adminId);
            conversation.setOtherUserName("管理员");
            conversation.setOtherUserAvatar(""); // 管理员默认头像为空，前端显示默认头像
            conversation.setOtherUserType(4); // 管理员类型
            conversation.setLastMessageContent("您可以在这里与管理员联系");
            conversation.setUnreadCount(0);
            
            return Result.ok(conversation);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "检查并创建管理员会话失败: " + e.getMessage());
        }
    }
}