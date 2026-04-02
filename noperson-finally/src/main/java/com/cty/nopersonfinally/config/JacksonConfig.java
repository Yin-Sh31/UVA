package com.cty.nopersonfinally.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 配置Java 8时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 序列化格式：yyyy-MM-dd HH:mm:ss
        DateTimeFormatter serializeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(serializeFormatter));
        
        // 反序列化格式：支持多种格式，包括JavaScript Date格式
        DateTimeFormatter deserializeFormatter = new DateTimeFormatterBuilder()
                // ISO格式
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                // JavaScript Date格式: "Wed Mar 04 2026 00:00:00 GMT+0800 (中国标准时间)"
                .appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)"))
                .appendOptional(DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .toFormatter();
        
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(deserializeFormatter));
        
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知字段（避免因前端传入多余字段导致反序列化失败）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 允许序列化空对象（避免因对象为空导致序列化失败）
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return objectMapper;

    }
}
