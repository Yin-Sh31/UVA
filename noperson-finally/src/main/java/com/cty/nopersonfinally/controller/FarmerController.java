package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.vo.DemandVO;
import com.cty.nopersonfinally.pojo.vo.FarmerInfoVO;
import com.cty.nopersonfinally.service.AllDemandService;
import com.cty.nopersonfinally.service.FarmerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 农户信息控制器
 * 提供飞手查询农户信息的接口
 */
@RestController
@RequestMapping("/farmer")
@Api(tags = "农户信息接口")
public class FarmerController {

    @Autowired
    private FarmerService farmerService;
    
    @Autowired
    private AllDemandService allDemandService;

    /**
     * 使用数据库查询但手动转换结果
     * 从数据库获取数据后，手动创建新的FarmerInfoVO对象列表
     */
    @GetMapping("/flyer/getFlyerList")
    @PreAuthorize("hasRole('flyer')")
    public Result<?> testDbManual() {
        // 从数据库获取农户列表
        Page<FarmerInfoVO> farmerPage = farmerService.getFarmersForFlyer(1, 10);
        List<FarmerInfoVO> dbFarmers = farmerPage.getRecords();
        
        // 手动创建新的FarmerInfoVO对象列表，确保所有字段都被正确赋值
        List<FarmerInfoVO> resultFarmers = new ArrayList<>();
        for (FarmerInfoVO dbFarmer : dbFarmers) {
            FarmerInfoVO newFarmer = new FarmerInfoVO();
            newFarmer.setUserId(dbFarmer.getUserId());
            newFarmer.setUsername(dbFarmer.getUsername());
            newFarmer.setPhone(dbFarmer.getPhone());
            newFarmer.setAvatar(dbFarmer.getAvatar());
            newFarmer.setRealName(dbFarmer.getRealName());
            newFarmer.setRoleType(dbFarmer.getRoleType());
            newFarmer.setStatus(dbFarmer.getStatus());
            newFarmer.setBalance(dbFarmer.getBalance());
            newFarmer.setCreateTime(dbFarmer.getCreateTime());
            newFarmer.setLastLoginTime(dbFarmer.getLastLoginTime());
            resultFarmers.add(newFarmer);
        }
        
        return Result.ok(resultFarmers);
    }
    
    /**
     * 飞手根据ID查询单个农户详细信息及需求列表
     * 同时返回该农户的所有需求（当前和历史发布的需求），不使用Page包装
     */
    @GetMapping("/flyer/info")
    @PreAuthorize("hasRole('flyer')")
    @ApiOperation("飞手查询农户详细信息及需求列表")
    public Result<?> getFarmerInfo(
            @ApiParam("农户ID") @RequestParam Long farmerId) {
        
        try {
            // 获取农户信息
            FarmerInfoVO farmerInfo = farmerService.getFarmerInfoById(farmerId);
            if (farmerInfo == null) {
                return Result.error("农户信息不存在");
            }
            
            // 获取农户所有需求（不使用分页，获取全部需求列表）
            // 设置一个足够大的值确保获取所有记录，不使用默认的10条限制
            Page<DemandVO> demandsPage = allDemandService.getFarmerDemandsByType(farmerId, 1, 1000, null, null);
            List<DemandVO> demandsList = demandsPage.getRecords();
            
            // 创建返回数据对象
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("farmerInfo", farmerInfo);
            resultMap.put("demands", demandsList);
            resultMap.put("totalDemands", demandsList.size());
            
            return Result.ok(resultMap);
        } catch (Exception e) {
            return Result.error("查询农户信息及需求失败: " + e.getMessage());
        }
    }
}