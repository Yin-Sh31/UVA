package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.pojo.dto.DeviceMaintainDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.vo.DeviceMaintainVO;
import com.cty.nopersonfinally.service.DeviceMaintainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/device/maintain")
@Api(tags = "设备管理")
public class DeviceManageController {

    @Resource
    private DeviceMaintainService deviceMaintainService;

//    @ApiOperation("提交设备维护记录（飞手）")
//    @PostMapping("/create")
//    @PreAuthorize("hasRole('flyer')")
//    public Result<Long> createMaintainRecord(@Valid @RequestBody DeviceMaintainDTO dto, Principal principal) {
//        Long operatorId = Long.parseLong(principal.getName());
//        Long recordId = deviceMaintainService.createMaintainRecord(dto, operatorId);
//        return Result.ok(recordId);
//    }

    @ApiOperation("审核维护记录（管理员）")
    @PostMapping("/audit/{recordId}")
    @PreAuthorize("hasRole('admin')")
    public Result auditRecord(@PathVariable Long recordId,
                              @RequestParam Integer status, // 1-通过 2-拒绝
                              Principal principal) {
        Long adminId = Long.parseLong(principal.getName());
        boolean success = deviceMaintainService.auditMaintainRecord(recordId, status, adminId);
        return success ? Result.ok("审核成功") : Result.error("审核失败");
    }

    @ApiOperation("查询设备维护记录")
    @GetMapping("/list/{deviceId}")
    public Result<Page<DeviceMaintainVO>> getMaintainRecords(@PathVariable Long deviceId,
                                                             @RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        Page<DeviceMaintainVO> page = deviceMaintainService.getDeviceMaintainRecords(deviceId, pageNum, pageSize);
        return Result.ok(page);
    }

    @ApiOperation("查询设备最近维护记录")
    @GetMapping("/last/{deviceId}")
    public Result<DeviceMaintainVO> getLastMaintain(@PathVariable Long deviceId) {
        return Result.ok(deviceMaintainService.getLastMaintainRecord(deviceId));
    }

    @ApiOperation("检查设备是否需要维护")
    @GetMapping("/check/{deviceId}")
    public Result<Boolean> checkNeedMaintain(@PathVariable Long deviceId) {
        return Result.ok(deviceMaintainService.checkNeedMaintain(deviceId));
    }
}