package com.cty.nopersonfinally.controller;


import com.cty.nopersonfinally.pojo.dto.PaymentDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.vo.PaymentVO;
import com.cty.nopersonfinally.service.PaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 支付控制器
 */
@RestController
@RequestMapping("/payment")
@Api(tags = "支付管理")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * 需求支付接口
     * @param demandId 需求ID
     * @param request 请求对象（用于获取用户信息）
     * @return 支付结果
     */
    @PostMapping("/demand/{demandId}")
    @ApiOperation("支付需求费用")
    public Result<?> payDemand(
            @ApiParam(value = "需求ID", required = true) @PathVariable Long demandId,
            HttpServletRequest request) {
        try {
            // 从请求中获取用户ID（实际项目中应从JWT token中解析）
            Long userId = (Long) request.getAttribute("userId");
            
            PaymentVO paymentVO = paymentService.payDemand(demandId, userId);
            return Result.ok(paymentVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建支付订单接口
     * @param dto 支付DTO
     * @param orderId 订单ID（查询参数）
     * @param orderType 订单类型（查询参数）
     * @param amount 支付金额（查询参数）
     * @param request 请求对象（用于获取用户信息）
     * @return 支付订单信息
     */
    @PostMapping("/create")
    @ApiOperation("创建支付订单")
    public Result<?> createPayment(
            @ApiParam(value = "支付信息", required = true) @RequestBody PaymentDTO dto,
            @ApiParam(value = "订单ID", required = true) @RequestParam String orderId,
            @ApiParam(value = "订单类型", required = true) @RequestParam String orderType,
            @ApiParam(value = "支付金额", required = true) @RequestParam Double amount,
            HttpServletRequest request) {
        try {
            // 从请求中获取用户ID（实际项目中应从JWT token中解析）
            Long userId = (Long) request.getAttribute("userId");
            
            PaymentVO paymentVO = paymentService.createPayment(dto, userId, orderId, orderType, amount);
            return Result.ok(paymentVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询支付状态接口
     * @param orderType 订单类型
     * @param orderId 订单ID
     * @param request 请求对象（用于获取用户信息）
     * @return 支付状态信息
     */
    @GetMapping("/status")
    @ApiOperation("查询支付状态")
    public Result<?> queryPaymentStatus(
            @ApiParam(value = "订单类型", required = true) @RequestParam String orderType,
            @ApiParam(value = "订单ID", required = true) @RequestParam String orderId,
            HttpServletRequest request) {
        try {
            // 从请求中获取用户ID（实际项目中应从JWT token中解析）
            Long userId = (Long) request.getAttribute("userId");
            
            PaymentVO paymentVO = paymentService.queryPaymentStatus(orderType, orderId, userId);
            return Result.ok(paymentVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}