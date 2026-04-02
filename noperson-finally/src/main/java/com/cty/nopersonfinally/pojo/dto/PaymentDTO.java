package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "支付订单创建DTO")
public class PaymentDTO {

    @Schema(description = "订单类型（INSPECTION-巡检 SPRAY-喷洒）", example = "INSPECTION")
    @NotBlank(message = "订单类型不能为空")
    private String orderType;

    @Schema(description = "订单/需求ID", example = "1001")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "支付方式（WECHAT-微信 ALIPAY-支付宝）", example = "WECHAT")
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;


    public @NotBlank(message = "订单类型不能为空") String getOrderType() {
        return orderType;
    }

    public void setOrderType(@NotBlank(message = "订单类型不能为空") String orderType) {
        this.orderType = orderType;
    }

    public @NotNull(message = "订单ID不能为空") Long getOrderId() {
        return orderId;
    }

    public void setOrderId(@NotNull(message = "订单ID不能为空") Long orderId) {
        this.orderId = orderId;
    }

    public @NotBlank(message = "支付方式不能为空") String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(@NotBlank(message = "支付方式不能为空") String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}