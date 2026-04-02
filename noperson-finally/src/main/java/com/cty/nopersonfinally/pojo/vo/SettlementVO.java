package com.cty.nopersonfinally.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "结算单VO")
public class SettlementVO {

    @Schema(description = "结算单ID")
    private Long settlementId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单类型")
    private String orderType;

    @Schema(description = "订单总金额（元）")
    private Double orderAmount;

    @Schema(description = "平台手续费（元）")
    private Double platformFee;

    @Schema(description = "飞手收入（元）")
    private Double flyerIncome;

    @Schema(description = "状态（0-待结算 1-已结算）")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "结算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime settlementTime;
}