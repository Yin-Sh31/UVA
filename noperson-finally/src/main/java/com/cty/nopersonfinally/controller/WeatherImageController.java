package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.service.WeatherImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 天气图片控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/weather")
public class WeatherImageController {

    @Autowired
    private WeatherImageService weatherImageService;

    /**
     * 获取雷达图URL
     */
    @GetMapping("/radar")
    @PreAuthorize("permitAll()")
    public Result<?> getRadarImageUrl() {
        String url = weatherImageService.getRadarImageUrl();
        if (url == null) {
            log.warn("雷达图URL为空，触发手动爬取");
            try {
                weatherImageService.crawlWeatherImages();
                url = weatherImageService.getRadarImageUrl();
            } catch (Exception e) {
                log.error("手动爬取雷达图失败", e);
                return Result.error("获取雷达图失败");
            }
        }
        return new Result<>(200, "操作成功", url);
    }

    /**
     * 获取台风路径URL
     */
    @GetMapping("/typhoon")
    @PreAuthorize("permitAll()")
    public Result<?> getTyphoonImageUrl() {
        String url = weatherImageService.getTyphoonImageUrl();
        if (url == null) {
            log.warn("台风路径URL为空，触发手动爬取");
            try {
                weatherImageService.crawlWeatherImages();
                url = weatherImageService.getTyphoonImageUrl();
            } catch (Exception e) {
                log.error("手动爬取台风路径失败", e);
                return Result.error("获取台风路径失败");
            }
        }
        return new Result<>(200, "操作成功", url);
    }
}
