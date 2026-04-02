package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求参数DTO
 */

@Schema(description = "登录请求参数")
@Data
public class LoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号（非管理员登录时必填）", example = "13800138000")
    private String phone;
    
    @Schema(description = "用户名（管理员登录时必填）", example = "admin")
    private String username;

    @Schema(description = "登录密码（密码登录时必填）", example = "123456")
    private String password;

    @Schema(description = "验证码（验证码登录时必填）", example = "123456")
    private String code;

    @NotBlank(message = "登录方式不能为空")
    @Schema(description = "登录方式（password-密码登录，code-验证码登录）", example = "password")
    private String loginType;

    @NotBlank(message = "角色不能为空")
    @Schema(description = "用户角色（admin/farmer/flyer/owner）", example = "farmer")
    private String role;

    public @NotBlank(message = "手机号不能为空") String getPhone() {
        return phone;
    }

    public void setPhone(@NotBlank(message = "手机号不能为空") String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public @NotBlank(message = "登录方式不能为空") String getLoginType() {
        return loginType;
    }
    
    public void setLoginType(@NotBlank(message = "登录方式不能为空") String loginType) {
        this.loginType = loginType;
    }
    
    public @NotBlank(message = "角色不能为空") String getRole() {
        return role;
    }
    
    public void setRole(@NotBlank(message = "角色不能为空") String role) {
        this.role = role;
    }
}
