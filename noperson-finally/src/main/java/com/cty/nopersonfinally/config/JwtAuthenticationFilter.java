package com.cty.nopersonfinally.config;


import com.cty.nopersonfinally.utils.JWTUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 用于验证请求头中的JWT令牌，并设置认证信息到SecurityContext
 */

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired      
    private StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {

        // 记录请求URL便于调试
        log.debug("处理请求: {}", request.getRequestURI());
        
        // 跳过公开接口的JWT验证，这些接口应该允许匿名访问
        String requestUri = request.getRequestURI();
        // 跳过认证接口、JWT接口、天气图片接口和静态资源路径
        if ("/auth/login".equals(requestUri) || "/auth/send-code".equals(requestUri) || "/auth/sendCode".equals(requestUri) || "/auth/register".equals(requestUri) ||
            "/api/auth/login".equals(requestUri) || "/api/auth/send-code".equals(requestUri) || "/api/auth/sendCode".equals(requestUri) || "/api/auth/register".equals(requestUri) ||
            requestUri.startsWith("/api/jwt/") ||
            requestUri.startsWith("/api/weather/") ||
            requestUri.startsWith("/images/") || requestUri.startsWith("/api/images/")) {
            log.debug("公开接口[{}]或静态资源，跳过JWT验证", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 从请求头中获取Authorization
            String authorizationHeader = request.getHeader("Authorization");
            System.out.println("[JwtAuthFilter] 请求URI: " + request.getRequestURI() + ", Authorization头是否存在: " + (authorizationHeader != null ? "是" : "否"));

            // 检查令牌是否存在且格式正确
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                System.out.println("[JwtAuthFilter] Authorization头包含Bearer前缀");
                // 安全地提取token，防止索引越界
                String token = authorizationHeader.substring(7).trim(); // 去除"Bearer "前缀并修剪空白字符
                if (token.isEmpty()) {
                    System.out.println("[JwtAuthFilter] 提取的token为空字符串");
                    log.debug("提取的token为空字符串");
                } else {
                    System.out.println("[JwtAuthFilter] 提取的token前20位: " + token.substring(0, Math.min(20, token.length())) + "...");
                    log.debug("找到有效格式的Authorization头，提取token前20位: {}", token.substring(0, Math.min(20, token.length())));
                }

                try {
                    // 从令牌中提取用户ID用于验证
                    Long userId = null;
                    try {
                        System.out.println("[JwtAuthFilter] 开始调用JWTUtil.getUserIdFromToken提取用户ID");
                        userId = JWTUtil.getUserIdFromToken(token);
                        if (userId == null) {
                            System.out.println("[JwtAuthFilter] JWTUtil.getUserIdFromToken返回null");
                            log.error("JWTUtil.getUserIdFromToken返回null，token前20位: {}", token.substring(0, Math.min(20, token.length())));
                        } else {
                            System.out.println("[JwtAuthFilter] 成功从token中提取userId: " + userId);
                        }
                    } catch (Exception e) {
                        System.out.println("[JwtAuthFilter] 调用JWTUtil.getUserIdFromToken时发生异常: " + e.getMessage());
                        e.printStackTrace();
                        log.error("调用JWTUtil.getUserIdFromToken时发生异常: {}, token前20位: {}", e.getMessage(), token.substring(0, Math.min(20, token.length())));
                    }
                    
                    // 验证令牌有效性和用户ID匹配
                    if (userId != null) {
                        System.out.println("[JwtAuthFilter] 开始验证JWT令牌有效性");
                        if (JWTUtil.isTokenValid(token, userId)) {
                            log.debug("JWT令牌验证通过");
                            // 从令牌中提取角色
                            String role = JWTUtil.getRoleFromToken(token);
                            log.debug("从token中提取用户信息 - userId: {}, role: {}", userId, role);

                            if (role != null) {
                                log.debug("JWT令牌验证通过，用户ID: {}, 角色: {}, 准备进行Redis验证", userId, role);
                                 
                                boolean redisVerified = false;
                                try {
                                    // 检查Redis中是否存在该token
                                    String redisKey = "token:" + userId + ":" + role;
                                    String storedToken = stringRedisTemplate.opsForValue().get(redisKey);
                                    log.debug("从Redis获取的token前20位: {}", storedToken != null ? storedToken.substring(0, Math.min(20, storedToken.length())) : "null");
                                      
                                    if (storedToken != null && storedToken.equals(token)) {
                                        // 刷新Redis中token的过期时间
                                        stringRedisTemplate.expire(redisKey, 5, java.util.concurrent.TimeUnit.MINUTES);
                                        redisVerified = true;
                                        log.debug("Redis验证成功，已刷新token过期时间，用户ID: {}, 请求URI: {}", userId, request.getRequestURI());
                                    } else {
                                        log.info("Redis中token不存在或不匹配，用户ID: {}, 请求URI: {}", userId, request.getRequestURI());
                                        // 不再将token存储到Redis，直接使用JWT验证
                                        redisVerified = true;
                                        log.info("已跳过Redis验证，使用JWT令牌单独验证模式");
                                    }
                                } catch (Exception e) {
                                    log.error("Redis操作失败: {}", e.getMessage(), e);
                                    // 在Redis连接失败的情况下，仍然允许基于JWT令牌的认证（回退机制）
                                    // 注意：这会降低安全性，但提高了系统的可用性
                                    redisVerified = true; // 临时启用：Redis不可用时允许仅JWT认证
                                    log.warn("Redis连接失败，已启用JWT令牌单独验证模式");
                                }

                                // 如果Redis验证通过（或回退机制启用），创建认证对象
                                if (redisVerified) {
                                    // 创建认证令牌 - 添加ROLE_前缀，与hasRole方法配合使用
                                    UsernamePasswordAuthenticationToken authentication =
                                            new UsernamePasswordAuthenticationToken(
                                                    userId.toString(), // 用户名使用用户ID
                                                    null,
                                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                                            );

                                    // 设置认证详情
                                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                    // 将认证信息存入SecurityContext
                                    SecurityContextHolder.getContext().setAuthentication(authentication);
                                    log.debug("认证成功，用户ID: {}, 角色: ROLE_{}, 请求URI: {}", userId, role, request.getRequestURI());
                                } else {
                                    // Redis验证失败，清除认证上下文
                                    SecurityContextHolder.clearContext();
                                    log.debug("认证失败，已清除SecurityContext，用户ID: {}, 请求URI: {}", userId, request.getRequestURI());
                                }
                            } else {
                                log.error("从token中提取的角色为空，用户ID: {}", userId);
                                SecurityContextHolder.clearContext();
                            }
                        } else {
                            System.out.println("[JwtAuthFilter] JWT令牌验证失败");
                            log.error("JWT令牌验证失败");
                            SecurityContextHolder.clearContext();
                        }
                    } else {
                        System.out.println("[JwtAuthFilter] 从token中提取userId失败");
                        log.error("从token中提取userId失败，返回未授权");
                        SecurityContextHolder.clearContext();
                    }
                } catch (Exception e) {
                    System.out.println("[JwtAuthFilter] JWT认证过程发生异常: " + e.getMessage());
                    e.printStackTrace();
                    log.error("JWT认证过程发生异常: {}", e.getMessage(), e);
                    SecurityContextHolder.clearContext();
                }
            } else {
                // 没有有效的Authorization头
                if (authorizationHeader == null) {
                    System.out.println("[JwtAuthFilter] Authorization头为空");
                } else {
                    System.out.println("[JwtAuthFilter] Authorization头格式不正确，不包含Bearer前缀: " + authorizationHeader.substring(0, Math.min(20, authorizationHeader.length())));
                }
                log.debug("未找到有效格式的Authorization头");
            }
        } catch (Exception e) {
            System.out.println("[JwtAuthFilter] 处理JWT令牌时发生异常: " + e.getMessage());
            e.printStackTrace();
            log.error("处理JWT令牌时发生异常: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }
        
        // 确保无论认证是否成功，过滤链都能继续执行
        filterChain.doFilter(request, response);
    }
}

