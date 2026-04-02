package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.Banner;
import com.cty.nopersonfinally.service.BannerService;
import com.cty.nopersonfinally.utils.OssFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 轮播图控制器
 */
@RestController
@RequestMapping
@Api(tags = "轮播图管理")
public class BannerController {
    
    @Autowired
    private BannerService bannerService;
    
    @Autowired
    private OssFileUtil ossFileUtil;
    
    // 服务器基础URL
    @Value("${server.url}")
    private String serverUrl;
    
    /**
     * 获取轮播图列表
     * @param type 用户类型：farmer(农户) 或 flyer(飞手)
     * @return 轮播图列表
     */
    @GetMapping("/api/banners")
    @ApiOperation("获取轮播图列表")
    public Result<List<Banner>> getBanners(
            @ApiParam(value = "用户类型：farmer(农户) 或 flyer(飞手)", required = true) 
            @RequestParam String type) {
        List<Banner> banners = bannerService.getBannersByType(type);
        // 处理新旧两种URL格式
        banners.forEach(banner -> {
            String imageUrl = banner.getImageUrl();
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                // 旧的相对路径，转换为完整URL
                banner.setImageUrl(serverUrl + imageUrl);
            }
            // OSS返回的URL已经是完整的，不需要转换
        });
        return Result.ok(banners);
    }
    
    /**
     * 管理员获取所有轮播图
     * @return 所有轮播图列表
     */
    @GetMapping("/api/admin/banners")
    @ApiOperation("获取所有轮播图（管理员）")
    @PreAuthorize("hasRole('admin')")
    public Result<List<Banner>> getAllBanners() {
        List<Banner> banners = bannerService.getAllBanners();
        // 处理新旧两种URL格式
        banners.forEach(banner -> {
            String imageUrl = banner.getImageUrl();
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                // 旧的相对路径，转换为完整URL
                banner.setImageUrl(serverUrl + imageUrl);
            }
            // OSS返回的URL已经是完整的，不需要转换
        });
        System.out.println("调用轮播图成功");
        return Result.ok(banners);
    }
    
    /**
     * 管理员创建轮播图
     * @param banner 轮播图对象
     * @return 创建的轮播图
     */
    @PostMapping("/api/admin/banners")
    @ApiOperation("创建轮播图（管理员）")
    @PreAuthorize("hasRole('admin')")
    public Result<Banner> createBanner(
            @ApiParam(value = "轮播图信息", required = true)
            @RequestBody Banner banner) {
        Banner createdBanner = bannerService.createBanner(banner);
        return Result.ok(createdBanner);
    }
    
    /**
     * 管理员更新轮播图
     * @param id 轮播图ID
     * @param banner 轮播图对象
     * @return 更新后的轮播图
     */
    @PutMapping("/api/admin/banners/{id}")
    @ApiOperation("更新轮播图（管理员）")
    @PreAuthorize("hasRole('admin')")
    public Result<Banner> updateBanner(
            @ApiParam(value = "轮播图ID", required = true)
            @PathVariable Long id,
            @ApiParam(value = "轮播图信息", required = true)
            @RequestBody Banner banner) {
        Banner updatedBanner = bannerService.updateBanner(id, banner);
        return Result.ok(updatedBanner);
    }
    
    /**
     * 管理员删除轮播图
     * @param id 轮播图ID
     * @return 删除结果
     */
    @DeleteMapping("/api/admin/banners/{id}")
    @ApiOperation("删除轮播图（管理员）")
    @PreAuthorize("hasRole('admin')")
    public Result deleteBanner(
            @ApiParam(value = "轮播图ID", required = true)
            @PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.ok("删除成功");
    }
    
    /**
     * 上传轮播图图片
     * @param file 图片文件
     * @return 图片访问URL
     */
    @PostMapping("/api/admin/banners/upload")
    @ApiOperation("上传轮播图图片（管理员）")
    @PreAuthorize("hasRole('admin')")
    public Result<?> uploadBannerImage(
            @ApiParam(value = "轮播图图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = ossFileUtil.uploadFile(file, "banner", Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp"));
            return Result.ok(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }
}