package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新DTO
 * 用于用户更新个人信息的数据传输
 */
@Data
public class UserInfoUpdateDTO {

    @Schema(description = "真实姓名")
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    @Schema(description = "头像URL")
    @Size(max = 255, message = "头像URL不能超过255个字符")
    private String avatar;

    @Schema(description = "地址信息")
    @Size(max = 200, message = "地址信息不能超过200个字符")
    private String address;

    public @Size(max = 50, message = "真实姓名不能超过50个字符") String getRealName() {
        return realName;
    }

    public void setRealName(@Size(max = 50, message = "真实姓名不能超过50个字符") String realName) {
        this.realName = realName;
    }

    public @Size(max = 255, message = "头像URL不能超过255个字符") String getAvatar() {
        return avatar;
    }

    public void setAvatar(@Size(max = 255, message = "头像URL不能超过255个字符") String avatar) {
        this.avatar = avatar;
    }

    public @Size(max = 200, message = "地址信息不能超过200个字符") String getAddress() {
        return address;
    }

    public void setAddress(@Size(max = 200, message = "地址信息不能超过200个字符") String address) {
        this.address = address;
    }
}