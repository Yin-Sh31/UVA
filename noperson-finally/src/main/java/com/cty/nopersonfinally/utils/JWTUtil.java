package com.cty.nopersonfinally.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类（静态方法改造）
 */
@Component
public class JWTUtil {

    @Autowired
    private Environment env;

    // 静态变量存储配置值
    private static String staticSecret;
    private static Long staticExpire;

    // 初始化时将配置值赋值给静态变量
    @PostConstruct
    public void init() {
        staticSecret = env.getProperty("jwt.secret", "defaultSecretKey12345678901234567890");
        String expireStr = env.getProperty("jwt.expire", "86400000");
        staticExpire = Long.parseLong(expireStr);
        System.out.println("[JWTUtil] 初始化完成，secret长度: " + staticSecret.length() + ", expire: " + staticExpire);
    }

    /**
     * 生成JWT令牌（静态方法）
     */
    public static String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + staticExpire);

        return Jwts.builder()
                .setSubject(userId.toString()) // 存储用户ID
                .claim("role", role) // 存储角色
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从令牌中获取用户ID（静态方法）
     */
    public static Long getUserIdFromToken(String token) {
        System.out.println("[JWTUtil] 开始解析token获取userId，token是否为空: " + (token == null ? "是" : "否"));
        if (token == null || token.isEmpty()) {
            System.out.println("[JWTUtil] 传入的token为空");
            return null;
        }
        try {
            // 预处理：去除可能存在的"Bearer "前缀和空格
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
                System.out.println("[JWTUtil] 已去除Bearer前缀，处理后的token: " + token);
            }
            // 去除token中可能存在的所有空格
            token = token.replaceAll("\\s+", "");
            
            // 正常用户token处理
            Claims claims = parseToken(token);
            String subject = claims.getSubject();
            System.out.println("[JWTUtil] 成功解析token，subject: " + subject);
            return Long.valueOf(subject);
        } catch (Exception e) {
            System.out.println("[JWTUtil] 解析token失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从令牌中获取角色（静态方法）
     */
    public static String getRoleFromToken(String token) {
        System.out.println("[JWTUtil] 开始解析token获取角色，token是否为空: " + (token == null ? "是" : "否"));
        if (token == null || token.isEmpty()) {
            System.out.println("[JWTUtil] 传入的token为空");
            return null;
        }
        try {
            // 预处理：去除可能存在的"Bearer "前缀和空格
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }
            token = token.replaceAll("\\s+", "");
            
            // 正常用户token处理
            Claims claims = parseToken(token);
            String role = claims.get("role", String.class);
            System.out.println("[JWTUtil] 成功解析token，提取的角色: " + role);
            return role;
        } catch (Exception e) {
            System.out.println("[JWTUtil] 解析token获取角色失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证令牌是否有效（静态方法）
     */
    public static boolean isTokenValid(String token) {
        try {
            // 预处理：去除可能存在的"Bearer "前缀和空格
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }
            token = token.replaceAll("\\s+", "");
            
            // 正常用户token处理
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证令牌是否有效且包含指定的用户ID（静态方法）
     */
    public static boolean isTokenValid(String token, Long userId) {
        System.out.println("[JWTUtil] 开始验证token有效性和用户ID匹配，userId: " + userId);
        try {
            // 预处理：去除可能存在的"Bearer "前缀和空格
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }
            token = token.replaceAll("\\s+", "");
            
            // 首先验证令牌基本有效性
            if (!isTokenValid(token)) {
                System.out.println("[JWTUtil] token基本有效性验证失败");
                return false;
            }
            
            // 验证令牌中的用户ID是否与传入的用户ID匹配
            Long tokenUserId = getUserIdFromToken(token);
            boolean idMatch = tokenUserId != null && tokenUserId.equals(userId);
            System.out.println("[JWTUtil] tokenUserId: " + tokenUserId + ", 用户ID匹配结果: " + idMatch);
            return idMatch;
        } catch (Exception e) {
            System.out.println("[JWTUtil] 验证token有效性异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 解析令牌（静态方法）
     */
    private static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取加密密钥（静态方法）
     */
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(staticSecret.getBytes(StandardCharsets.UTF_8));
    }
}
