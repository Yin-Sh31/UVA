package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.UserInfoUpdateDTO;
import com.cty.nopersonfinally.pojo.vo.UserInfoVO;
import com.cty.nopersonfinally.service.UserInfoService;
import com.cty.nopersonfinally.utils.JWTUtil;
import com.cty.nopersonfinally.utils.OssFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 头像上传控制器
 */
@RestController
@RequestMapping({"/file", "/api/file"})
@Api(tags = "头像管理")
public class AvatarController {

    @Autowired
    private OssFileUtil ossFileUtil;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private JWTUtil jwtUtil;

    /**
     * 上传用户头像
     * 支持农户、飞手、机主角色
     */
    @PostMapping("/upload")
    @ApiOperation("上传用户头像")
    public ResponseEntity<?> uploadAvatar(
            @ApiParam(value = "头像文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "JWT令牌", required = true) @RequestHeader("Authorization") String token) {

        try {
            // 1. 解析用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("未授权访问");
            }

            // 2. 查询用户信息
            UserInfoVO userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null) {
                return ResponseEntity.status(404).body("用户不存在");
            }

            // 3. 检查用户角色（支持农户、飞手、机主）
            Integer roleType = userInfo.getRoleType();
            if (roleType == null || (roleType != 1 && roleType != 2 && roleType != 3)) {
                return ResponseEntity.status(403).body("不支持的用户角色");
            }

            // 4. 删除旧头像文件（如果存在）
            if (userInfo.getAvatar() != null && !userInfo.getAvatar().isEmpty()) {
                try {
                    ossFileUtil.deleteFile(userInfo.getAvatar());
                } catch (Exception e) {
                    // 旧文件删除失败不影响新文件上传
                    System.err.println("删除旧头像文件失败: " + e.getMessage());
                }
            }

            // 5. 上传新头像文件
            String avatarUrl = ossFileUtil.uploadImage(file, "user/" + userId);

            // 6. 更新用户头像信息
            UserInfoUpdateDTO updateDTO = new UserInfoUpdateDTO();
            updateDTO.setAvatar(avatarUrl);
            boolean success = userInfoService.updateUserInfo(updateDTO, userId);
            
            if (!success) {
                return ResponseEntity.status(500).body("头像更新失败");
            }

            // 7. 返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "头像上传成功");
            result.put("data", avatarUrl);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 处理异常
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("message", "头像上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 代理访问阿里云OSS图片
     * 前端通过这个接口访问OSS图片，避免跨域问题
     */
    @GetMapping("/proxy/{userId}/avatar")
    @ApiOperation("代理访问用户头像")
    public ResponseEntity<byte[]> proxyAvatar(
            @ApiParam(value = "用户ID", required = true) @PathVariable("userId") Long userId) {
        try {
            // 查询用户信息获取头像URL
            UserInfoVO userInfo = userInfoService.getUserInfo(userId);
            if (userInfo == null || userInfo.getAvatar() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            String avatarUrl = userInfo.getAvatar();
            // 从OSS URL中提取文件名（去掉域名部分）
            String objectName = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            String folder = "user/" + userId;
            
            // 使用OSS客户端获取文件流
            com.aliyun.oss.OSS ossClient = null;
            try {
                ossClient = new com.aliyun.oss.OSSClientBuilder().build(
                        ossFileUtil.getEndpoint(),
                        ossFileUtil.getAccessKeyId(),
                        ossFileUtil.getAccessKeySecret()
                );
                
                String fullObjectName = folder + "/" + objectName;
                InputStream inputStream = ossClient.getObject(ossFileUtil.getBucketName(), fullObjectName).getObjectContent();
                
                // 读取文件内容
                byte[] bytes = inputStream.readAllBytes();
                
                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);
                headers.setContentLength(bytes.length);
                
                return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}