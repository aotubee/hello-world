package com.edc.erp.directly.dirrequestorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 要货单明细汇总出参
 * @author wei
 */
@Data
public class RequestSummarizingOut {
    /** 总要货金额 */
    @ApiModelProperty(name = "totalRequestAmount", value = "总要货金额")
    private BigDecimal totalRequestAmount;

    /** 总要货数量 */
    @ApiModelProperty(name = "totalRequestQuantity", value = "总要货数量")
    private BigDecimal totalRequestQuantity;

}
