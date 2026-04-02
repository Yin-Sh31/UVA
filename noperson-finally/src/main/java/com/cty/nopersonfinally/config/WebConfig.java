package com.cty.nopersonfinally.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.List;

@Configuration
// 移除 @EnableWebMvc，避免覆盖默认配置

public class WebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            System.out.println("加载的转换器：" + converter.getClass().getSimpleName());
        }
        // 移除可能存在的 StringHttpMessageConverter 干扰
        converters.removeIf(converter -> converter instanceof StringHttpMessageConverter);

        // 配置 Jackson 转换器
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 解决日期序列化问题（如果 Result 中有 LocalDateTime 等类型）
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        jacksonConverter.setObjectMapper(objectMapper);

        // 将 jackson 转换器放到最前面，确保优先使用
        converters.add(0, jacksonConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 保留本地静态资源映射，支持旧的相对路径访问
        // 新上传的图片使用OSS，返回完整URL，不需要本地映射
        String userDir = System.getProperty("user.dir").replace("\\", "/");
        String avatarBasePath = "file:" + userDir + "/upload/";
        registry.addResourceHandler("/avatar/**", "/api/avatar/**")
                .addResourceLocations(avatarBasePath)
                .setCachePeriod(3600); // 设置缓存时间为1小时
        
        // 配置轮播图图片访问路径，确保轮播图图片能够正确访问
        // 映射路径 /images/** 到本地目录，用于小程序访问
        String bannerImagePath = "file:" + userDir + "/upload/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations(bannerImagePath)
                .setCachePeriod(0); // 开发环境下不缓存，方便调试
        
        // 保留/api/images/**映射以确保向后兼容
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations(bannerImagePath)
                .setCachePeriod(0);
    }
}