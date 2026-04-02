package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.utils.OSSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
public class FileUploadController {
    
    @Autowired
    private OSSUtil ossUtil;
    
    /**
     * 上传图片文件
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 上传到OSS，文件夹为feedback
            String fileUrl = ossUtil.uploadFile(file, "feedback");
            return Result.ok(fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }
}
