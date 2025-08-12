package com.edc.erp.disrequestorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 集货单明细汇总出参
 * @author wei
 */
@Data
public class DisRequestSummarizingOut {
    /** 总集货金额 */
    @ApiModelProperty(name = "totalRequestAmount", value = "总集货金额")
    private BigDecimal totalRequestAmount;

    /** 总集货数量 */
    @ApiModelProperty(name = "totalRequestQuantity", value = "总集货数量")
    private BigDecimal totalRequestQuantity;

}
