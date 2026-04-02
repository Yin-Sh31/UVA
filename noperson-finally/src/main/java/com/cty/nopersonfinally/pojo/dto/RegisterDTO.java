package com.cty.nopersonfinally.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求参数DTO
 */
@Data
@Schema(description = "注册请求参数")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&*])[a-zA-Z0-9@#$%^&*]{8,20}$",
            message = "密码需包含数字、字母和特殊字符，长度8-20位")
    @Schema(description = "登录密码", example = "Zhang@123")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;

    @NotBlank(message = "角色类型不能为空")
    @Pattern(regexp = "^(farmer|flyer|owner)$", message = "角色类型必须是farmer、flyer或owner")
    @Schema(description = "角色类型（farmer-农户，flyer-飞手，owner-机主）", example = "flyer")
    private String role;


    public @NotBlank(message = "用户名不能为空") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "用户名不能为空") String username) {
        this.username = username;
    }

    public @NotBlank(message = "密码不能为空") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&*])[a-zA-Z0-9@#$%^&*]{8,20}$",
            message = "密码需包含数字、字母和特殊字符，长度8-20位") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "密码不能为空") @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&*])[a-zA-Z0-9@#$%^&*]{8,20}$",
            message = "密码需包含数字、字母和特殊字符，长度8-20位") String password) {
        this.password = password;
    }

    public @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String getPhone() {
        return phone;
    }

    public void setPhone(@NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
        this.phone = phone;
    }

    public @NotBlank(message = "真实姓名不能为空") String getRealName() {
        return realName;
    }

    public void setRealName(@NotBlank(message = "真实姓名不能为空") String realName) {
        this.realName = realName;
    }

    public @NotBlank(message = "角色类型不能为空") @Pattern(regexp = "^(farmer|flyer|owner)$", message = "角色类型必须是FARMER、FLYER或OWNER") String getRole() {
        return role;
    }

    public void setRole(@NotBlank(message = "角色类型不能为空") @Pattern(regexp = "^(farmer|flyer|owner)$", message = "角色类型必须是FARMER、FLYER或OWNER") String role) {
        this.role = role;
    }
}