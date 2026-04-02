package com.cty.nopersonfinally.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cty.nopersonfinally.pojo.entity.FlyerComplaint;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 飞手投诉Mapper接口
 */
@Mapper
public interface FlyerComplaintMapper extends BaseMapper<FlyerComplaint> {

    /**
     * 根据被投诉人ID获取投诉列表
     */
    List<FlyerComplaint> getByTargetId(Long targetId);

    /**
     * 根据订单ID和投诉人ID查询是否已投诉
     */
    FlyerComplaint getByOrderAndReporter(Long orderId, Long reporterId);

    /**
     * 获取待处理的投诉数量
     */
    Integer countPendingComplaints();
}
