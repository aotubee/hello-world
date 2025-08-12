package com.edc.erp.common.model.in.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 查询允许批发出货业务的商品信息 入参类
 * @author lx
 * @since 2022-10-27 10:54:12
 */
@Data
public class QuerySaleGoodsInfoIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode",value = "商品代码")
    private String goodsCode;

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode",value = "客户代码")
    private String clientCode;

    /** 仓位id */
    @ApiModelProperty(name = "stockId",value = "仓位id")
    private Integer stockId;

    /** 仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode",value = "业务组织代码")
    private String bizOrgCode;

    /** 批发类型(out：出货，returns：退货) */
    @ApiModelProperty(name = "wholesaleOrderType", value = "批发类型(out：出货，returns：退货)")
    private String wholesaleOrderType;

    /** 允许批发(出货和退货)条件字段 */
    @ApiModelProperty(name = "isOutReturn", value = "允许批发(出货和退货)条件字段")
    private String isOutReturn;

    /**
     * 商品代码集合
     */
    @ApiModelProperty(name = "goodsCodeList",value = "商品代码集合")
    private List<String> goodsCodeList;

    /** 批发价格组代码 */
    @ApiModelProperty(name = "priceGroupCode", value = "批发价格组代码")
    private String priceGroupCode;
}
