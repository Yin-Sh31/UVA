package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.utils.OssFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备图片上传控制器
 */
@RestController
@RequestMapping("/device/image")
@Api(tags = "设备图片管理")
public class DeviceImageController {

    @Autowired
    private OssFileUtil ossFileUtil;

    /**
     * 上传设备图片
     */
    @PostMapping("/upload")
    @ApiOperation("上传设备图片")
    public ResponseEntity<?> uploadDeviceImage(
            @ApiParam(value = "设备图片文件", required = true) @RequestParam("file") MultipartFile file) {

        try {
            // 上传图片到device文件夹
            String imageUrl = ossFileUtil.uploadImage(file, "device");

            // 返回成功响应
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "图片上传成功");
            result.put("data", imageUrl);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 处理异常
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("message", "图片上传失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 删除设备图片
     */
    @DeleteMapping("/delete")
    @ApiOperation("删除设备图片")
    public ResponseEntity<?> deleteDeviceImage(
            @ApiParam(value = "图片路径", required = true) @RequestParam("filePath") String filePath) {

        try {
            ossFileUtil.deleteFile(filePath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "图片删除成功");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("code", 500);
            errorResult.put("message", "图片删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }
}