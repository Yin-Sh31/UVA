package com.cty.nopersonfinally.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "评价记录VO")
public class EvaluationVO {

    @Schema(description = "评价ID")
    private Long evaluationId;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "评价人ID")
    private Long evaluatorId;

    @Schema(description = "评价人姓名")
    private String evaluatorName;

    @Schema(description = "评分（1-5星）")
    private Integer score;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}