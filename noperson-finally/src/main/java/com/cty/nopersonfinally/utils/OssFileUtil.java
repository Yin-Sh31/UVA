package com.cty.nopersonfinally.utils;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云OSS文件上传工具类
 */
@Component
@Data
public class OssFileUtil {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKeyId;

    @Value("${spring.cloud.alicloud.secret-key}")
    private String accessKeySecret;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.oss.bucket-name}")
    private String bucketName;

    @Value("${spring.cloud.alicloud.oss.region-id}")
    private String regionId;

    // 支持的图片类型
    private static final List<String> IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");

    // 最大文件大小（5MB）
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传图片文件
     * @param file 上传的文件
     * @param folder 文件夹名称
     * @return 文件访问URL
     */
    public String uploadImage(MultipartFile file, String folder) {
        return uploadFile(file, folder, IMAGE_TYPES);
    }

    /**
     * 上传文件（带类型校验）
     * @param file 上传的文件
     * @param folder 文件夹名称
     * @param allowTypes 允许的文件类型
     * @return 文件访问URL
     */
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

        // 3. 大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过5MB");
        }

        // 4. 生成新文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = StrUtil.isNotEmpty(originalFilename) && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID() + suffix;

        // 5. 构建OSS对象路径
        String objectName = folder + "/" + fileName;

        // 6. 上传文件到OSS
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            InputStream inputStream = file.getInputStream();

            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);

            // 上传文件
            ossClient.putObject(bucketName, objectName, inputStream, metadata);

            // 返回完整的URL
            return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectName;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        if (StrUtil.isEmpty(fileUrl)) {
            return;
        }

        try {
            // 从URL中提取对象名称
            String objectName = extractObjectName(fileUrl);
            if (StrUtil.isEmpty(objectName)) {
                return;
            }

            OSS ossClient = null;
            try {
                ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                ossClient.deleteObject(bucketName, objectName);
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (Exception e) {
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }

    /**
     * 从URL中提取对象名称
     * @param fileUrl 文件URL
     * @return 对象名称
     */
    private String extractObjectName(String fileUrl) {
        if (StrUtil.isEmpty(fileUrl)) {
            return null;
        }

        try {
            // URL格式: https://bucket-name.oss-cn-beijing.aliyuncs.com/folder/filename.jpg
            String prefix = "https://" + bucketName + "." + endpoint.replace("https://", "") + "/";
            if (fileUrl.startsWith(prefix)) {
                return fileUrl.substring(prefix.length());
            }

            // 兼容旧格式: /avatar/folder/filename.jpg
            if (fileUrl.startsWith("/avatar/")) {
                return fileUrl.substring("/avatar/".length());
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}