package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cty.nopersonfinally.pojo.entity.DemandFeedback;

/**
 * 需求反馈服务接口
 */
public interface DemandFeedbackService {
    
    /**
     * 创建需求反馈
     * @param feedback 反馈信息
     * @return 创建结果
     */
    boolean createFeedback(DemandFeedback feedback);
    
    /**
     * 根据ID获取反馈详情
     * @param id 反馈ID
     * @return 反馈详情
     */
    DemandFeedback getFeedbackById(Long id);
    
    /**
     * 获取反馈列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<DemandFeedback> getFeedbackPage(int pageNum, int pageSize, String status);
    
    /**
     * 审核通过反馈
     * @param id 反馈ID
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     * @param remark 审核备注
     * @return 审核结果
     */
    boolean approveFeedback(Long id, Long adminId, String adminName, String remark);
    
    /**
     * 审核拒绝反馈
     * @param id 反馈ID
     * @param adminId 管理员ID
     * @param adminName 管理员姓名
     * @param remark 审核备注
     * @return 审核结果
     */
    boolean rejectFeedback(Long id, Long adminId, String adminName, String remark);
    
    /**
     * 根据需求ID获取反馈
     * @param demandId 需求ID
     * @return 反馈信息
     */
    DemandFeedback getFeedbackByDemandId(Long demandId);
}