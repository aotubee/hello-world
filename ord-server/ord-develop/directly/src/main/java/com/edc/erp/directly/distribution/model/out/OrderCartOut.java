package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.dirordercart.entity.OrdDirOrderCart;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 购物车信息出参
 * @since 2022/10/17 19:06
 */
@Data
public class OrderCartOut extends OrdDirOrderCart implements Serializable {
    private static final long serialVersionUID = -636699723102441819L;

    @ApiModelProperty(name = "originalPrice", value = "商品配送价（原价）")
    private BigDecimal originalPrice;

    @ApiModelProperty(name = "suggestedRetailPrice", value = "建议零售价")
    private BigDecimal suggestedRetailPrice;
}
