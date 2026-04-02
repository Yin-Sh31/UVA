package com.cty.nopersonfinally.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI诊断历史记录实体类
 */
@Data
@TableName("ai_diagnosis_history")
public class AiDiagnosisHistory {
    
    /**
     * 诊断记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 农户用户ID
     */
    private Long userId;
    
    /**
     * 作物类型
     */
    private String cropType;
    
    /**
     * 诊断图片URL
     */
    private String imageUrl;
    
    /**
     * 病虫害名称
     */
    private String diseaseName;
    
    /**
     * 置信度（0-100）
     */
    private Integer confidence;
    
    /**
     * 推荐农药
     */
    private String recommendedPesticide;
    
    /**
     * 防治措施
     */
    private String controlMeasures;
    
    /**
     * 诊断时间
     */
    private LocalDateTime createTime;
}
