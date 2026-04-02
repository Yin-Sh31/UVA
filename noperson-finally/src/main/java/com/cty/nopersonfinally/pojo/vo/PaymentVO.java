package com.cty.nopersonfinally.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;



@Data
@Schema(description = "支付订单VO")
public class PaymentVO {

    @Schema(description = "支付记录ID")
    private Long paymentId;

    @Schema(description = "商户订单号")
    private String outTradeNo;

    @Schema(description = "支付金额（元）")
    private Double amount;

    @Schema(description = "支付方式")
    private String paymentMethod;

    @Schema(description = "支付链接（跳转支付页面）")
    private String payUrl;
    
    @Schema(description = "支付状态")
    private String status;
    
    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getPayTime() {
        return payTime;
    }
    
    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }
}