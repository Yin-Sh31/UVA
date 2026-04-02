package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "评价提交DTO")
public class EvaluationDTO {

    @Schema(description = "订单类型（INSPECTION-巡检 SPRAY-喷洒）", example = "INSPECTION")
    @NotBlank(message = "订单类型不能为空")
    private String orderType;

    @Schema(description = "订单ID", example = "1001")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "评分（1-5星）", example = "5")
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer score;

    @Schema(description = "评价内容", example = "飞手操作专业，效率高")
    private String content;


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

    public @NotNull(message = "评分不能为空") @Min(value = 1, message = "评分最低1星") @Max(value = 5, message = "评分最高5星") Integer getScore() {
        return score;
    }

    public void setScore(@NotNull(message = "评分不能为空") @Min(value = 1, message = "评分最低1星") @Max(value = 5, message = "评分最高5星") Integer score) {
        this.score = score;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}