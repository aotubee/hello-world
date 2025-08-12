package com.edc.erp.common.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 在单量对象
 * @author lishaobo
 * @date 2021-12-16
 */
@Data
public class InOneQtyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(value = "在单量")
    private BigDecimal qty;
}
