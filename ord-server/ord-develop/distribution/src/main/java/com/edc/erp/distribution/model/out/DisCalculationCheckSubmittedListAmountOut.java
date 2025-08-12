package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提交订货清单后勾选指定待支付清单核算金额出参对象
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-06-12 14:41
 */
@Data
public class DisCalculationCheckSubmittedListAmountOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订货金额
     */
    @ApiModelProperty(name = "orderAmount", value = "订货金额")
    private BigDecimal orderAmount;

    /**
     * 海鼎支付前可用账户余额
     */
    private BigDecimal accountBalanceTotalAmount;

    /**
     * 需要现金支付金额
     */
    private BigDecimal cashPaymentAmount;
}
