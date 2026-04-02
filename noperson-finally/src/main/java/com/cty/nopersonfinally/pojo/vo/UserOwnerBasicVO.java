package com.cty.nopersonfinally.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "机主基本信息VO（列表展示用）")
public class UserOwnerBasicVO {

    @Schema(description = "机主ID")
    private Long id;

    @Schema(description = "机主姓名")
    private String realName;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "运营执照类型（INDIVIDUAL-个人，ENTERPRISE-企业）")
    private String licenseType;

    @Schema(description = "运营执照类型描述")
    private String licenseTypeDesc;

    @Schema(description = "管理设备总数")
    private Integer deviceTotal;

    @Schema(description = "可用设备数")
    private Integer availableDeviceCount;

    @Schema(description = "信誉分")
    private Integer creditScore;

    @Schema(description = "审核状态（0-待审核 1-已通过 2-已拒绝）")
    private Integer auditStatus;

    @Schema(description = "审核状态描述")
    private String auditStatusDesc;
}