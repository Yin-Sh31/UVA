package com.cty.nopersonfinally.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.mapper.NotificationMapper;
import com.cty.nopersonfinally.pojo.dto.NotificationDTO;
import com.cty.nopersonfinally.pojo.entity.Notification;
import com.cty.nopersonfinally.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void sendNotification(Long userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        // 确保通知类型符合数据库要求：1-系统通知 2-订单通知 3-审核通知 4-评价通知
        String notifyType = "1"; // 默认系统通知
        if (type != null) {
            if (type.contains("ORDER")) {
                notifyType = "2";
            } else if (type.contains("AUDIT")) {
                notifyType = "3";
            } else if (type.contains("EVAL")) {
                notifyType = "4";
            }
        }
        notification.setType(notifyType);
        notification.setIsRead(0); // 初始未读
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional
    public void sendOrderMatchNotification(List<Long> flyerIds, Long orderId) {
        if (flyerIds == null || flyerIds.isEmpty()) {
            return;
        }
        // 构建订单匹配通知内容
        String title = "新订单匹配";
        String content = String.format("您有新的订单待接取，订单ID：%d，请及时处理", orderId);

        // 批量发送通知
        for (Long flyerId : flyerIds) {
            sendNotification(flyerId, title, content, "ORDER_MATCH");
        }
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(queryWrapper).intValue();
    }

    @Override
    public boolean markAsRead(Long noticeId, Long userId) {
        // 校验通知是否属于该用户
        Notification notification = notificationMapper.selectById(noticeId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }
        // 已读状态无需重复更新
        if (notification.getIsRead() == 1) {
            return true;
        }
        // 更新为已读
        notification.setIsRead(1);
        return notificationMapper.updateById(notification) > 0;
    }

    @Override
    @Transactional
    public int batchMarkAsRead(List<Long> noticeIds, Long userId) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return 0;
        }
        // 构建更新条件：通知ID在列表中 + 属于当前用户 + 未读
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Notification::getNoticeId, noticeIds)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);

        // 批量更新为已读
        Notification updateEntity = new Notification();
        updateEntity.setIsRead(1);
        return notificationMapper.update(updateEntity, queryWrapper);
    }

    @Override
    public IPage<Notification> getUserNotifications(Long userId, int pageNum, int pageSize, Integer isRead) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                // 按创建时间倒序，最新的在前
                .orderByDesc(Notification::getCreateTime);

        // 若指定阅读状态，添加条件
        if (isRead != null) {
            queryWrapper.eq(Notification::getIsRead, isRead);
        }

        return notificationMapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<NotificationDTO> getUserNotificationDTOs(Long userId, int pageNum, int pageSize, Integer isRead) {
        // 获取原始通知列表
        IPage<Notification> notificationPage = getUserNotifications(userId, pageNum, pageSize, isRead);
        
        // 创建DTO分页对象
        Page<NotificationDTO> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(notificationPage.getTotal());
        dtoPage.setPages(notificationPage.getPages());
        
        // 转换实体列表为DTO列表
        List<NotificationDTO> dtoList = notificationPage.getRecords().stream().map(notification -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setNoticeId(notification.getNoticeId());
            dto.setTitle(notification.getTitle());
            dto.setContent(notification.getContent());
            // 使用setTypeWithCode方法转换通知类型
            dto.setTypeWithCode(notification.getType());
            dto.setIsRead(notification.getIsRead());
            // 设置格式化时间
            dto.setCreateTime(notification.getCreateTime().toString());
            
            // 尝试从内容中提取关联ID（如果需要更精确的提取，可以优化此逻辑）
            // 这里仅做示例，实际应用中可能需要更复杂的解析或额外存储关联ID
            dto.setRelatedId(null);
            dto.setRelatedType(null);
            
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}