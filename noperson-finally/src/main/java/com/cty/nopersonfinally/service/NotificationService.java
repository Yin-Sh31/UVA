package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cty.nopersonfinally.pojo.dto.NotificationDTO;
import com.cty.nopersonfinally.pojo.entity.Notification;

import java.util.List;

public interface NotificationService {
    /**
     * 发送通知
     * @param userId 接收用户ID
     * @param title 标题
     * @param content 内容
     * @param type 类型（MINIPROGRAM/SYSTEM/INSPECTION_ORDER等）
     */
    void sendNotification(Long userId, String title, String content, String type);

    /**
     * 发送订单匹配通知给多个飞手
     * @param flyerIds 飞手ID列表
     * @param orderId 订单ID
     */
    void sendOrderMatchNotification(List<Long> flyerIds, Long orderId);

    /**
     * 获取用户未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     * @param noticeId 通知ID
     * @param userId 用户ID（用于权限校验）
     * @return 是否标记成功
     */
    boolean markAsRead(Long noticeId, Long userId);

    /**
     * 批量标记通知为已读
     * @param noticeIds 通知ID列表
     * @param userId 用户ID（用于权限校验）
     * @return 成功标记数量
     */
    int batchMarkAsRead(List<Long> noticeIds, Long userId);

    /**
     * 分页查询用户通知
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param isRead 阅读状态（null-全部，0-未读，1-已读）
     * @return 分页通知列表
     */
    IPage<Notification> getUserNotifications(Long userId, int pageNum, int pageSize, Integer isRead);

    /**
     * 分页查询用户通知DTO（用于前端展示）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param isRead 阅读状态（null-全部，0-未读，1-已读）
     * @return 分页通知DTO列表
     */
    IPage<NotificationDTO> getUserNotificationDTOs(Long userId, int pageNum, int pageSize, Integer isRead);
}