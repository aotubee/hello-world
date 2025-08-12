package com.edc.erp.disordercart.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-13 14:51
 */
@Data
public class DisOperationShoppingCartIn extends BaseEntity {

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

    @ApiModelProperty(name = "optionalGiftCodeList", value = "选择赠送商品代码集合")
    private List<String> optionalGiftCodeList;
}
