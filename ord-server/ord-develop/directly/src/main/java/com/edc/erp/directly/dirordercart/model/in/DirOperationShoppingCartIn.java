package com.edc.erp.directly.dirordercart.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-13 14:51
 */
@Data
public class DirOperationShoppingCartIn extends BaseEntity {

    /**
     * 商品code
     */
    @ApiModelProperty(name = "goodsCode", value = "商品code", required = true)
    private String goodsCode;

    /**
     * 订货包装数量
     */
    @ApiModelProperty(name = "packageQuantity", value = "订货包装数量", required = true)
    private BigDecimal packageQuantity;
}
