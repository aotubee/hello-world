package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 直营订货单汇总信息出参
 *
 * @author wanglidong
 * @since 2022/11/24 15:19
 */
@Data
@ApiModel(value = "OrderSummaryOut", description = "直营订货单汇总信息出参")
public class OrderSummaryOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单应付金额汇总
     */
    @ApiModelProperty(name = "payableTotalAmount", value = "订单应付金额")
    private BigDecimal payableTotalAmount;

    /**
     * 订单优惠金额汇总
     */
    @ApiModelProperty(name = "preferentialTotalAmount", value = "订单优惠金额汇总")
    private BigDecimal preferentialTotalAmount;
}