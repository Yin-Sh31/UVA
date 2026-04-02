package com.cty.nopersonfinally.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;




/**
 * 余额更新DTO
 * 用于接收用户余额更新的请求参数
 */
@Data
public class BalanceUpdateDTO {

    @Schema(description = "金额（正数增加，负数减少）")
    @NotNull(message = "金额不能为空")
    private Double amount;

    public @NotNull(message = "金额不能为空") Double getAmount() {
        return amount;
    }

    public void setAmount(@NotNull(message = "金额不能为空") Double amount) {
        this.amount = amount;
    }

}