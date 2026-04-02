package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.OrderDemandDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.OrderDemand;
import com.cty.nopersonfinally.pojo.enums.OrderTypeEnum;
import com.cty.nopersonfinally.pojo.enums.ResultCode;
import com.cty.nopersonfinally.pojo.vo.MatchResultVO;
import com.cty.nopersonfinally.pojo.vo.DemandVO;
import com.cty.nopersonfinally.service.DemandMatchService;
import com.cty.nopersonfinally.service.AllDemandService;
import com.cty.nopersonfinally.service.SystemConfigService;
import com.cty.nopersonfinally.utils.BusinessException;
import com.cty.nopersonfinally.utils.JWTUtil;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需求控制器
 */
@RestController
@RequestMapping({"/demand", "/demands", "/spray-demand"}) // 添加额外路径映射以兼容前端错误请求
@RequiredArgsConstructor
public class DemandController {

    @Autowired
    private DemandMatchService demandMatchService;
    @Autowired
    private AllDemandService allDemandService;
    @Autowired
    private SystemConfigService systemConfigService;
    /**
     * 农户创建订单（支持喷洒和巡检）
     */
    @PostMapping("/order/create")
    @PreAuthorize("hasRole('farmer')")
    public Result<?> createOrder(
            @RequestBody OrderDemandDTO dto,
            @RequestHeader("Authorization") String token) {
        try {
            // 1. 解析农户ID
            Long farmerId = JWTUtil.getUserIdFromToken(token);
            if (farmerId == null) {
                return Result.error(ResultCode.UNAUTHORIZED);
            }

            // 2. 校验订单类型
            if (dto.getOrderType() == null) {
                return Result.error(ResultCode.INVALID_PARAMS.getCode(), "订单类型不能为空");
            }

            // 3. 创建订单
            Long orderId = allDemandService.createOrder(dto, farmerId);
            // 创建包含消息和订单ID的Map
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("message", dto.getOrderType() == 1 ? "喷洒需求发布成功，等待匹配飞手" : "巡检订单创建成功");
            resultMap.put("orderId", orderId);
            return Result.ok(resultMap);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 处理旧的喷洒需求创建接口
     * 兼容前端调用/demand/spray/publish路径
     */
    @PostMapping("/spray/publish")
    @PreAuthorize("hasRole('farmer')")
    public Result<?> publishSprayDemand(
            @RequestBody OrderDemandDTO dto,
            @RequestHeader("Authorization") String token) {
        // 调用现有的createOrder方法处理请求
        return createOrder(dto, token);
    }

    /**
     * 农户查看自己所有需求列表（包含喷洒和巡检）
     */
    @GetMapping({"/spray/farmer/list", "/farmer/all-list"})
    @PreAuthorize("hasRole('farmer')")
    public Result<?> getFarmerDemandList(
            Principal principal,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String statusStr) {
        return getFarmerDemandsByType(principal, pageNum, pageSize, statusStr, null);
    }
    
    /**
     * 农户只查看自己的喷洒需求列表
     */
    @GetMapping("/spray/farmer/spray-only")
    @PreAuthorize("hasRole('farmer')")
    public Result<?> getFarmerSprayDemands(
            Principal principal,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String statusStr) {
        return getFarmerDemandsByType(principal, pageNum, pageSize, statusStr, OrderTypeEnum.SPRAY.getCode());
    }
    
    /**
     * 农户只查看自己的巡检需求列表
     */
    @GetMapping("/spray/farmer/inspection-only")
    @PreAuthorize("hasRole('farmer')")
    public Result<?> getFarmerInspectionDemands(
            Principal principal,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String statusStr) {
        return getFarmerDemandsByType(principal, pageNum, pageSize, statusStr, OrderTypeEnum.INSPECTION.getCode());
    }
    
    /**
     * 按类型查询农户需求的通用方法
     */
    private Result<?> getFarmerDemandsByType(
            Principal principal,
            int pageNum,
            int pageSize,
            String statusStr,
            Integer orderType) {
        try {
            // 获取当前登录农户ID
            Long farmerId = Long.parseLong(principal.getName());
            
            // 处理状态参数，将字符串转换为Integer
            Integer status = null;
            if (statusStr != null && !statusStr.isEmpty() && !"undefined".equals(statusStr)) {
                try {
                    status = Integer.parseInt(statusStr);
                } catch (NumberFormatException e) {
                    // 无效的数字格式，设置为null
                    status = null;
                }
            }
            
            // 记录查询信息日志
            String demandTypeDesc = orderType != null ? (orderType == OrderTypeEnum.SPRAY.getCode() ? "喷洒" : "巡检") : "所有";
            System.out.println("查询农户" + demandTypeDesc + "需求列表 - 农户ID: " + farmerId + ", 页码: " + pageNum + ", 每页数量: " + pageSize + ", 状态: " + status);
            
            // 调用service层方法获取需求列表（按类型筛选）
            Page<DemandVO> demandPage = allDemandService.getFarmerDemandsByType(farmerId, pageNum, pageSize, status, orderType);
            
            // 记录查询结果日志
            System.out.println("查询结果 - 总记录数: " + demandPage.getTotal() + ", 当前页记录数: " + demandPage.getRecords().size());
            
            // 调整返回格式，确保前端能正确获取数据
            // 创建包含必要字段的响应对象
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", demandPage.getTotal()); // 总记录数
            resultMap.put("pages", demandPage.getPages()); // 总页数
            resultMap.put("current", demandPage.getCurrent()); // 当前页码
            resultMap.put("size", demandPage.getSize()); // 每页记录数
            resultMap.put("records", demandPage.getRecords()); // 记录列表
            
            return Result.ok(resultMap);
        } catch (Exception e) {
            System.out.println("获取需求列表异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取需求列表失败：" + e.getMessage());
        }
    }

    /**
     * 判断字符串是否为有效数字
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 智能匹配飞手和设备
     */
    @PostMapping("/spray/match")
    @PreAuthorize("hasRole('farmer')")
    public Result<List<MatchResultVO>> matchSprayDemand(@RequestBody OrderDemandDTO dto) {
        List<MatchResultVO> matchResults = demandMatchService.matchSprayDemand(dto);
        return Result.ok(matchResults);
    }
    
//    /**
//     * 农户查看其他农户需求列表（匿名化处理）
//     * 直接查看所有需求，不包含筛选功能，不展示取消的需求，展示待处理和已完成的需求
//     */
//    @GetMapping("/spray/other-farmers/list")
//    @PreAuthorize("hasRole('farmer')")
//    public Result<?> getOtherFarmersDemandsList(
//            Principal principal,
//            @RequestParam(defaultValue = "1") int pageNum,
//            @RequestParam(defaultValue = "10") int pageSize) {
//        try {
//            // 获取当前登录农户ID
//            Long currentFarmerId = Long.parseLong(principal.getName());
//
//            // 记录查询信息日志
//            System.out.println("查询其他农户需求列表 - 当前农户ID: " + currentFarmerId + ", 页码: " + pageNum + ", 每页数量: " + pageSize);
//
//            // 调用service层方法获取其他农户需求列表
//            Page<SprayDemandVO> demandPage = sprayDemandService.getOtherFarmersDemands(
//                    currentFarmerId, pageNum, pageSize, null, null, null);
//
//            // 记录查询结果日志
//            System.out.println("查询结果 - 总记录数: " + demandPage.getTotal() + ", 当前页记录数: " + demandPage.getRecords().size());
//
//            // 调整返回格式，确保前端能正确获取数据
//            // 创建包含必要字段的响应对象
//            Map<String, Object> resultMap = new HashMap<>();
//            resultMap.put("total", demandPage.getTotal()); // 总记录数
//            resultMap.put("pages", demandPage.getPages()); // 总页数
//            resultMap.put("current", demandPage.getCurrent()); // 当前页码
//            resultMap.put("size", demandPage.getSize()); // 每页记录数
//            resultMap.put("records", demandPage.getRecords()); // 记录列表
//
//            return Result.ok(resultMap);
//        } catch (Exception e) {
//            System.out.println("获取其他农户需求列表异常: " + e.getMessage());
//            e.printStackTrace();
//            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取其他农户需求列表失败：" + e.getMessage());
//        }
//    }
    
    /**
     * 飞手查询所有农户需求列表（主要是待接取的需求）
     * 飞手查询用户需求的接口
     */
    @GetMapping("/spray/flyer/all-demands")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> getFlyerAllDemandsList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String statusStr) {
        try {
            // 处理状态参数，将字符串转换为Integer
            Integer status = null;
            if (statusStr != null && !statusStr.isEmpty() && !"undefined".equals(statusStr)) {
                try {
                    status = Integer.parseInt(statusStr);
                } catch (NumberFormatException e) {
                    // 无效的数字格式，设置为null
                    status = null;
                }
            }
            
            // 记录查询信息日志
            System.out.println("飞手查询所有农户需求列表 - 页码: " + pageNum + ", 每页数量: " + pageSize + ", 技能等级: " + skillLevel + ", 位置: " + location + ", 状态: " + status);
            
            // 调用service层方法获取所有农户需求列表
            Page<DemandVO> demandPage = allDemandService.getAllFarmersDemandsForFlyer(
                    pageNum, pageSize, skillLevel, location, status);
            
            // 记录查询结果日志
            System.out.println("查询结果 - 总记录数: " + demandPage.getTotal() + ", 当前页记录数: " + demandPage.getRecords().size());
            
            // 调整返回格式，确保前端能正确获取数据
            // 创建包含必要字段的响应对象
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", demandPage.getTotal()); // 总记录数
            resultMap.put("pages", demandPage.getPages()); // 总页数
            resultMap.put("current", demandPage.getCurrent()); // 当前页码
            resultMap.put("size", demandPage.getSize()); // 每页记录数
            resultMap.put("records", demandPage.getRecords()); // 记录列表
            
            return Result.ok(resultMap);
        } catch (Exception e) {
            System.out.println("获取所有农户需求列表异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取所有农户需求列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 飞手只查询喷洒需求列表
     */
    @GetMapping("/spray/flyer/spray-only")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> getFlyerSprayOnlyDemandsList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String statusStr) {
        return getFlyerDemandsByType(pageNum, pageSize, skillLevel, location, statusStr, OrderTypeEnum.SPRAY.getCode());
    }
    
    /**
     * 飞手只查询巡检需求列表
     */
    @GetMapping("/spray/flyer/inspection-only")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> getFlyerInspectionOnlyDemandsList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String statusStr) {
        return getFlyerDemandsByType(pageNum, pageSize, skillLevel, location, statusStr, OrderTypeEnum.INSPECTION.getCode());
    }
    
    /**
     * 飞手按类型查询需求的通用方法
     */
    private Result<?> getFlyerDemandsByType(
            int pageNum,
            int pageSize,
            String skillLevel,
            String location,
            String statusStr,
            Integer orderType) {
        try {
            // 处理状态参数，将字符串转换为Integer
            Integer status = null;
            if (statusStr != null && !statusStr.isEmpty() && !"undefined".equals(statusStr)) {
                try {
                    status = Integer.parseInt(statusStr);
                } catch (NumberFormatException e) {
                    // 无效的数字格式，设置为null
                    status = null;
                }
            }
            
            // 记录查询信息日志
            String demandTypeDesc = orderType == OrderTypeEnum.SPRAY.getCode() ? "喷洒" : "巡检";
            System.out.println("飞手查询" + demandTypeDesc + "需求列表 - 页码: " + pageNum + ", 每页数量: " + pageSize + ", 技能等级: " + skillLevel + ", 位置: " + location + ", 状态: " + status);
            
            // 调用service层方法获取按类型筛选的需求列表
            Page<DemandVO> demandPage = allDemandService.getFarmersDemandsByTypeForFlyer(
                    pageNum, pageSize, skillLevel, location, status, orderType);
            
            // 记录查询结果日志
            System.out.println("查询结果 - 总记录数: " + demandPage.getTotal() + ", 当前页记录数: " + demandPage.getRecords().size());
            
            // 调整返回格式，确保前端能正确获取数据
            // 创建包含必要字段的响应对象
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", demandPage.getTotal()); // 总记录数
            resultMap.put("pages", demandPage.getPages()); // 总页数
            resultMap.put("current", demandPage.getCurrent()); // 当前页码
            resultMap.put("size", demandPage.getSize()); // 每页记录数
            resultMap.put("records", demandPage.getRecords()); // 记录列表
            
            return Result.ok(resultMap);
        } catch (Exception e) {
            System.out.println("获取" + (orderType == OrderTypeEnum.SPRAY.getCode() ? "喷洒" : "巡检") + "需求列表异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取" + (orderType == OrderTypeEnum.SPRAY.getCode() ? "喷洒" : "巡检") + "需求列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取需求详情
     */
    @GetMapping("/{demandId}")
    @PreAuthorize("hasRole('farmer') or hasRole('flyer')")
    public Result<?> getDemandDetail(@PathVariable String demandId, Principal principal) {
        try {
            // 增强处理：检查是否为NaN或其他非数字值
            if ("NaN".equals(demandId) || !isNumeric(demandId)) {
                return Result.ok(null); // 新建需求或无效ID时返回空对象
            }
            
            Long id = Long.parseLong(demandId);
            Long userId = Long.parseLong(principal.getName());
            String role = principal.toString().contains("farmer") ? "farmer" : "flyer";
            
            OrderDemand demand = allDemandService.getDemandById(id, userId, role);
        // 调用service层的实体转VO方法
            DemandVO vo = allDemandService.convertToVO(demand);
        return Result.ok(vo);
        } catch (NumberFormatException e) {
            return Result.error(ResultCode.INVALID_PARAMS.getCode(), "需求ID格式错误");
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
        }
    }
    
    /**
     * 农户取消需求
     */
    @PostMapping({"/cancel/{demandId}", "/spray/cancel/{demandId}"})
    @PreAuthorize("hasRole('farmer')")
    public Result<?> cancelDemand(
            @PathVariable Long demandId,
            @RequestParam(required = false) String reason,
            Principal principal) {
        try {
            // 检查需求ID是否为空
            if (demandId == null) {
                return Result.error(ResultCode.INVALID_PARAMS.getCode(), "需求ID不能为空");
            }
            
            // 获取当前登录农户ID
            Long farmerId = Long.parseLong(principal.getName());
            
            // 记录取消信息日志
            System.out.println("农户取消需求 - 农户ID: " + farmerId + ", 需求ID: " + demandId + ", 取消原因: " + reason);
            
            // 调用service层方法执行取消操作
            boolean success = allDemandService.cancelDemand(demandId, farmerId, reason);
            
            if (success) {
                return Result.ok("需求取消成功");
            } else {
                return Result.error("需求取消失败，请重试");
            }
        } catch (NumberFormatException e) {
            return Result.error(ResultCode.INVALID_PARAMS.getCode(), "需求ID格式错误");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.out.println("取消需求异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "取消需求失败：" + e.getMessage());
        }
    }
    
    /**
     * 飞手查询自己已接单的需求列表
     */
    @GetMapping("/spray/flyer/my-demands")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> getMyDemandsList(
            Principal principal,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String statusStr) {
        try {
            // 获取当前登录飞手ID
            Long flyerId = Long.parseLong(principal.getName());
            
            // 处理状态参数，将字符串转换为Integer
            Integer status = null;
            if (statusStr != null && !statusStr.isEmpty() && !"undefined".equals(statusStr)) {
                try {
                    status = Integer.parseInt(statusStr);
                } catch (NumberFormatException e) {
                    // 无效的数字格式，设置为null
                    status = null;
                }
            }
            
            // 记录查询信息日志
            System.out.println("飞手查询已接单需求列表 - 飞手ID: " + flyerId + ", 页码: " + pageNum + ", 每页数量: " + pageSize + ", 状态: " + status);
            
            // 调用service层方法获取飞手已接单的需求列表
            Page<DemandVO> demandPage = allDemandService.getFlyerDemands(flyerId, pageNum, pageSize, status);
            
            // 记录查询结果日志
            System.out.println("查询结果 - 总记录数: " + demandPage.getTotal() + ", 当前页记录数: " + demandPage.getRecords().size());
            
            // 调整返回格式，确保前端能正确获取数据
            // 创建包含必要字段的响应对象
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", demandPage.getTotal()); // 总记录数
            resultMap.put("pages", demandPage.getPages()); // 总页数
            resultMap.put("current", demandPage.getCurrent()); // 当前页码
            resultMap.put("size", demandPage.getSize()); // 每页记录数
            resultMap.put("records", demandPage.getRecords()); // 记录列表
            
            return Result.ok(resultMap);
        } catch (Exception e) {
            System.out.println("获取飞手已接单需求列表异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "获取飞手已接单需求列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 飞手接单
     */
    @PostMapping("/spray/accept/{demandId}")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> acceptDemand(
            @PathVariable String demandId,
            Principal principal) {
        try {
            // 1. 校验参数和转换类型
            if (demandId == null || "undefined".equals(demandId) || "".equals(demandId.trim())) {
                return Result.error(ResultCode.INVALID_PARAMS.getCode()+"需求ID不能为空");
            }
            
            Long id;
            try {
                id = Long.parseLong(demandId);
            } catch (NumberFormatException e) {
                return Result.error(ResultCode.INVALID_PARAMS.getCode(), "需求ID格式错误，请提供有效的数字");
            }
            
            // 2. 获取当前登录飞手ID
            Long flyerId = Long.parseLong(principal.getName());
            
            // 3. 获取需求信息，判断支付状态
            com.cty.nopersonfinally.pojo.entity.OrderDemand demand = allDemandService.getById(id);
            if (demand == null) {
                return Result.error(ResultCode.NOT_FOUND.getCode(), "需求不存在");
            }
            
            // 4. 根据需求状态和支付状态调用不同的service方法
            boolean success;
            if (demand.getPaymentStatus() != null && demand.getPaymentStatus() == 1) {
                // 已支付状态的需求
                success = allDemandService.acceptPaidDemand(id, flyerId);
            } else {
                // 未支付状态的需求
                success = allDemandService.acceptDemand(id, flyerId);
            }
            
            if (!success) {
                return Result.error("接单失败，请重试");
            }
            
            // 5. 查询更新后的需求信息并返回
            com.cty.nopersonfinally.pojo.entity.OrderDemand updatedDemand = allDemandService.getDemandById(id, flyerId, "flyer");
            DemandVO vo = allDemandService.convertToVO(updatedDemand);
            
            return Result.ok(vo);
        } catch (NumberFormatException e) {
            return Result.error(ResultCode.INVALID_PARAMS.getCode(), "ID格式错误");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            System.out.println("飞手接单异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "接单失败：" + e.getMessage());
        }
    }
    
    /**
     * 飞手完成作业并确认
     * 功能：飞手点击确认后，订单直接变为已完成状态，平台抽成10%，飞手获得90%
     */
    /**
     * 飞手开始工作
     */
    @PostMapping("/start-work")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> startWork(@RequestBody Map<String, Object> params, Principal principal) {
        try {
            // 从请求体中获取需求ID
            Long demandId = null;
            if (params.containsKey("demandId")) {
                Object demandIdObj = params.get("demandId");
                if (demandIdObj instanceof Number) {
                    demandId = ((Number) demandIdObj).longValue();
                } else if (demandIdObj instanceof String) {
                    demandId = Long.parseLong((String) demandIdObj);
                }
            }
            
            // 校验需求ID是否存在
            if (demandId == null) {
                return Result.error("需求ID不能为空");
            }
            
            // 获取当前登录飞手ID
            Long flyerId = Long.parseLong(principal.getName());
            
            // 调用service层开始工作方法
            boolean success = allDemandService.startWork(demandId, flyerId);
            
            if (!success) {
                return Result.error("开始工作失败，请重试");
            }
            
            return Result.ok(success);
        } catch (BusinessException e) {
            // 处理业务异常，如权限不足、状态错误等
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 记录系统异常日志
            System.out.println("飞手开始工作异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("系统异常，请稍后重试");
        }
    }
    
    /**
     * 飞手完成作业并确认
     * 功能：飞手点击确认后，订单直接变为已完成状态，平台抽成10%，飞手获得90%
     */
    @PostMapping("/complete-work")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> completeWork(@RequestBody Map<String, Object> params, Principal principal) {
        try {
            // 从请求体中获取需求ID
            Long demandId = null;
            if (params.containsKey("demandId")) {
                Object demandIdObj = params.get("demandId");
                if (demandIdObj instanceof Number) {
                    demandId = ((Number) demandIdObj).longValue();
                } else if (demandIdObj instanceof String) {
                    demandId = Long.parseLong((String) demandIdObj);
                }
            }
            
            // 校验需求ID是否存在
            if (demandId == null) {
                return Result.error("需求ID不能为空");
            }
            
            // 获取当前登录飞手ID
            Long flyerId = Long.parseLong(principal.getName());
            
            // 调用service层完成作业方法，该方法会自动处理状态变更和分成计算
            boolean success = allDemandService.completeWork(demandId, flyerId);
            
            return Result.ok(success);
        } catch (BusinessException e) {
            // 处理业务异常，如权限不足、状态错误等
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 记录系统异常日志
            e.printStackTrace();
            return Result.error("系统异常，请稍后重试");
        }
    }

    /**
     * 获取飞手接单控制状态（飞手端使用）
     */
    @GetMapping("/system/flyer-order-status")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> getFlyerOrderStatusForFlyer() {
        try {
            boolean isBanned = systemConfigService.isFlyerOrderBanned();
            return Result.ok(isBanned);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取状态失败");
        }
    }
}