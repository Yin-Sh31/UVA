package com.cty.nopersonfinally.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应结果VO
 */
@Data
@Schema(description = "登录响应结果")

public class LoginResponseVO {

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "用户信息")
    private UserInfo user;

    public LoginResponseVO() {
    }

    public static class UserInfo {
        @Schema(description = "用户ID", example = "1001")
        private Long id;  // 注意这里修改为id，前端期望的是id而非userId

        @Schema(description = "用户名", example = "张三")
        private String name;  // 注意这里修改为name，前端期望的是name

        @Schema(description = "用户角色（farmer-农户，flyer-飞手，owner-机主）", example = "farmer")
        private String role;  // 注意角色值改为小写，与前端保持一致

        @Schema(description = "手机号")
        private String phone;

        @Schema(description = "角色名称", example = "飞手")
        private String roleName;

        @Schema(description = "审核状态（0-待审核，1-通过，2-拒绝），仅飞手/机主有此状态", example = "1")
        private Integer auditStatus;

        @Schema(description = "审核状态描述", example = "已通过")
        private String auditStatusDesc;

        public UserInfo() {
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
}