package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 配销订货单汇总信息出参
 * @since 2022/11/15 9:46
 */
@Data
@ApiModel(value = "OrderSummaryOut",description = "配销订货单汇总信息出参")
public class OrderSummaryOut implements Serializable {
    private static final long serialVersionUID = 1749548621243571985L;
    /**
     * 订单应付金额汇总
     *
     */
    @ApiModelProperty(name = "payableTotalAmount", value = "订单应付金额")
    private BigDecimal payableTotalAmount;

    /**
     * 订单优惠金额汇总
     */
    @ApiModelProperty(name = "preferentialTotalAmount",value = "订单优惠金额汇总")
    private BigDecimal preferentialTotalAmount;
}
