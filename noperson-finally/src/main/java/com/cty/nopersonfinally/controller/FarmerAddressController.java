package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.FarmerFavoriteAddress;
import com.cty.nopersonfinally.mapper.FarmerFavoriteAddressMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * 农户常用地址控制器
 */
@RestController
@RequestMapping("/farmer/address")
@Api(tags = "农户常用地址接口")
public class FarmerAddressController {

    @Autowired
    private FarmerFavoriteAddressMapper addressMapper;

    /**
     * 获取当前农户的常用地址列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('farmer')")
    @ApiOperation("获取农户常用地址列表")
    public Result<List<FarmerFavoriteAddress>> getFavoriteAddresses(Principal principal) {
        Long farmerId = Long.parseLong(principal.getName());
        QueryWrapper<FarmerFavoriteAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("farmer_id", farmerId);
        List<FarmerFavoriteAddress> addresses = addressMapper.selectList(queryWrapper);
        return Result.ok(addresses);
    }

    /**
     * 根据需求类型获取常用地址
     */
    @GetMapping("/by-type")
    @PreAuthorize("hasRole('farmer')")
    @ApiOperation("根据需求类型获取常用地址")
    public Result<FarmerFavoriteAddress> getAddressByType(
            @ApiParam("需求类型：1-喷洒，2-巡检") @RequestParam Integer orderType,
            Principal principal) {
        Long farmerId = Long.parseLong(principal.getName());
        QueryWrapper<FarmerFavoriteAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("farmer_id", farmerId).eq("order_type", orderType);
        FarmerFavoriteAddress address = addressMapper.selectOne(queryWrapper);
        return Result.ok(address);
    }

    /**
     * 保存或更新常用地址
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('farmer')")
    @ApiOperation("保存或更新常用地址")
    public Result<?> saveFavoriteAddress(
            @RequestBody FarmerFavoriteAddress address,
            Principal principal) {
        Long farmerId = Long.parseLong(principal.getName());
        address.setFarmerId(farmerId);

        // 先查询是否已存在
        QueryWrapper<FarmerFavoriteAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("farmer_id", farmerId).eq("order_type", address.getOrderType());
        FarmerFavoriteAddress existing = addressMapper.selectOne(queryWrapper);

        if (existing != null) {
            // 更新
            address.setId(existing.getId());
            addressMapper.updateById(address);
        } else {
            // 新增
            addressMapper.insert(address);
        }
        return Result.ok("保存成功");
    }

    /**
     * 删除常用地址
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('farmer')")
    @ApiOperation("删除常用地址")
    public Result<?> deleteFavoriteAddress(
            @ApiParam("需求类型：1-喷洒，2-巡检") @RequestParam Integer orderType,
            Principal principal) {
        Long farmerId = Long.parseLong(principal.getName());
        QueryWrapper<FarmerFavoriteAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("farmer_id", farmerId).eq("order_type", orderType);
        addressMapper.delete(queryWrapper);
        return Result.ok("删除成功");
    }
}
