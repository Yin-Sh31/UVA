package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.mapper.DemandMapper;
import com.cty.nopersonfinally.mapper.SysUserMapper;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.DemandFeedback;
import com.cty.nopersonfinally.pojo.entity.OrderDemand;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.service.DemandFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 需求反馈控制器
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {
    
    @Autowired
    private DemandFeedbackService demandFeedbackService;
    
    @Autowired
    private DemandMapper demandMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 提交需求反馈
     * @param demandId 需求ID
     * @param data 反馈数据
     * @return 响应结果
     */
    @PostMapping("/demand/{demandId}")
    public Result submitDemandFeedback(@PathVariable Long demandId, @RequestBody FeedbackDTO data) {
        try {
            // 查询需求信息，获取飞手ID
            OrderDemand demand = demandMapper.selectById(demandId);
            if (demand == null) {
                return Result.error("需求不存在");
            }
            
            // 获取农户信息
            SysUser farmer = sysUserMapper.selectById(data.getUserId());
            String farmerName = farmer != null ? farmer.getUsername() : "农户用户";
            
            // 获取飞手信息
            Long flyerId = demand.getFlyerId();
            String flyerName = "飞手用户";
            if (flyerId != null) {
                SysUser flyer = sysUserMapper.selectById(flyerId);
                if (flyer != null) {
                    flyerName = flyer.getUsername();
                }
            }
            
            // 创建反馈实体
            DemandFeedback feedback = new DemandFeedback();
            feedback.setDemandId(demandId);
            feedback.setFarmerId(data.getUserId());
            feedback.setFarmerName(farmerName);
            feedback.setFlyerId(flyerId);
            feedback.setFlyerName(flyerName);
            feedback.setFeedbackType(data.getFeedbackType());
            feedback.setFeedbackContent(data.getFeedbackContent());
            feedback.setFeedbackImages(data.getFeedbackImages());
            
            // 保存反馈
            boolean success = demandFeedbackService.createFeedback(feedback);
            
            if (success) {
                return Result.ok("反馈提交成功");
            } else {
                return Result.error("反馈提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交反馈时发生错误");
        }
    }
}

/**
 * 反馈数据传输对象
 */
class FeedbackDTO {
    private String feedbackType; // 反馈类型，如"incomplete"表示需求未完成
    private String feedbackContent; // 反馈内容（文字描述）
    private String feedbackImages; // 反馈图片URLs，多个用逗号分隔
    private Long demandId; // 需求ID
    private Long userId; // 用户ID
    
    // getter和setter方法
    public String getFeedbackType() {
        return feedbackType;
    }
    
    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }
    
    public String getFeedbackContent() {
        return feedbackContent;
    }
    
    public void setFeedbackContent(String feedbackContent) {
        this.feedbackContent = feedbackContent;
    }
    
    public String getFeedbackImages() {
        return feedbackImages;
    }
    
    public void setFeedbackImages(String feedbackImages) {
        this.feedbackImages = feedbackImages;
    }
    
    public Long getDemandId() {
        return demandId;
    }
    
    public void setDemandId(Long demandId) {
        this.demandId = demandId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}