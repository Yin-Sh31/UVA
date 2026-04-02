package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cty.nopersonfinally.pojo.dto.OwnerQualificationDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.dto.UserOwnerDTO;
import com.cty.nopersonfinally.pojo.entity.UserOwner;
import com.cty.nopersonfinally.pojo.vo.UserOwnerDetailVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerDeviceStatVO;
import com.cty.nopersonfinally.pojo.vo.UserOwnerVO;
import com.cty.nopersonfinally.service.UserOwnerService;
import com.cty.nopersonfinally.utils.JWTUtil;
import com.cty.nopersonfinally.utils.OssFileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 机主控制器
 */
@RestController
@RequestMapping("/owner")
@RequiredArgsConstructor
@Tag(name = "机主接口", description = "机主资质管理、设备统计等接口")
public class UserOwnerController {

    @Autowired
    private UserOwnerService ownerService;

    @Autowired
    private OssFileUtil ossFileUtil;

    /**
     * 机主上传资质文件
     */
    @PostMapping("/upload-qualification")
    @PreAuthorize("hasRole('owner')")
    @Operation(summary = "机主上传资质文件", description = "上传营业执照或其他资质文件")
    public Result<?> uploadQualification(
            @RequestParam(value = "licenseFile", required = false) MultipartFile licenseFile,
            @Valid OwnerQualificationDTO dto,
            @RequestHeader("Authorization") String token) {

        // 1. 检查是否上传了文件
        if (licenseFile == null || licenseFile.isEmpty()) {
            return Result.error("请上传资质文件");
        }
        
        // 2. 校验文件类型（支持PDF和图片）
        List<String> allowTypes = Arrays.asList("image/jpeg", "image/png", "application/pdf");
        if (!allowTypes.contains(licenseFile.getContentType())) {
            return Result.error("文件仅支持JPG、PNG、PDF格式");
        }

        // 3. 解析用户ID
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }

        // 4. 处理已上传的旧文件
        UserOwner existOwner = ownerService.getByUserId(userId);
        if (existOwner != null && existOwner.getLicenseUrls() != null && !existOwner.getLicenseUrls().isEmpty()) {
            // 删除旧的执照文件
            String[] oldUrls = existOwner.getLicenseUrls().split(",");
            for (String oldUrl : oldUrls) {
                if (!oldUrl.trim().isEmpty()) {
                    ossFileUtil.deleteFile(oldUrl);
                }
            }
        }

        // 5. 上传新文件到指定路径
        String folderPath = "ownerQuanlify/" + userId;
        String licenseUrl = ossFileUtil.uploadFile(licenseFile, folderPath, allowTypes);

        // 6. 保存/更新资质信息
        UserOwnerDTO ownerDTO = new UserOwnerDTO();
        ownerDTO.setLicenseType(dto.getLicenseType());
        ownerDTO.setLicenseNumber(dto.getLicenseNumber());
        ownerDTO.setCommonArea(dto.getCommonArea());
        ownerDTO.setLicenseUrls(Arrays.asList(licenseUrl));

        // 7. 调用服务层保存
        ownerService.submitQualification(ownerDTO, userId);
        return Result.ok("资质上传成功，等待平台审核");
    }

    /**
     * 提交/更新机主资质资料
     */
    @PostMapping("/qualification/submit")
    @PreAuthorize("hasRole('owner')")
    @Operation(summary = "提交机主资质资料", description = "新用户提交或审核未通过用户重新提交")
    public Result<?> submitQualification(
            @Valid @RequestBody UserOwnerDTO dto,
            @RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        // 确保DTO不为null且picture字段得到处理
        if (dto == null) {
            return Result.error("请求数据不能为空");
        }
        // picture字段会在service层进行处理和保存
        ownerService.submitQualification(dto, userId);
        return Result.ok("资质资料提交成功，请等待审核");
    }

    /**
     * 获取当前机主详情
     */
    @GetMapping("/detail")
    @PreAuthorize("hasRole('owner')")
    @Operation(summary = "获取机主详情", description = "查询当前登录机主的详细信息")
    public Result<?> getOwnerDetail(@RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        return Result.ok(ownerService.getDetailByUserId(userId));
    }

    /**
     * 更新常用作业区域
     */
    @PutMapping("/common-area")
    @PreAuthorize("hasRole('owner')")
    @Operation(summary = "更新常用作业区域")
    public Result<?> updateCommonArea(
            @RequestParam String commonArea,
            @RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        ownerService.updateCommonArea(commonArea, userId);
        return Result.ok("常用区域更新成功");
    }

    /**
     * 获取设备统计信息
     */
    @GetMapping("/device/stat")
    @PreAuthorize("hasRole('owner')")
    @Operation(summary = "获取设备统计数据", description = "查询机主管理的设备数量及分布")
    public Result<?> getDeviceStat(@RequestHeader("Authorization") String token) {
        // 从token中获取用户ID
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("无效的token");
        }
        UserOwner owner = ownerService.getByUserId(userId);
        return Result.ok(ownerService.getDeviceStat(owner.getId()));
    }

    /**
     * 分页查询机主列表（管理员用）
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "分页查询机主列表", description = "管理员查询机主列表，支持区域和审核状态筛选")
    public Result<IPage<UserOwnerVO>> getOwnerPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) Integer auditStatus) {
        return Result.ok(ownerService.getOwnerPage(pageNum, pageSize, area, auditStatus));
    }
}