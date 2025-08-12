package com.edc.erp.common.model.out.stock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName StockWarehouseOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/12/7 11:36
 **/
@Data
public class StockWarehouseOut implements Serializable {
    private static final long serialVersionUID = -7315012816746302077L;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 财务库存
     */
    @ApiModelProperty(name = "financialQty", value = "财务库存")
    private BigDecimal financialQty;

    /**
     * 库存价格
     */
    @ApiModelProperty(name = "stockPrice", value = "库存价格")
    private BigDecimal stockPrice;

    /**
     * 占用库存
     */
    @ApiModelProperty(name = "takeQty", value = "占用库存")
    private BigDecimal takeQty;

    /**
     * 业务库存
     */
    @ApiModelProperty(name = "businessQty", value = "业务库存")
    private BigDecimal businessQty;
}
