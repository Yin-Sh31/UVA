package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.entity.FlyerEvaluation;
import com.cty.nopersonfinally.service.FlyerEvaluationService;
import com.cty.nopersonfinally.pojo.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 飞手评价控制器
 */
@RestController
@RequestMapping("/api/flyer/evaluation")
public class FlyerEvaluationController {

    @Autowired
    private FlyerEvaluationService flyerEvaluationService;

    /**
     * 添加评价
     */
    @PostMapping("/add")
    public Result addEvaluation(@RequestBody FlyerEvaluation evaluation) {
        try {
            // 检查是否已评价
            boolean hasEvaluated = flyerEvaluationService.hasEvaluated(evaluation.getOrderId(), evaluation.getFarmerId());
            if (hasEvaluated) {
                return Result.error("该订单已评价");
            }
            
            boolean result = flyerEvaluationService.addEvaluation(evaluation);
            if (result) {
                return Result.ok("评价成功");
            } else {
                return Result.error("评价失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("评价异常");
        }
    }

    /**
     * 根据飞手用户ID获取评价列表
     */
    @GetMapping("/list/{userId}")
    public Result getByFlyerId(@PathVariable Long userId) {
        try {
            List<FlyerEvaluation> evaluations = flyerEvaluationService.getByFlyerId(userId);
            return Result.ok(evaluations);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评价列表失败");
        }
    }

    /**
     * 获取飞手好评率
     */
    @GetMapping("/positive-rate/{userId}")
    public Result getPositiveRate(@PathVariable Long userId) {
        try {
            Double positiveRate = flyerEvaluationService.getPositiveRate(userId);
            return Result.ok(positiveRate != null ? positiveRate : 0.0);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取好评率失败");
        }
    }

    /**
     * 获取飞手评价总数
     */
    @GetMapping("/count/{userId}")
    public Result getEvaluationCount(@PathVariable Long userId) {
        try {
            Integer count = flyerEvaluationService.getEvaluationCount(userId);
            return Result.ok(count != null ? count : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取评价总数失败");
        }
    }

    /**
     * 检查是否已评价
     */
    @GetMapping("/check")
    public Result checkEvaluation(@RequestParam Long orderId, @RequestParam Long farmerId) {
        try {
            boolean hasEvaluated = flyerEvaluationService.hasEvaluated(orderId, farmerId);
            return Result.ok(hasEvaluated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查评价状态失败");
        }
    }

    /**
     * 更新飞手信用数据
     */
    @PostMapping("/update-credit/{flyerId}")
    public Result updateFlyerCredit(@PathVariable Long flyerId) {
        try {
            boolean result = flyerEvaluationService.updateFlyerCredit(flyerId);
            if (result) {
                return Result.ok("信用数据更新成功");
            } else {
                return Result.error("信用数据更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("信用数据更新异常");
        }
    }
}
