package com.cty.nopersonfinally.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 天气图片服务
 */
@Slf4j
@Service
public class WeatherImageService {

    // 雷达图URL缓存
    private String radarImageUrl;
    // 台风路径URL缓存
    private String typhoonImageUrl;

    /**
     * 每小时爬取一次天气图片链接
     */
    @Scheduled(cron = "0 0 * * * *") // 每小时执行一次
    public void crawlWeatherImages() {
        log.info("更新天气网站链接...");
        
        try {
            // 直接设置雷达图网站链接
            radarImageUrl = "https://www.weather.com.cn/radar/index.shtml";
            log.info("雷达图网站链接: {}", radarImageUrl);
            
            // 直接设置台风路径网站链接
            typhoonImageUrl = "https://typhoon.weather.com.cn/typhoon_new/typhoon_new.shtml";
            log.info("台风路径网站链接: {}", typhoonImageUrl);
            
            log.info("天气网站链接更新完成");
        } catch (Exception e) {
            log.error("更新天气网站链接失败", e);
        }
    }



    /**
     * 获取雷达图URL
     */
    public String getRadarImageUrl() {
        return radarImageUrl;
    }

    /**
     * 获取台风路径URL
     */
    public String getTyphoonImageUrl() {
        return typhoonImageUrl;
    }
    
    /**
     * 初始化时立即爬取一次
     */
    @PostConstruct
    public void init() {
        log.info("初始化天气图片服务，立即爬取一次图片");
        crawlWeatherImages();
    }
}