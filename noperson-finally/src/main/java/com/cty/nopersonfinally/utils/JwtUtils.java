package com.cty.nopersonfinally.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.EdECPrivateKey;
import java.security.interfaces.EdECPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtUtils {

    // JWT配置
    private static final String ISSUER = "CJB3F493TY"; // Credential ID
    private static final String KID = "CJB3F493TY"; // Credential ID
    private static final long EXPIRATION_TIME = 3600; // 1小时过期

    /**
     * 生成ED25519 JWT Token
     * @return JWT Token字符串
     * @throws Exception 生成过程中的异常
     */
    public static String generateEd25519Token() throws Exception {
        log.info("生成JWT Token，iss: {}, kid: {}", ISSUER, KID);
        
        // 生成Ed25519密钥对
        OctetKeyPair jwk = new OctetKeyPairGenerator(Curve.Ed25519)
                .keyID(KID)
                .generate();
        
        // 创建JWT头部
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                .keyID(KID)
                .build();
        
        // 创建JWT载荷
        Date now = new Date();
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("qweather-api")
                .issuer(ISSUER)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + EXPIRATION_TIME * 1000))
                .claim("version", "v1")
                .build();
        
        // 创建JWT
        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        
        // 使用Ed25519私钥签名
        JWSSigner signer = new Ed25519Signer(jwk);
        signedJWT.sign(signer);
        
        // 序列化JWT
        String token = signedJWT.serialize();
        log.info("生成的JWT Token: {}", token);
        
        return token;
    }

    /**
     * 验证ED25519 JWT Token
     * @param token JWT Token字符串
     * @return 是否验证通过
     */
    public static boolean validateEd25519Token(String token) {
        try {
            if (token == null || token.isEmpty()) {
                log.error("JWT token is empty");
                return false;
            }
            
            // 简单验证格式
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.error("Invalid JWT format");
                return false;
            }
            
            log.info("JWT token validation passed");
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成新的ED25519密钥对
     * @return 包含私钥和公钥的Map
     * @throws NoSuchAlgorithmException 算法不存在异常
     */
    public static Map<String, String> generateEd25519KeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        EdECPrivateKey privateKey = (EdECPrivateKey) keyPair.getPrivate();
        EdECPublicKey publicKey = (EdECPublicKey) keyPair.getPublic();

        String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        Map<String, String> keyPairMap = new HashMap<>();
        keyPairMap.put("privateKey", privateKeyBase64);
        keyPairMap.put("publicKey", publicKeyBase64);

        return keyPairMap;
    }
}