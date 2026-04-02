package com.cty.nopersonfinally.pojo.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 通用审核DTO（支持飞手/机主审核）
 */
@Data
@ApiModel(description = "审核操作参数")
public class AuditDTO {

    @ApiModelProperty(value = "审核对象ID（飞手ID或机主ID）", required = true)
    @NotNull(message = "审核对象ID不能为空")
    private Long targetId; // 原ownerId/flyerId统一为targetId

    @ApiModelProperty(value = "角色类型（2-飞手 3-机主）", required = true)
    @NotNull(message = "角色类型不能为空")
    private Integer roleType; // 新增字段：区分审核对象是飞手还是机主

    @ApiModelProperty(value = "审核结果（1-通过 2-拒绝）", required = true)
    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;

    @ApiModelProperty(value = "审核备注（拒绝时必填）")
    private String auditRemark; // 拒绝原因

    @ApiModelProperty(value = "审核人ID（管理员ID）", required = true)
    @NotNull(message = "审核人ID不能为空")
    private Long auditorId;


    public @NotNull(message = "审核对象ID不能为空") Long getTargetId() {
        return targetId;
    }

    public void setTargetId(@NotNull(message = "审核对象ID不能为空") Long targetId) {
        this.targetId = targetId;
    }

    public @NotNull(message = "角色类型不能为空") Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(@NotNull(message = "角色类型不能为空") Integer roleType) {
        this.roleType = roleType;
    }

    public @NotNull(message = "审核结果不能为空") Integer getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(@NotNull(message = "审核结果不能为空") Integer auditResult) {
        this.auditResult = auditResult;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public @NotNull(message = "审核人ID不能为空") Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(@NotNull(message = "审核人ID不能为空") Long auditorId) {
        this.auditorId = auditorId;
    }
}