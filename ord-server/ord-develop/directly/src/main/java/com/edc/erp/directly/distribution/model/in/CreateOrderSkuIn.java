package com.edc.erp.directly.distribution.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 创建订单商品信息入参
 * @since 2022/10/17 17:47
 */
@Data
@ApiModel(description = "创建订单商品信息入参")
public class CreateOrderSkuIn {

    /**
     *
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode",value = "商品代码")
    private String goodsCode;


    /**
     * 订货数
     */
    @ApiModelProperty(name = "packageQuantity",value = "订货数")
    private BigDecimal packageQuantity;
}
