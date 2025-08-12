package com.edc.erp.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName SubmitPresaleOrderGoodsIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/9/4 17:47
 **/
@Data
public class CreatePresaleOrderGoodsIn implements Serializable {
    private static final long serialVersionUID = -9089867490827968402L;

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "buyPackageQuantity", value = "订货包装数")
    private BigDecimal buyPackageQuantity;
}
