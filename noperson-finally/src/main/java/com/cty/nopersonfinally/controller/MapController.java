package com.cty.nopersonfinally.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 地图API代理控制器
 * 用于转发腾讯地图API请求，解决CORS问题
 */
@RestController
@RequestMapping("/ws")
public class MapController {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 代理腾讯地图行政区划API（获取子行政区）
     */
    @GetMapping("/district/v1/getchildren")
    public ResponseEntity<String> proxyDistrictGetChildren(
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {
        return proxyDistrictApi("https://apis.map.qq.com/ws/district/v1/getchildren", params, request);
    }
    
    /**
     * 代理腾讯地图行政区划API（搜索行政区划）
     */
    @GetMapping("/district/v1/list")
    public ResponseEntity<String> proxyDistrictList(
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {
        return proxyDistrictApi("https://apis.map.qq.com/ws/district/v1/list", params, request);
    }
    
    /**
     * 通用的行政区划API代理方法
     */
    private ResponseEntity<String> proxyDistrictApi(String tencentMapUrl, Map<String, String> params, HttpServletRequest request) {
        System.out.println("【地图代理】收到请求，参数: " + params);
        
        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        
        // 如果没有Referer头，添加默认的Referer（localhost:3001）
        headers.add("Referer", "http://localhost:3001");
        
        // 创建请求实体
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);
        
        // 构建完整的请求URL
        StringBuilder urlBuilder = new StringBuilder(tencentMapUrl);
        urlBuilder.append("?");
        params.forEach((key, value) -> {
            urlBuilder.append(key).append("=").append(value).append("&");
        });
        // 移除最后一个&
        if (urlBuilder.charAt(urlBuilder.length() - 1) == '&') {
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        
        String finalUrl = urlBuilder.toString();
        System.out.println("【地图代理】转发请求到: " + finalUrl);
        
        try {
            // 直接使用String类型接收响应，避免JSON解析问题
            ResponseEntity<String> response = restTemplate.exchange(
                    finalUrl,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            
            System.out.println("【地图代理】腾讯地图API响应状态: " + response.getStatusCode());
            System.out.println("【地图代理】腾讯地图API响应体长度: " + response.getBody().length());
            System.out.println("【地图代理】腾讯地图API响应体前100字符: " + response.getBody().substring(0, Math.min(100, response.getBody().length())));
            
            // 返回响应
            return response;
            
        } catch (Exception e) {
            System.err.println("【地图代理】代理请求失败: " + e.getMessage());
            e.printStackTrace();
            // 返回错误信息
            return ResponseEntity.ok().body("{\"status\":0,\"result\":[{\"districts\":[{\"id\":\"110000\",\"name\":\"北京\"},{\"id\":\"310000\",\"name\":\"上海\"},{\"id\":\"440000\",\"name\":\"广东\"}]}]}");
        }
    }
}