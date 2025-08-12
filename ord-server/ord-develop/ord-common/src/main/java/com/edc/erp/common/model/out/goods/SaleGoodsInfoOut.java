package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author lx
 * @since 2022-10-27 12:11:02
 */
@Data
public class SaleGoodsInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 组织商品ID */
    @ApiModelProperty(name = "orgGoodsId",value = "组织商品ID")
    private Long orgGoodsId;

    /** 标准商品ID */
    @ApiModelProperty(name = "goodsId",value = "标准商品ID")
    private Long goodsId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode",value = "商品代码")
    private String goodsCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName",value = "商品名称")
    private String goodsName;

    /** 商品条码 */
    @ApiModelProperty(name = "barCode",value = "商品条码")
    private String barCode;

    /** 品类属性 */
    @ApiModelProperty(name = "goodsType",value = "品类属性")
    private String goodsType;

    /** 品类属性 - 中文 */
    @ApiModelProperty(name = "goodsTypeStr",value = "品类属性 - 中文")
    private String goodsTypeStr;

    /** 批发规格ID */
    @ApiModelProperty(name = "wholesaleSpecId",value = "批发规格ID")
    private Long wholesaleSpecId;

    /** 包装规格 */
    @ApiModelProperty(name = "packageSpecification",value = "包装规格")
    private String packageSpecification;

    /** 规格数量 */
    @ApiModelProperty(name = "qpc",value = "规格数量")
    private Integer qpc;

    /** 单位ID */
    @ApiModelProperty(name = "unitId",value = "单位ID")
    private Integer unitId;

    /** 单位名称 */
    @ApiModelProperty(name = "packageUnit",value = "包装单位")
    private String packageUnit;

    /** 配货方式 */
    @ApiModelProperty(name = "distributionWay",value = "配货方式")
    private String distributionWay;

    /** 配货方式 - 中文 */
    @ApiModelProperty(name = "distributionWayStr",value = "配货方式-中文")
    private String distributionWayStr;

    /** 批发价 */
    @ApiModelProperty(name = "salePrice",value = "批发价")
    private BigDecimal salePrice;

    /** 仓位id */
    @ApiModelProperty(name = "stockId",value = "仓位id")
    private Integer stockId;

    /** 库存价 */
    @ApiModelProperty(name = "inventoryPrice",value = "库存价")
    private BigDecimal inventoryPrice;

    /**
     * 进项税率id
     */
    @ApiModelProperty(name = "inTaxId",value = "进项税率id")
    private Integer inTaxId;

    /**
     * 进项税率
     */
    @ApiModelProperty(name = "inTax",value = "进项税率")
    private BigDecimal inTax;

    /**
     * 销项税率id
     */
    @ApiModelProperty(name = "outTaxId",value = "销项税率id")
    private Integer outTaxId;

    /**
     * 销项税率
     */
    @ApiModelProperty(name = "outTax",value = "销项税率")
    private BigDecimal outTax;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode",value = "业务组织代码")
    private String bizOrgCode;

    /** 业务组织代码 */
    @ApiModelProperty(name = "vendorCode",value = "订单方")
    private String vendorCode;

    /** 退货原则ID */
    @ApiModelProperty(name = "returnPrincipleId",value = "退货原则ID")
    private Integer returnPrincipleId;

    /** 退货原则 */
    @ApiModelProperty(name = "returnPrinciple",value = "退货原则")
    private String returnPrinciple;

    /** 是否可执行业务 */
    @ApiModelProperty(name = "isExecute",value = "是否可执行业务")
    private Integer isExecute;

    @ApiModelProperty(name = "businessQty",value = "业务可用库存")
    private BigDecimal businessQty;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

    @ApiModelProperty(name = "standardSpecs", value = "规格集合")
    private List<StandardSpecOut> standardSpecs;
}
