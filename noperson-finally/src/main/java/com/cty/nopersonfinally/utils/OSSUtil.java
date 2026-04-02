package com.cty.nopersonfinally.utils;

import com.aliyun.oss.OSSClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "spring.cloud.alicloud")
@Data
public class OSSUtil {
    private String accessKey;
    private String secretKey;
    private OSSProperties oss;
    private String domain; // 自定义域名（如oss.example.com）
    
    @Data
    public static class OSSProperties {
        private String endpoint;
        private String bucketName;
        private String regionId;
    }

    // 新增：获取基础URL（用于生成处理后文件的基础路径）
    public String getBaseUrl() {
        // 优先使用自定义域名，否则使用OSS默认域名
        if (domain != null && !domain.isEmpty()) {
            return "https://" + domain + "/";
        } else {
            return "https://" + oss.getBucketName() + "." + oss.getEndpoint().replace("https://", "") + "/";
        }
    }

    // 上传文件（带类型校验）
    public String uploadFile(MultipartFile file, String folder, List<String> allowTypes) {
        // 1. 基础校验
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        // 2. 类型校验
        String contentType = file.getContentType();
        if (contentType == null || !allowTypes.contains(contentType)) {
            throw new BusinessException("文件类型不支持，允许的类型：" + allowTypes);
        }
        // 3. 大小校验（10MB限制）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        // 4. 上传逻辑
        OSSClient ossClient = null;
        try {
            ossClient = getOSSClient();
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = folder + "/" + UUID.randomUUID() + suffix;
            ossClient.putObject(oss.getBucketName(), fileName, file.getInputStream());
            return getBaseUrl() + fileName; // 使用getBaseUrl拼接完整URL
        } catch (Exception e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    // 保留原图片上传方法（兼容旧逻辑）
    public String uploadFile(MultipartFile file, String folder) {
        List<String> imageTypes = Arrays.asList("image/jpeg", "image/png");
        return uploadFile(file, folder, imageTypes);
    }

    // 删除文件
    public void deleteFile(String fileUrl) {
        OSSClient ossClient = null;
        try {
            String baseUrl = getBaseUrl();
            if (fileUrl.startsWith(baseUrl)) {
                String fileName = fileUrl.substring(baseUrl.length());
                ossClient = getOSSClient();
                if (!ossClient.doesObjectExist(oss.getBucketName(), fileName)) {
                    throw new BusinessException("文件不存在，无法删除");
                }
                ossClient.deleteObject(oss.getBucketName(), fileName);
            } else {
                throw new BusinessException("文件URL格式错误，无法删除");
            }
        } catch (Exception e) {
            throw new BusinessException("文件删除失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private OSSClient getOSSClient() {
        return new OSSClient(oss.getEndpoint(), accessKey, secretKey);
    }
}