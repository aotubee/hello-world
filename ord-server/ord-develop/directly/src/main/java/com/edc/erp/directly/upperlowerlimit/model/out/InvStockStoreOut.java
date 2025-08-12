package com.edc.erp.directly.upperlowerlimit.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 门店库存出参
 *
 * @author weichao
 */
@Data
public class InvStockStoreOut {
    /**
     * 门店代码
     */
    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

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
     * 占用库存
     */
    @ApiModelProperty(name = "takeQty", value = "占用库存")
    private BigDecimal takeQty;

    /**
     * 业务库存
     */
    @ApiModelProperty(name = "businessQty", value = "业务库存")
    private BigDecimal businessQty;

    /**
     * 库存价格
     */
    @ApiModelProperty(name = "stockPrice", value = "库存价格")
    private BigDecimal stockPrice;

    /**
     * 公司代码
     */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

}
