package com.cty.nopersonfinally.controller;

import com.cty.nopersonfinally.pojo.dto.Result;
import com.cty.nopersonfinally.pojo.entity.AiDiagnosisHistory;
import com.cty.nopersonfinally.service.AIService;
import com.cty.nopersonfinally.service.AiDiagnosisHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI诊断控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;
    
    @Autowired
    private AiDiagnosisHistoryService aiDiagnosisHistoryService;

    /**
     * 病虫害诊断
     */
    @PostMapping("/diagnose")
    public Result diagnose(@RequestBody DiagnoseRequest request) {
        try {
            DiagnoseResult result = aiService.diagnose(request.getImageUrl(), request.getCropType());
            
            // 保存诊断历史记录
            if (request.getUserId() != null) {
                AiDiagnosisHistory history = new AiDiagnosisHistory();
                history.setUserId(request.getUserId());
                history.setCropType(request.getCropType());
                history.setImageUrl(request.getImageUrl());
                history.setDiseaseName(result.getDiseaseName());
                history.setConfidence(result.getConfidence());
                history.setRecommendedPesticide(result.getRecommendedPesticide());
                history.setControlMeasures(result.getControlMeasures());
                aiDiagnosisHistoryService.saveDiagnosisHistory(history);
            }
            
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("诊断失败：" + e.getMessage());
        }
    }

    /**
     * 诊断请求参数
     */
    public static class DiagnoseRequest {
        private String imageUrl;
        private String cropType;
        private Long userId;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCropType() {
            return cropType;
        }

        public void setCropType(String cropType) {
            this.cropType = cropType;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    /**
     * 诊断结果
     */
    public static class DiagnoseResult {
        private String diseaseName;
        private Integer confidence;
        private String recommendedPesticide;
        private String controlMeasures;

        public DiagnoseResult() {
        }

        public DiagnoseResult(String diseaseName, Integer confidence, String recommendedPesticide, String controlMeasures) {
            this.diseaseName = diseaseName;
            this.confidence = confidence;
            this.recommendedPesticide = recommendedPesticide;
            this.controlMeasures = controlMeasures;
        }

        public String getDiseaseName() {
            return diseaseName;
        }

        public void setDiseaseName(String diseaseName) {
            this.diseaseName = diseaseName;
        }

        public Integer getConfidence() {
            return confidence;
        }

        public void setConfidence(Integer confidence) {
            this.confidence = confidence;
        }

        public String getRecommendedPesticide() {
            return recommendedPesticide;
        }

        public void setRecommendedPesticide(String recommendedPesticide) {
            this.recommendedPesticide = recommendedPesticide;
        }

        public String getControlMeasures() {
            return controlMeasures;
        }

        public void setControlMeasures(String controlMeasures) {
            this.controlMeasures = controlMeasures;
        }
    }
}