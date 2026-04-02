package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/jwt")
public class JwtController {

    /**
     * 获取ED25519 JWT Token
     * @return JWT Token
     */
    @GetMapping("/token")
    public Result<?> getJwtToken() {
        try {
            log.info("开始生成ED25519 JWT Token");
            String token = JwtUtils.generateEd25519Token();
            log.info("ED25519 JWT Token生成成功");
            return new Result<>(200, token, null);
        } catch (Exception e) {
            log.error("生成ED25519 JWT Token失败", e);
            return Result.error("生成Token失败: " + e.getMessage());
        }
    }

    /**
     * 生成新的ED25519密钥对（仅供测试使用）
     * @return 密钥对
     */
    @GetMapping("/generate-key-pair")
    public Result<?> generateKeyPair() {
        try {
            log.info("开始生成ED25519密钥对");
            Map<String, String> keyPair = JwtUtils.generateEd25519KeyPair();
            log.info("ED25519密钥对生成成功");
            return Result.ok(keyPair);
        } catch (NoSuchAlgorithmException e) {
            log.error("生成ED25519密钥对失败", e);
            return Result.error("生成密钥对失败: " + e.getMessage());
        }
    }
}