package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cty.nopersonfinally.config.JwtAuthenticationFilter;
import com.cty.nopersonfinally.pojo.dto.FlyerQualificationDTO;
import com.cty.nopersonfinally.pojo.dto.FlyerUpdateDTO;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.UserFlyer;
import com.cty.nopersonfinally.pojo.enums.ResultCode;
import com.cty.nopersonfinally.pojo.vo.UserFlyerVO;
import com.cty.nopersonfinally.pojo.vo.OwnerInfoVO;
import java.util.List;
import com.cty.nopersonfinally.service.UserFlyerService;
import com.cty.nopersonfinally.utils.JWTUtil;
import com.cty.nopersonfinally.utils.OssFileUtil;
import com.cty.nopersonfinally.utils.OSSUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;



@RestController
@RequestMapping("/flyer")
@RequiredArgsConstructor
@Api(tags = "飞手相关接口")

public class FlyerController {
    @Autowired
    private UserFlyerService flyerService;
    @Autowired
    private OssFileUtil ossFileUtil; // OSS文件工具类

    private static final Logger log = LoggerFactory.getLogger(FlyerController.class);

    // 1. 飞手上传资质（执照+保险）
    @PostMapping("/upload-qualification")
//    @PreAuthorize("hasRole('flyer')")
    @ApiOperation("飞手上传资质文件（执照和/或保险）")
    public Result<?> uploadQualification(
            @RequestParam(value = "licenseFile", required = false) MultipartFile licenseFile,
            @RequestParam(value = "insuranceFile", required = false) MultipartFile insuranceFile,
            @Valid FlyerQualificationDTO dto, // 移除@RequestBody注解，让Spring自动绑定表单参数
            @RequestHeader("Authorization") String token) {

        // 1. 检查至少上传了一个文件
        if ((licenseFile == null || licenseFile.isEmpty()) && (insuranceFile == null || insuranceFile.isEmpty())) {
            return Result.error("请至少上传一个资质文件（执照或保险）");
        }
        
        // 2. 校验文件类型（支持PDF和图片）
        List<String> allowTypes = Arrays.asList("image/jpeg", "image/png", "application/pdf");
        
        // 3. 如果提供了执照文件，校验其类型
        if (licenseFile != null && !licenseFile.isEmpty() && !allowTypes.contains(licenseFile.getContentType())) {
            return Result.error("执照文件仅支持JPG、PNG、PDF格式");
        }
        
        // 4. 如果提供了保险文件，则校验其类型
        if (insuranceFile != null && !insuranceFile.isEmpty() && !allowTypes.contains(insuranceFile.getContentType())) {
            return Result.error("保险文件仅支持JPG、PNG、PDF格式");
        }

        // 2. 解析用户ID
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        // 3. 处理已上传的旧文件
        UserFlyer existFlyer = flyerService.getByUserId(userId);
        if (existFlyer != null) {
            // 只有在提供了新的执照文件时才删除旧的执照文件
            if (licenseFile != null && !licenseFile.isEmpty()) {
                ossFileUtil.deleteFile(existFlyer.getLicenseUrl());
            }
            
            // 只有在提供了新的保险文件时才删除旧的保险文件
            if (insuranceFile != null && !insuranceFile.isEmpty()) {
                ossFileUtil.deleteFile(existFlyer.getInsuranceUrl());
            }
        }

        // 4. 确定飞手ID，使用userId作为flyerId的一部分或直接使用userId
        // 按照要求使用userId作为飞手唯一标识
        Long flyerId = userId;
        
        // 5. 上传新文件到指定路径：flyerQuanlify/{flyerId}
        String folderPath = "flyerQuanlify/" + flyerId;
        String licenseUrl = null;
        String insuranceUrl = null;
        
        // 只在提供了执照文件时才上传
        if (licenseFile != null && !licenseFile.isEmpty()) {
            licenseUrl = ossFileUtil.uploadFile(licenseFile, folderPath, allowTypes);
        }
        
        // 只在提供了保险文件时才上传
        if (insuranceFile != null && !insuranceFile.isEmpty()) {
            insuranceUrl = ossFileUtil.uploadFile(insuranceFile, folderPath, allowTypes);
        }

        // 5. 保存/更新资质信息
        UserFlyer flyer = existFlyer != null ? existFlyer : new UserFlyer();
        flyer.setUserId(userId);
        
        // 设置或更新执照信息
        if (licenseUrl != null) {
            flyer.setLicenseType(dto.getLicenseType());
            flyer.setLicenseNo(dto.getLicenseNo());
            flyer.setLicenseUrl(licenseUrl);
        }
        
        // 处理保险编号
        if (existFlyer == null) {
            // 对于新用户，如果没有提供保险编号，设置为空
            flyer.setInsuranceNo(dto.getInsuranceNo() != null ? dto.getInsuranceNo() : "");
        } else if (dto.getInsuranceNo() != null && !dto.getInsuranceNo().trim().isEmpty()) {
            // 对于老用户，如果提供了新的保险编号，则更新
            flyer.setInsuranceNo(dto.getInsuranceNo());
        }
        
        // 只有在提供了新的保险文件时才更新保险URL
        if (insuranceUrl != null) {
            flyer.setInsuranceUrl(insuranceUrl);
        } else if (existFlyer == null) {
            // 对于新用户，如果没有提供保险文件，设置为空
            flyer.setInsuranceUrl("");
        }
        // 对于老用户，如果没有提供新的保险文件，保留原有的保险URL
        // 保留现有简介或设置为空字符串
        if (existFlyer == null) {
            flyer.setIntroduction(""); // 新用户简介为空
        }
        flyerService.saveOrUpdate(flyer); // 支持更新
        // 6. 更新审核状态
        flyerService.updateUserAuditStatus(userId, 0); // 0-待审核
        return Result.ok("资质上传成功，等待平台审核");
    }


