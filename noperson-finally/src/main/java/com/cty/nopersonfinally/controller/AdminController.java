package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cty.nopersonfinally.pojo.dto.AuditDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.pojo.vo.UserFlyerVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.vo.DemandVO;
import com.cty.nopersonfinally.pojo.vo.DeviceDetailVO;
import com.cty.nopersonfinally.pojo.vo.FarmerInfoVO;
import com.cty.nopersonfinally.pojo.vo.UserFlyerVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerVO;
import com.cty.nopersonfinally.service.AllDemandService;
import com.cty.nopersonfinally.service.DemandFeedbackService;
import com.cty.nopersonfinally.service.DeviceService;
import com.cty.nopersonfinally.service.FarmerService;
import com.cty.nopersonfinally.service.MessageService;
import com.cty.nopersonfinally.service.SystemConfigService;
import com.cty.nopersonfinally.service.UserFlyerService;
import com.cty.nopersonfinally.service.UserOwnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


/**
 * 管理员控制器
 * 提供审核飞手、审核机主、查看需求、管理无人机资质等功能
 */
@RestController
@RequestMapping({"/admin", "/api/admin"})
@Api(tags = "管理员接口")
@PreAuthorize("hasRole('admin')") // 所有接口都需要管理员权限
public class AdminController {

    @Autowired
    private UserFlyerService userFlyerService;

    @Autowired
    private UserOwnerService userOwnerService;
    
    @Autowired
    private FarmerService farmerService;

    @Autowired
    private AllDemandService allDemandService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private DemandFeedbackService demandFeedbackService;
    
    @Autowired
    private SystemConfigService systemConfigService;

    // ==================== 农户管理 ====================
    
