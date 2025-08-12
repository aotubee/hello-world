package com.edc.erp.directly.dirordercart.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author fxw
 * @description: 清空购物车入参
 * @since 2022/10/17 17:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteOrderCartIn {

    /**
     * 商品sku
     */
    @ApiModelProperty(name = "goodsCode", value = "商品code",required = true)
    private String goodsCode;

    /**
     * 活动主键
     */
    @ApiModelProperty(name = "activityId", value = "活动主键")
    private Integer activityId;
}
