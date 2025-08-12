package com.edc.erp.common.model.out.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName GoodsStockSupplyRateOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2024/4/29 17:27
 **/
@Data
public class GoodsStockSupplyRateOut implements Serializable {
    private static final long serialVersionUID = 2691938890248304085L;

    @ApiModelProperty(value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(value = "仓位代码")
    private String stockCode;

    @ApiModelProperty(value = "可用库存")
    private BigDecimal businessQty;

    @ApiModelProperty(value = "总订货量")
    private BigDecimal totalQuantity;

    @ApiModelProperty(value = "满足率")
    private BigDecimal supplyRate;
}