    /**
     * 分页查询农户列表（管理员）
     */
    @GetMapping("/farmers/page")
    @ApiOperation("分页查询农户列表")
    public Result<IPage<FarmerInfoVO>> getFarmerPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("关键词搜索") @RequestParam(required = false) String keyword) {
        return Result.ok(farmerService.getFarmersForAdmin(pageNum, pageSize, keyword));
    }

    // ==================== 飞手管理 ====================
    
    /**
     * 分页查询飞手列表（管理员）
     */
    @GetMapping("/flyers/page")
    @ApiOperation("分页查询飞手列表")
    public Result<IPage<UserFlyerVO>> getFlyerPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("技能等级") @RequestParam(required = false) String skillLevel,
            @ApiParam("审核状态：0-待审核、1-通过、2-拒绝") @RequestParam(required = false) Integer auditStatus) {
        // 获取飞手分页数据
        IPage<UserFlyer> flyerPage = userFlyerService.getFlyerPage(pageNum, pageSize, skillLevel, auditStatus);
        
        // 将UserFlyer转换为UserFlyerVO，确保返回的数据包含sys_user和user_flyer两个表的信息
        IPage<UserFlyerVO> resultPage = new Page<>();
        resultPage.setRecords(flyerPage.getRecords().stream()
                .map(flyer -> userFlyerService.getFlyerInfoVO(flyer.getUserId()))
                .toList());
        resultPage.setCurrent(flyerPage.getCurrent());
        resultPage.setSize(flyerPage.getSize());
        resultPage.setTotal(flyerPage.getTotal());
        resultPage.setPages(flyerPage.getPages());
        
        return Result.ok(resultPage);
    }

    /**
     * 审核飞手资质
     */
    @PostMapping("/flyers/audit")
    @ApiOperation("审核飞手资质")
    public Result<?> auditFlyer(@Valid @RequestBody AuditDTO dto, Principal principal) {
        // 设置审核人ID
        dto.setAuditorId(Long.parseLong(principal.getName()));
        userFlyerService.auditQualification(dto);
        return Result.ok("审核成功");
    }

    // ==================== 机主管理 ====================
    
    /**
     * 分页查询机主列表（管理员）
     */
    @GetMapping("/owners/page")
    @ApiOperation("分页查询机主列表")
    public Result<IPage<UserOwnerVO>> getOwnerPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("区域筛选") @RequestParam(required = false) String area,
            @ApiParam("审核状态：0-待审核、1-通过、2-拒绝") @RequestParam(required = false) Integer auditStatus) {
        return Result.ok(userOwnerService.getOwnerPage(pageNum, pageSize, area, auditStatus));
    }

    /**
     * 审核机主资质
     */
    @PostMapping("/owners/audit")
    @ApiOperation("审核机主资质")
    public Result<?> auditOwner(@Valid @RequestBody AuditDTO dto, Principal principal) {
        // 设置审核人ID
        dto.setAuditorId(Long.parseLong(principal.getName()));
        userOwnerService.auditQualification(dto);
        return Result.ok("审核成功");
    }

    // ==================== 需求管理 ====================
    
    /**
     * 查看所有需求列表
     */
    @GetMapping("/demands/page")
    @ApiOperation("查看所有需求列表")
    public Result<IPage<DemandVO>> getAllDemands(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("需求状态") @RequestParam(required = false) Integer status) {
        // 调用allDemandService的方法获取所有需求
        IPage<?> page = allDemandService.getAdminDemandPage(pageNum, pageSize, status);
        return Result.ok((IPage<DemandVO>) page);
    }

    /**
     * 获取需求详情
     */
    @GetMapping("/demands/{demandId}")
    @ApiOperation("获取需求详情")
    public Result<?> getDemandDetail(@PathVariable Long demandId) {
        // 获取需求详情并转换为VO
        com.cty.nopersonfinally.pojo.entity.OrderDemand demand = allDemandService.getById(demandId);
        if (demand == null) {
            return Result.error("需求不存在");
        }
        return Result.ok(allDemandService.convertToVO(demand));
    }
    
    /**
     * 获取需求原始数据（用于调试）
     */
    @GetMapping("/demands/{demandId}/raw")
    @ApiOperation("获取需求原始数据")
    public Result<?> getDemandRawData(@PathVariable Long demandId) {
        // 获取需求原始数据，不进行VO转换
        com.cty.nopersonfinally.pojo.entity.OrderDemand demand = allDemandService.getById(demandId);
        if (demand == null) {
            return Result.error("需求不存在");
        }
        return Result.ok(demand);
    }

    // ==================== 无人机资质管理 ====================
    
    /**
     * 获取待审核的无人机列表
     */
    @GetMapping("/devices/audit/page")
    @ApiOperation("获取待审核的无人机列表")
    public Result<IPage<DeviceDetailVO>> getDevicesForAudit(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("机主ID") @RequestParam(required = false) Long ownerId) {
        return Result.ok(deviceService.getDevicesForAudit(pageNum, pageSize, ownerId));
    }

    /**
     * 审核无人机资质
     */
    @PostMapping("/devices/{deviceId}/audit")
    @ApiOperation("审核无人机资质")
    public Result<?> auditDevice(
            @PathVariable Long deviceId,
            @ApiParam("审核结果：1-通过 2-拒绝") @RequestParam Integer status,
            @ApiParam("审核备注") @RequestParam(required = false) String remark,
            Principal principal) {
        Long adminId = Long.parseLong(principal.getName());
        boolean success = deviceService.auditDevice(deviceId, status, adminId, remark);
        return success ? Result.ok("审核成功") : Result.error("审核失败");
    }

    /**
     * 获取所有无人机列表（管理员）
     */
    @GetMapping("/devices/page")
    @ApiOperation("获取所有无人机列表")
    public Result<IPage<DeviceDetailVO>> getAllDevices(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("机主ID") @RequestParam(required = false) Long ownerId,
            @ApiParam("设备状态") @RequestParam(required = false) Integer status) {
        return Result.ok(deviceService.getAllDevices(pageNum, pageSize, ownerId, status));
    }

    /**
     * 获取无人机详情
     */
    @GetMapping("/devices/{deviceId}")
    @ApiOperation("获取无人机详情")
    public Result<DeviceDetailVO> getDeviceDetail(@PathVariable Long deviceId) {
        return Result.ok(deviceService.getDeviceDetail(deviceId));
    }

    // ==================== 消息管理 ====================
    
    /**
     * 获取所有会话列表（管理员）
     */
    @GetMapping("/messages/conversations")
    @ApiOperation("获取所有会话列表")
    public Result<?> getAllConversations(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 调用MessageService获取所有会话
            java.util.List<com.cty.nopersonfinally.pojo.vo.ConversationVO> conversations = messageService.getAllConversations(page, pageSize);
            return Result.ok(conversations);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取会话消息列表（管理员）
     */
    @GetMapping("/messages/{conversationId}")
    @ApiOperation("获取会话消息列表")
    public Result<?> getConversationMessages(
            @PathVariable @ApiParam("会话ID") String conversationId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 调用MessageService获取消息列表，管理员ID固定为1
            java.util.List<com.cty.nopersonfinally.pojo.vo.MessageVO> messages = messageService.getMessages(conversationId, 1L, page, pageSize);
            return Result.ok(messages);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取消息列表失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息（管理员）
     */
    @PostMapping("/messages/send")
    @ApiOperation("发送消息")
    public Result<?> sendMessage(
            @RequestBody @ApiParam("消息数据") com.cty.nopersonfinally.pojo.dto.MessageDTO messageDTO) {
        try {
            // 发送消息，管理员作为发送者
            com.cty.nopersonfinally.pojo.vo.MessageVO messageVO = messageService.sendMessage(1L, messageDTO); // 假设管理员ID为1
            return Result.ok(messageVO);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 标记消息为已读（管理员）
     */
    @PutMapping("/messages/{conversationId}/read")
    @ApiOperation("标记消息为已读")
    public Result<?> markMessagesAsRead(
            @PathVariable @ApiParam("会话ID") String conversationId) {
        try {
            // 标记已读
            Integer count = messageService.markMessagesAsRead(conversationId, 1L); // 假设管理员ID为1
            return Result.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "标记已读失败: " + e.getMessage());
        }
    }

    // ==================== 反馈管理 ====================
    
    /**
     * 获取反馈列表（分页）
     */
    @GetMapping("/feedback/list")
    @ApiOperation("获取反馈列表")
    public Result<?> getFeedbackList(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        try {
            // 使用Service获取真实的反馈列表
            IPage<?> feedbackPage = demandFeedbackService.getFeedbackPage(pageNum, pageSize, null);
            return Result.ok(feedbackPage);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取反馈列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/feedback/{feedbackId}")
    @ApiOperation("获取反馈详情")
    public Result<?> getFeedbackDetail(@PathVariable Long feedbackId) {
        try {
            // 使用Service获取真实的反馈详情
            Object feedback = demandFeedbackService.getFeedbackById(feedbackId);
            if (feedback == null) {
                return Result.error("反馈不存在");
            }
            return Result.ok(feedback);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取反馈详情失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过反馈
     */
    @PutMapping("/feedback/{feedbackId}/approve")
    @ApiOperation("审核通过反馈")
    public Result<?> approveFeedback(@PathVariable Long feedbackId, Principal principal) {
        try {
            // 获取管理员信息
            Long adminId = Long.parseLong(principal.getName());
            String adminName = "管理员"; // TODO: 从用户服务获取真实姓名
            
            // 使用Service审核通过反馈
            boolean success = demandFeedbackService.approveFeedback(feedbackId, adminId, adminName, "审核通过");
            if (success) {
                return Result.ok("审核通过");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "审核失败: " + e.getMessage());
        }
    }

    /**
     * 审核不通过反馈
     */
    @PutMapping("/feedback/{feedbackId}/reject")
    @ApiOperation("审核不通过反馈")
    public Result<?> rejectFeedback(@PathVariable Long feedbackId, Principal principal) {
        try {
            // 获取管理员信息
            Long adminId = Long.parseLong(principal.getName());
            String adminName = "管理员"; // TODO: 从用户服务获取真实姓名
            
            // 使用Service审核拒绝反馈
            boolean success = demandFeedbackService.rejectFeedback(feedbackId, adminId, adminName, "审核不通过");
            if (success) {
                return Result.ok("审核不通过");
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "审核失败: " + e.getMessage());
        }
    }

    // ==================== 系统配置管理 ====================
    
    /**
     * 获取飞手接单控制状态
     */
    @GetMapping("/system/flyer-order-status")
    @ApiOperation("获取飞手接单控制状态")
    public Result<?> getFlyerOrderStatus() {
        try {
            boolean isBanned = systemConfigService.isFlyerOrderBanned();
            return Result.ok(isBanned);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置飞手接单控制状态
     */
    @PutMapping("/system/flyer-order-status")
    @ApiOperation("设置飞手接单控制状态")
    public Result<?> setFlyerOrderStatus(@RequestParam Boolean banned) {
        try {
            boolean success = systemConfigService.setFlyerOrderBan(banned);
            if (success) {
                return Result.ok("设置成功");
            } else {
                return Result.error("设置失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "设置失败: " + e.getMessage());
        }
    }
}

/**
 * 反馈VO类
 */
class FeedbackVO {
    private Long id;
    private Long demandId;
    private String farmerName;
    private String flyerName;
    private String feedbackType;
    private String feedbackContent;
    private String feedbackImages;
    private String createTime;
    private String status;
    private Long flyerId;
    private String flyerPhone;
    
    // 构造函数
    public FeedbackVO(Long id, Long demandId, String farmerName, String flyerName, 
                     String feedbackType, String feedbackContent, String feedbackImages,
                     String createTime, String status, Long flyerId, String flyerPhone) {
        this.id = id;
        this.demandId = demandId;
        this.farmerName = farmerName;
        this.flyerName = flyerName;
        this.feedbackType = feedbackType;
        this.feedbackContent = feedbackContent;
        this.feedbackImages = feedbackImages;
        this.createTime = createTime;
        this.status = status;
        this.flyerId = flyerId;
        this.flyerPhone = flyerPhone;
    }
    
    // getter和setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public String getFlyerName() { return flyerName; }
    public void setFlyerName(String flyerName) { this.flyerName = flyerName; }
    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
    public String getFeedbackContent() { return feedbackContent; }
    public void setFeedbackContent(String feedbackContent) { this.feedbackContent = feedbackContent; }
    public String getFeedbackImages() { return feedbackImages; }
    public void setFeedbackImages(String feedbackImages) { this.feedbackImages = feedbackImages; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getFlyerId() { return flyerId; }
    public void setFlyerId(Long flyerId) { this.flyerId = flyerId; }
    public String getFlyerPhone() { return flyerPhone; }
    public void setFlyerPhone(String flyerPhone) { this.flyerPhone = flyerPhone; }
}