    /**
     * 飞手更新个人信息（位置、价格、是否空闲等）
     */
    @PutMapping("/update-info")
    @PreAuthorize("hasRole('flyer')")
    @ApiOperation("飞手更新个人信息")
    public Result<?> updateFlyerInfo(
            @Valid @RequestBody FlyerUpdateDTO dto,
            @RequestHeader("Authorization") String token) {
        log.debug("收到飞手更新个人信息请求，token格式: {}", token != null ? "存在" : "不存在");
        
        Long userId = JWTUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.error("从token中提取userId失败，返回未授权");
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        log.debug("飞手更新信息 - userId: {}", userId);
        boolean success = flyerService.updateFlyerInfo(dto, userId);
        
        if (success) {
            log.debug("飞手信息更新成功 - userId: {}", userId);
            return Result.ok("信息更新成功");
        } else {
            log.error("飞手信息更新失败 - userId: {}", userId);
            return Result.error("信息更新失败");
        }
    }

    /**
     * 获取飞手个人信息（不含敏感信息）
     */
    @GetMapping("/info")
    @PreAuthorize("hasRole('flyer')")
    @ApiOperation("获取飞手个人信息")
    public Result<UserFlyerVO> getFlyerInfo(@RequestHeader("Authorization") String token) {
        System.out.println("[FlyerController] 收到获取飞手个人信息请求");
        System.out.println("[FlyerController] 原始Authorization头: " + token);
        
        // 检查token格式是否包含Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            System.out.println("[FlyerController] Authorization头包含Bearer前缀");
            // 提取实际的token部分（去掉前缀）
            String actualToken = token.substring(7);
            System.out.println("[FlyerController] 提取的实际token: " + actualToken.substring(0, Math.min(20, actualToken.length())) + "...");
            
            // 使用提取的token进行验证
            Long userId = JWTUtil.getUserIdFromToken(actualToken);
            if (userId == null) {
                System.out.println("[FlyerController] 使用提取的token解析userId失败");
                // 尝试直接使用原始token（不带前缀）进行验证，作为备用方案
                System.out.println("[FlyerController] 尝试直接使用原始token进行验证（不带前缀）");
                userId = JWTUtil.getUserIdFromToken(token);
                if (userId == null) {
                    System.out.println("[FlyerController] 从token中提取userId失败，返回未授权");
                    return (Result<UserFlyerVO>) Result.error(ResultCode.UNAUTHORIZED);
                }
            }
            
            System.out.println("[FlyerController] 成功提取userId: " + userId);
            
            // 调用service层方法获取VO对象
            UserFlyerVO vo = flyerService.getFlyerInfoVO(userId);
            
            if (vo == null) {
                System.out.println("[FlyerController] 飞手信息不存在 - userId: " + userId);
                return (Result<UserFlyerVO>) Result.error("飞手信息不存在");
            }
            
            System.out.println("[FlyerController] 成功获取飞手信息 - userId: " + userId);
            return Result.ok(vo);
        } else {
            System.out.println("[FlyerController] Authorization头为空或不包含Bearer前缀");
            // 如果没有Bearer前缀，直接使用整个token进行验证
            if (token != null) {
                System.out.println("[FlyerController] 尝试直接使用整个token进行验证");
                Long userId = JWTUtil.getUserIdFromToken(token);
                if (userId != null) {
                    System.out.println("[FlyerController] 成功提取userId: " + userId);
                    
                    UserFlyerVO vo = flyerService.getFlyerInfoVO(userId);
                    if (vo != null) {
                        System.out.println("[FlyerController] 成功获取飞手信息 - userId: " + userId);
                        return Result.ok(vo);
                    } else {
                        System.out.println("[FlyerController] 飞手信息不存在 - userId: " + userId);
                        return (Result<UserFlyerVO>) Result.error("飞手信息不存在");
                    }
                }
            }
            
            System.out.println("[FlyerController] 从token中提取userId失败，返回未授权");
            return (Result<UserFlyerVO>) Result.error(ResultCode.UNAUTHORIZED);
        }
    }

    /**
     * 根据飞手ID获取飞手信息（用于农户查看飞手详情）
     */
    @GetMapping("/info/{flyerId}")
    @PreAuthorize("hasRole('farmer')")
    @ApiOperation("根据飞手ID获取飞手信息")
    public Result<UserFlyerVO> getFlyerInfoById(@PathVariable Long flyerId) {
        System.out.println("[FlyerController] 收到根据ID获取飞手信息请求 - flyerId: " + flyerId);
        
        // 调用service层方法获取VO对象
        UserFlyerVO vo = flyerService.getFlyerInfoVO(flyerId);
        
        if (vo == null) {
            System.out.println("[FlyerController] 飞手信息不存在 - flyerId: " + flyerId);
            return (Result<UserFlyerVO>) Result.error("飞手信息不存在");
        }
        
        System.out.println("[FlyerController] 成功获取飞手信息 - flyerId: " + flyerId);
        return Result.ok(vo);
    }


    /**
     * 管理员分页查询飞手列表（带筛选）
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    @ApiOperation("管理员分页查询飞手列表")
    public Result<?> getFlyerPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("技能等级") @RequestParam(required = false) String skillLevel,
            @ApiParam("审核状态：0-待审核、1-通过、2-拒绝") @RequestParam(required = false) Integer auditStatus) {
        return Result.ok(flyerService.getFlyerPage(pageNum, pageSize, skillLevel, auditStatus));
    }

    /**
     * 农户查询飞手列表（仅返回已审核通过的飞手）
     */
    @GetMapping("/farmer/list")
    @PreAuthorize("hasRole('farmer')")
    @ApiOperation("农户查询飞手列表")
    public Result<?> getFlyersForFarmer(
            @ApiParam("页码") @RequestParam(defaultValue = "1") int pageNum,
            @ApiParam("每页条数") @RequestParam(defaultValue = "10") int pageSize,
            @ApiParam("技能等级") @RequestParam(required = false) String skillLevel,
            @ApiParam("是否空闲：0-忙碌、1-空闲") @RequestParam(required = false) Integer isFree) {
        // 农户只能查询已审核通过的飞手
        IPage<UserFlyer> flyerPage = flyerService.getFlyerPage(pageNum, pageSize, skillLevel, 1);
        
        // 将UserFlyer转换为UserFlyerVO，确保返回的数据安全且适合展示
        IPage<UserFlyerVO> resultPage = new Page<>();
        resultPage.setRecords(flyerPage.getRecords().stream()
                .map(flyer -> flyerService.getFlyerInfoVO(flyer.getUserId()))
                .toList());
        resultPage.setCurrent(flyerPage.getCurrent());
        resultPage.setSize(flyerPage.getSize());
        resultPage.setTotal(flyerPage.getTotal());
        resultPage.setPages(flyerPage.getPages());
        
        // 如果指定了空闲状态，进一步过滤
        if (isFree != null) {
            List<UserFlyerVO> filteredList = resultPage.getRecords().stream()
                    .filter(vo -> vo.getIsFree() != null && vo.getIsFree().equals(isFree))
                    .toList();
            resultPage.setRecords(filteredList);
            resultPage.setTotal(filteredList.size());
            resultPage.setPages((int) Math.ceil((double) filteredList.size() / pageSize));
        }
        
        return Result.ok(resultPage);
    }

