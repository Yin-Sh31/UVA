package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.entity.FlyerComplaint;
import com.cty.nopersonfinally.service.FlyerComplaintService;
import com.cty.nopersonfinally.pojo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 飞手投诉控制器
 */
@RestController
@RequestMapping("/api/flyer/complaint")
public class FlyerComplaintController {

    @Autowired
    private FlyerComplaintService flyerComplaintService;

    /**
     * 添加投诉
     */
    @PostMapping("/add")
    public Result addComplaint(@RequestBody FlyerComplaint complaint) {
        try {
            // 检查是否已投诉
            boolean hasComplained = flyerComplaintService.hasComplained(complaint.getOrderId(), complaint.getReporterId());
            if (hasComplained) {
                return Result.error("该订单已投诉");
            }
            
            boolean result = flyerComplaintService.addComplaint(complaint);
            if (result) {
                return Result.ok("投诉成功");
            } else {
                return Result.error("投诉失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("投诉异常");
        }
    }

    /**
     * 根据被投诉人ID获取投诉列表
     */
    @GetMapping("/list/{targetId}")
    public Result getByTargetId(@PathVariable Long targetId) {
        try {
            List<FlyerComplaint> complaints = flyerComplaintService.getByTargetId(targetId);
            return Result.ok(complaints);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取投诉列表失败");
        }
    }

    /**
     * 处理投诉
     */
    @PostMapping("/handle/{id}")
    public Result handleComplaint(@PathVariable Long id, @RequestParam String result) {
        try {
            boolean success = flyerComplaintService.handleComplaint(id, result);
            if (success) {
                return Result.ok("处理成功");
            } else {
                return Result.error("处理失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("处理异常");
        }
    }

    /**
     * 检查是否已投诉
     */
    @GetMapping("/check")
    public Result checkComplaint(@RequestParam Long orderId, @RequestParam Long reporterId) {
        try {
            boolean hasComplained = flyerComplaintService.hasComplained(orderId, reporterId);
            return Result.ok(hasComplained);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查投诉状态失败");
        }
    }

    /**
     * 获取待处理的投诉数量
     */
    @GetMapping("/pending/count")
    public Result countPendingComplaints() {
        try {
            Integer count = flyerComplaintService.countPendingComplaints();
            return Result.ok(count);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取待处理投诉数量失败");
        }
    }
}
