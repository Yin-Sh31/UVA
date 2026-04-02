package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cty.nopersonfinally.pojo.dto.NotificationDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/notification")
@Api(tags = "消息通知接口")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    @GetMapping("/unreadCount")
    @ApiOperation("获取未读消息数量")
    public Result<Integer> getUnreadCount(@RequestParam Long userId) {
        Integer count = notificationService.getUnreadCount(userId);
        return Result.ok(count);
    }

    /**
     * 标记通知为已读
     * @param noticeId 通知ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/markAsRead")
    @ApiOperation("标记消息为已读")
    public Result<Boolean> markAsRead(@RequestParam Long noticeId, @RequestParam Long userId) {
        boolean success = notificationService.markAsRead(noticeId, userId);
        return Result.ok(success);
    }

    /**
     * 批量标记通知为已读
     * @param noticeIds 通知ID列表
     * @param userId 用户ID
     * @return 成功标记数量
     */
    @PostMapping("/batchMarkAsRead")
    @ApiOperation("批量标记消息为已读")
    public Result<Integer> batchMarkAsRead(@RequestBody List<Long> noticeIds, @RequestParam Long userId) {
        int count = notificationService.batchMarkAsRead(noticeIds, userId);
        return Result.ok(count);
    }

    /**
     * 获取用户通知列表
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param isRead 阅读状态（null-全部，0-未读，1-已读）
     * @return 分页通知列表
     */
    @GetMapping("/list")
    @ApiOperation("获取消息列表")
    public Result<IPage<NotificationDTO>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer isRead) {
        IPage<NotificationDTO> notifications = notificationService.getUserNotificationDTOs(userId, pageNum, pageSize, isRead);
        return Result.ok(notifications);
    }
}