package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.dto.UserInfoUpdateDTO;
import com.cty.nopersonfinally.pojo.dto.BalanceUpdateDTO;
import com.cty.nopersonfinally.pojo.entity.SysUser;
import com.cty.nopersonfinally.pojo.vo.UserInfoVO;
import com.cty.nopersonfinally.service.UserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDateTime;

import com.cty.nopersonfinally.utils.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制器
 * 提供用户个人信息查看和修改功能
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户信息接口", description = "用户个人信息查看和修改接口")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private JWTUtil jwtUtil;

    /**
     * 获取当前登录用户的个人信息
     * 适用于农户、飞手、机主查看自己的基本信息
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('farmer', 'flyer', 'owner')")
    @Operation(summary = "获取用户个人信息", description = "获取当前登录用户的基本信息")
    public Result<?> getUserInfo(@RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        
        UserInfoVO userInfo = userInfoService.getUserInfo(userId);
        return Result.ok(userInfo);
    }
    
    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('farmer', 'flyer', 'owner')")
    @Operation(summary = "查询用户余额", description = "查询当前登录用户的余额")
    public Result<?> getUserBalance(@RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        
        Double balance = userInfoService.getUserBalance(userId);
        return Result.ok(balance);
    }
    
    @PostMapping("/balance/update")
    @PreAuthorize("hasAnyRole('farmer', 'flyer', 'owner')")
    @Operation(summary = "修改用户余额", description = "修改当前登录用户的余额（正数增加，负数减少）")
    public Result<?> updateUserBalance(@RequestHeader("Authorization") String token,
                                       @RequestBody @Valid BalanceUpdateDTO balanceUpdateDTO) {
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        
        boolean success = userInfoService.updateUserBalance(userId, balanceUpdateDTO.getAmount());
        if (success) {
            return Result.ok("余额更新成功");
        } else {
            return Result.error("余额更新失败");
        }
    }
    
    @PostMapping("/balance/recharge")
    @PreAuthorize("hasAnyRole('farmer', 'flyer', 'owner')")
    @Operation(summary = "充值金额", description = "充值金额到当前登录用户的账户")
    public Result<?> rechargeBalance(@RequestHeader("Authorization") String token,
                                     @RequestBody @Valid BalanceUpdateDTO balanceUpdateDTO) {
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        
        boolean success = userInfoService.rechargeBalance(userId, balanceUpdateDTO.getAmount());
        if (success) {
            return Result.ok("充值成功");
        } else {
            return Result.error("充值失败");
        }
    }
    
    @PostMapping("/balance/withdraw")
    @PreAuthorize("hasAnyRole('farmer', 'flyer', 'owner')")
    @Operation(summary = "提现余额", description = "从当前登录用户的账户中提现金额")
    public Result<?> withdrawBalance(@RequestHeader("Authorization") String token,
                                     @RequestBody @Valid BalanceUpdateDTO balanceUpdateDTO) {
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        
        boolean success = userInfoService.withdrawBalance(userId, balanceUpdateDTO.getAmount());
        if (success) {
            return Result.ok("提现成功");
        } else {
            return Result.error("提现失败");
        }
    }

    /**
     * 更新当前登录用户的个人信息
     * 适用于农户、飞手、机主修改自己的基本信息
     */
    @PutMapping("/info")
    @PreAuthorize("hasAnyRole('farmer', 'flyer', 'owner')")
    @Operation(summary = "更新用户个人信息", description = "更新当前登录用户的基本信息")
    public Result<?> updateUserInfo(
            @Valid @RequestBody UserInfoUpdateDTO dto,
            @RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        
        boolean success = userInfoService.updateUserInfo(dto, userId);
        if (success) {
            return Result.ok("信息更新成功");
        } else {
            return Result.error("信息更新失败");
        }
    }
    
    /**
     * 查询用户收支明细
     * 支持分页查询，包含时间范围和交易类型筛选
     * 适用于农户、飞手查询自己的收入支出记录
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('farmer', 'flyer')")
    @Operation(summary = "查询收支明细", description = "查询当前登录用户的收支明细记录，支持分页和时间范围筛选")
    public Result<?> getUserTransactions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss")
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss")
            @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "交易类型：收入(INCOME)、支出(EXPENSE)，不传则查询全部")
            @RequestParam(required = false) String transactionType) {
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        
        // 创建分页对象
        Page<?> pageResult = userInfoService.getUserTransactions(
                userId, 
                page, 
                pageSize, 
                startTime, 
                endTime, 
                transactionType);
        
        return Result.ok(pageResult);
    }
}