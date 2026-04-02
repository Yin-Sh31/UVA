package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.entity.FlyerComplaint;

import java.util.List;

/**
 * 飞手投诉服务接口
 */
public interface FlyerComplaintService extends IService<FlyerComplaint> {

    /**
     * 添加投诉
     */
    boolean addComplaint(FlyerComplaint complaint);

    /**
     * 根据被投诉人ID获取投诉列表
     */
    List<FlyerComplaint> getByTargetId(Long targetId);

    /**
     * 处理投诉
     */
    boolean handleComplaint(Long id, String result);

    /**
     * 检查是否已投诉
     */
    boolean hasComplained(Long orderId, Long reporterId);

    /**
     * 获取待处理的投诉数量
     */
    Integer countPendingComplaints();
}
