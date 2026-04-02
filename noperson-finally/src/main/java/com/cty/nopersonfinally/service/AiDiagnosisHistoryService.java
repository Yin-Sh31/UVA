package com.cty.nopersonfinally.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cty.nopersonfinally.pojo.entity.AiDiagnosisHistory;

import java.util.List;

public interface AiDiagnosisHistoryService extends IService<AiDiagnosisHistory> {
    
    /**
     * 保存诊断记录
     * @param history 诊断历史记录
     * @return 是否保存成功
     */
    boolean saveDiagnosisHistory(AiDiagnosisHistory history);
    
    /**
     * 根据用户ID查询诊断历史记录
     * @param userId 用户ID
     * @return 诊断历史记录列表
     */
    List<AiDiagnosisHistory> getDiagnosisHistoryByUserId(Long userId);
    
    /**
     * 根据用户ID分页查询诊断历史记录
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    IPage<AiDiagnosisHistory> getDiagnosisHistoryByUserId(Long userId, Integer pageNum, Integer pageSize);
}
