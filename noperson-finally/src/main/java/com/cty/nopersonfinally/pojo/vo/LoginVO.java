package com.cty.nopersonfinally.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应结果VO
 */
@Data
@Schema(description = "登录响应结果")
@NoArgsConstructor
public class LoginVO {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String realName;

    @Schema(description = "用户角色（FARMER-农户，FLYER-飞手，OWNER-机主）", example = "FLYER")
    private String role;

    @Schema(description = "角色名称", example = "飞手")
    private String roleName;

    @Schema(description = "JWT令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "审核状态（0-待审核，1-通过，2-拒绝），仅飞手/机主有此状态", example = "1")
    private Integer auditStatus;

    @Schema(description = "审核状态描述", example = "已通过")
    private String auditStatusDesc;


    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditStatusDesc() {
        return auditStatusDesc;
    }

    public void setAuditStatusDesc(String auditStatusDesc) {
        this.auditStatusDesc = auditStatusDesc;
    }
}
