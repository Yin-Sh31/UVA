package com.cty.nopersonfinally.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.AiDiagnosisHistory;
import com.cty.nopersonfinally.service.AiDiagnosisHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/history")
public class AiDiagnosisHistoryController {
    
    @Autowired
    private AiDiagnosisHistoryService aiDiagnosisHistoryService;
    
    /**
     * 保存诊断记录
     */
    @PostMapping("/save")
    public Result saveDiagnosisHistory(@RequestBody AiDiagnosisHistory history) {
        boolean result = aiDiagnosisHistoryService.saveDiagnosisHistory(history);
        if (result) {
            return Result.ok("保存诊断记录成功");
        } else {
            return Result.error("保存诊断记录失败");
        }
    }
    
    /**
     * 根据用户ID查询诊断历史记录
     */
    @GetMapping("/user/{userId}")
    public Result<List<AiDiagnosisHistory>> getDiagnosisHistoryByUserId(@PathVariable Long userId) {
        List<AiDiagnosisHistory> historyList = aiDiagnosisHistoryService.getDiagnosisHistoryByUserId(userId);
        return Result.ok(historyList);
    }
    
    /**
     * 根据用户ID分页查询诊断历史记录
     */
    @GetMapping("/user/{userId}/page")
    public Result<IPage<AiDiagnosisHistory>> getDiagnosisHistoryByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<AiDiagnosisHistory> pageResult = aiDiagnosisHistoryService.getDiagnosisHistoryByUserId(userId, pageNum, pageSize);
        return Result.ok(pageResult);
    }
}
