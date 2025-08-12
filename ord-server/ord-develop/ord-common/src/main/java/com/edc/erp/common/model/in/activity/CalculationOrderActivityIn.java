package com.edc.erp.common.model.in.activity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 核算订单级优惠活动商品入参
 * @since 2022/11/22 17:13
 */
@Data
public class CalculationOrderActivityIn implements Serializable {
    private static final long serialVersionUID = -1915514934776553569L;


    /**
     * 商品sku
     */
    @ApiModelProperty(name = "goodsCode", value = "商品sku")
    private String goodsCode;

    /**
     * 商品数量
     */
    @ApiModelProperty(name = "quantity", value = "商品数量")
    private BigDecimal quantity;

    /**
     * 商品支付单价
     */
    @ApiModelProperty(name = "payPrice", value = "商品支付单价")
    private BigDecimal payPrice;

    /**
     * 零售规格单位
     */
    @ApiModelProperty(name = "retailSpecificationUnit", value = "零售规格单位")
    private String retailSpecificationUnit;
}
