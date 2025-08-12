package com.edc.erp.distribution.model.out;

import com.edc.erp.disordercart.entity.OrdDisOrderCart;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author fxw
 * @description: 购物车信息出参
 * @since 2022/10/17 19:06
 */
@Data
public class OrderCartOut extends OrdDisOrderCart implements Serializable {
    private static final long serialVersionUID = -636699723102441819L;

    @ApiModelProperty(name = "originalPrice", value = "商品配送价（原价）")
    private BigDecimal originalPrice;

    @ApiModelProperty(name = "suggestedRetailPrice", value = "建议零售价")
    private BigDecimal suggestedRetailPrice;

    @ApiModelProperty(name = "optionalGiftCodeList", value = "选择赠送商品代码集合")
    private List<String> optionalGiftCodeList;
}