//    /**
//     * 农户邀请飞手接单
//     */
//    @PostMapping("/invite")
//    @PreAuthorize("hasRole('farmer')")
//    @ApiOperation("农户邀请飞手接单")
//    public Result<?> inviteFlyer(
//            @ApiParam("飞手用户ID") @RequestParam Long flyerUserId,
//            @ApiParam("需求类型：inspection-巡检、spray-喷洒") @RequestParam String demandType,
//            @ApiParam("需求ID") @RequestParam Long demandId,
//            @ApiParam("邀请备注") @RequestParam(required = false) String remark,
//            Principal principal) {
//        try {
//            Long farmerId = Long.parseLong(principal.getName());
//
//            // 构建邀请内容
//            String content = "您收到了新的作业邀请。";
//            if (remark != null && !remark.isEmpty()) {
//                content += "备注：" + remark;
//            }
//
//            // 发送邀请通知
//            notificationService.sendNotification(
//                    flyerUserId,
//                    "作业邀请",
//                    content,
//                    String.format("%s_INVITE", demandType.toUpperCase())
//            );
//
//            return Result.ok("邀请已发送");
//        } catch (Exception e) {
//            log.error("邀请飞手失败", e);
//            return Result.error("邀请失败，请重试");
//        }
//    }
    
    /**
     * 飞手查询所有机主列表
     */
    @GetMapping("/owners/list")
    @PreAuthorize("hasRole('flyer')")
    @ApiOperation("飞手查询所有机主列表")
    public Result<?> getAllOwners() {
        List<OwnerInfoVO> owners = flyerService.getAllOwners();
        return Result.ok(owners);
    }

}