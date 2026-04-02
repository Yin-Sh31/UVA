package com.cty.nopersonfinally.config;


import com.cty.nopersonfinally.pojo.enums.ResultCode;
import com.cty.nopersonfinally.utils.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;
import java.util.Collections;

/**
 * Spring Security配置类
 * 配置认证授权规则、JWT过滤器等
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 启用方法级别的权限控制
public class SecurityConfig {
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 构造方法注入依赖
    public SecurityConfig(JWTUtil jwtUtil, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtUtil = jwtUtil;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF保护（前后端分离项目不需要）
                .csrf(csrf -> csrf.disable())
                // 关闭会话管理（使用JWT无状态认证）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 直接在Spring Security中配置CORS
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(Collections.singletonList("*")); // 允许所有来源
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }))
                // 配置URL访问权限
                .authorizeHttpRequests(auth -> auth
                        // 允许OPTIONS请求通过
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 允许匿名访问的接口
                        .requestMatchers("/auth/login", "/auth/register", "/auth/send-code", "/auth/sendCode").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/send-code", "/api/auth/sendCode").permitAll()
                        .requestMatchers("/api/banners").permitAll() // 允许访问轮播图接口
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // 允许访问Swagger文档
                        .requestMatchers("/avatar/**", "/images/**", "/api/images/**").permitAll() // 允许访问图片资源
                        .requestMatchers("/chat/**", "/api/chat/**").permitAll() // 允许访问聊天接口
                        .requestMatchers("/message/**", "/api/message/**").permitAll() // 允许访问消息接口
                        .requestMatchers("/ws/**", "/api/ws/**").permitAll() // 允许访问地图代理接口
                        .requestMatchers("/api/jwt/**").permitAll() // 允许访问JWT相关接口
                        .requestMatchers("/api/weather/**").permitAll() // 允许访问天气图片接口
                        .requestMatchers("/upload/**", "/api/upload/**").permitAll() // 允许访问上传接口
                        .requestMatchers("/api/ai/**").permitAll() // 允许访问AI诊断接口
                        // 临时允许访问owner相关接口，方便测试
                        .requestMatchers("/owner/**").permitAll()
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                // 配置异常处理
                .exceptionHandling(ex -> ex
                        // 未授权访问处理
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"code\":" + ResultCode.UNAUTHORIZED.getCode()
                                    + ",\"msg\":\"" + ResultCode.UNAUTHORIZED.getMsg() + "\"}");
                        })
                        // 权限不足处理
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"code\":" + ResultCode.FORBIDDEN.getCode()
                                    + ",\"msg\":\"" + ResultCode.FORBIDDEN.getMsg() + "\"}");
                        })
                )
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
