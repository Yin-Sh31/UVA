package com.cty.nopersonfinally.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 本地文件上传工具类
 */
@Component
@Data
public class LocalFileUtil {

    // 服务器基础URL
    @Value("${server.url}")
    private String serverUrl;

    // 文件存储根目录 - 使用项目内部目录，避免权限问题
    private static final String BASE_UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "upload"; // 项目根目录下的upload文件夹

    // 支持的图片类型
    private static final List<String> IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/bmp");

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

        // 4. 确保存储目录存在
        String uploadPath = BASE_UPLOAD_PATH + File.separator + folder;
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new BusinessException("创建上传目录失败");
            }
        }

        // 5. 生成新文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = StrUtil.isNotEmpty(originalFilename) && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = UUID.randomUUID() + suffix;

        // 6. 保存文件
        File destFile = new File(uploadPath + File.separator + fileName);
        try {
            file.transferTo(destFile);
            // 返回完整的URL，前端可以直接使用
            String relativePath = "/avatar/" + folder + "/" + fileName;
            return serverUrl + relativePath;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param filePath 文件路径（可以是相对路径或完整URL）
     */
    public void deleteFile(String filePath) {
        if (StrUtil.isEmpty(filePath)) {
            return;
        }

        try {
            // 如果是完整URL，提取相对路径
            String relativePath = filePath;
            if (filePath.startsWith(serverUrl)) {
                relativePath = filePath.substring(serverUrl.length());
            }
            
            // 转换为本地文件路径
            String localPath = BASE_UPLOAD_PATH + File.separator + relativePath.replace("/avatar/", "");
            File file = new File(localPath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    throw new BusinessException("文件删除失败");
                }
            }
        } catch (Exception e) {
            throw new BusinessException("文件删除失败：" + e.getMessage());
        }
    }
}