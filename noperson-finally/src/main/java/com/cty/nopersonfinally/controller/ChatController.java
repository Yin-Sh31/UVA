package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.mapper.MessageMapper;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.Conversation;
import com.cty.nopersonfinally.pojo.entity.Message;
import com.cty.nopersonfinally.pojo.vo.MessageVO;
import com.cty.nopersonfinally.service.ConversationService;
import com.cty.nopersonfinally.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/chat", "/api/chat"})
@Tag(name = "聊天接口", description = "聊天功能相关接口")
public class ChatController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private com.cty.nopersonfinally.mapper.ConversationMapper conversationMapper;

    @Autowired
    private com.cty.nopersonfinally.utils.OssFileUtil ossFileUtil;

    @Autowired
    private com.cty.nopersonfinally.utils.JWTUtil jwtUtil;

    @PostMapping("/conversation/create")
    @Operation(summary = "创建会话", description = "创建或获取已有会话")
    public Result<?> createConversation(
            @RequestBody @Parameter(description = "目标用户ID") Map<String, String> request,
            @RequestHeader("Authorization") String token) {
        try {
            String targetUserId = request.get("targetUserId");
            if (targetUserId == null || targetUserId.isEmpty()) {
                return Result.error("目标用户ID不能为空");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("未授权访问");
            }

            Conversation conversation = conversationService.createConversation(String.valueOf(userId), targetUserId);

            Map<String, Object> result = new HashMap<>();
            result.put("conversationId", String.valueOf(conversation.getId()));
            result.put("createTime", conversation.getCreateTime());

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "创建会话失败: " + e.getMessage());
        }
    }

    @GetMapping("/messages")
    @Operation(summary = "获取聊天记录", description = "分页获取聊天记录")
    public Result<?> getMessages(
            @RequestParam @Parameter(description = "会话ID") String conversationId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer pageNum,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页数量") Integer pageSize) {
        try {
            Page<Message> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getConversationId, conversationId);
            wrapper.orderByDesc(Message::getCreateTime);

            Page<Message> messagePage = messageMapper.selectPage(page, wrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("records", messagePage.getRecords());
            result.put("total", messagePage.getTotal());
            result.put("pages", messagePage.getPages());
            result.put("current", messagePage.getCurrent());

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取聊天记录失败: " + e.getMessage());
        }
    }

    @PostMapping("/message/send")
    @Operation(summary = "发送消息", description = "发送消息")
    public Result<?> sendMessage(
            @RequestBody @Parameter(description = "消息数据") Map<String, Object> request,
            @RequestHeader("Authorization") String token) {
        try {
            String conversationId = (String) request.get("conversationId");
            String content = (String) request.get("content");
            String messageType = (String) request.get("messageType");
            String receiverIdStr = (String) request.get("receiver_id");

            if (conversationId == null || conversationId.isEmpty()) {
                return Result.error("会话ID不能为空");
            }
            if (content == null || content.isEmpty()) {
                return Result.error("消息内容不能为空");
            }
            if (receiverIdStr == null || receiverIdStr.isEmpty()) {
                return Result.error("接收者ID不能为空");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return Result.error("未授权访问");
            }

            Message message = new Message();
            message.setConversationId(conversationId);
            message.setContent(content);
            message.setMessageType("text".equals(messageType) ? 1 : 2);
            message.setSenderId(userId);
            message.setReceiverId(Long.parseLong(receiverIdStr));
            message.setIsRead(0);

            messageMapper.insert(message);

            Map<String, Object> result = new HashMap<>();
            result.put("id", String.valueOf(message.getMessageId()));
            result.put("conversationId", message.getConversationId());
            result.put("senderId", String.valueOf(message.getSenderId()));
            result.put("content", message.getContent());
            result.put("messageType", "text".equals(messageType) ? "text" : "image");
            result.put("status", 1);
            result.put("createTime", message.getCreateTime());

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "发送消息失败: " + e.getMessage());
        }
    }

    @PostMapping("/image/upload")
    @Operation(summary = "上传聊天图片", description = "上传聊天图片")
    public Result<?> uploadImage(
            @RequestParam("file") @Parameter(description = "图片文件") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            String url = ossFileUtil.uploadImage(file, "chat");

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);

            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "上传图片失败: " + e.getMessage());
        }
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息数")
    public Result<?> getUnreadCount(@RequestParam @Parameter(description = "用户ID") Long userId) {
        try {
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getReceiverId, userId);
            wrapper.eq(Message::getIsRead, 0);
            
            Long count = messageMapper.selectCount(wrapper);
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取未读消息数失败: " + e.getMessage());
        }
    }

    @GetMapping("/conversation/list")
    @Operation(summary = "获取会话列表", description = "获取用户的会话列表")
    public Result<?> getConversationList(
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer pageNum,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页数量") Integer pageSize) {
        try {
            Page<Conversation> page = new Page<>(pageNum, pageSize);
            LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Conversation::getUser1Id, String.valueOf(userId))
                   .or()
                   .eq(Conversation::getUser2Id, String.valueOf(userId));
            wrapper.orderByDesc(Conversation::getUpdateTime);
            
            Page<Conversation> conversationPage = conversationMapper.selectPage(page, wrapper);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", conversationPage.getRecords());
            result.put("total", conversationPage.getTotal());
            result.put("pages", conversationPage.getPages());
            result.put("current", conversationPage.getCurrent());
            
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取会话列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/messages/read")
    @Operation(summary = "标记消息已读", description = "标记会话中的消息为已读")
    public Result<?> markMessagesAsRead(@RequestBody @Parameter(description = "会话ID") Map<String, String> request) {
        try {
            String conversationId = request.get("conversationId");
            if (conversationId == null || conversationId.isEmpty()) {
                return Result.error("会话ID不能为空");
            }
            
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getConversationId, conversationId);
            wrapper.eq(Message::getIsRead, 0);
            
            Message updateMsg = new Message();
            updateMsg.setIsRead(1);
            
            messageMapper.update(updateMsg, wrapper);
            
            return Result.ok("标记成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "标记消息已读失败: " + e.getMessage());
        }
    }

    @PostMapping("/conversation/delete")
    @Operation(summary = "删除会话", description = "删除指定会话")
    public Result<?> deleteConversation(@RequestBody @Parameter(description = "会话ID") Map<String, String> request) {
        try {
            String conversationId = request.get("conversationId");
            if (conversationId == null || conversationId.isEmpty()) {
                return Result.error("会话ID不能为空");
            }
            
            conversationMapper.deleteById(Long.parseLong(conversationId));
            
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getConversationId, conversationId);
            messageMapper.delete(wrapper);
            
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "删除会话失败: " + e.getMessage());
        }
    }
}