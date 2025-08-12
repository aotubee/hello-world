package com.edc.erp.directly.distribution.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-24 15:15
 */
@Data
public class UpdateDirOrderGoodsIn extends BaseEntity {

    @ApiModelProperty(name = "orderDetailId", value = "订单明细id")
    private Long orderDetailId;

    @ApiModelProperty(name = "packageQuantity", value = "包装数量")
    private BigDecimal packageQuantity;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

}
