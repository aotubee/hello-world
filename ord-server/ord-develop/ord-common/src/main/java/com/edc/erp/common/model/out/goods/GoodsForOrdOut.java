/**
 * com.edc.
 * Copyright (c) 2019-2020 All Rights Reserved.
 */
package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @description: 查询商品（订单业务用）
 * @author gusiyuan
 * @since 2023/11/15 14:56
 */
@Data
public class GoodsForOrdOut implements Serializable {

    private static final long serialVersionUID = 523996091973843691L;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "orgGoodsName", value = "商品名称")
    private String goodsName;

    /**
     * 条码代码
     */
    @ApiModelProperty(name = "barCode", value = "条码代码")
    private String barCode;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /**
     * 配货规格Id
     */
    @ApiModelProperty(name = "distributionSpecId", value = "配货规格Id")
    private Long distributionSpecId;

    /**
     * 配货规格
     */
    @ApiModelProperty(name = "distributionSpecification", value = "配货规格")
    private StandardSpecTransInfoOut distributionSpecification;

    /**
     * 品类
     */
    @ApiModelProperty(name = "sortName", value = "品类名称")
    private String sortName;

    /**
     * 品类代码
     */
    @ApiModelProperty(name = "sort", value = "品类代码")
    private String sort;

    /**
     * 品类代码
     */
    @ApiModelProperty(name = "sortId", value = "品类id")
    private Integer sortId;

    /**
     * 仓位
     */
    @ApiModelProperty(name = "stockName", value = "仓位")
    private String stockName;

    /**
     * 仓位代码
     */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 配货价
     */
    @ApiModelProperty(name = "distributionPrice", value = "配货价")
    private BigDecimal distributionPrice;

    /**
     * 业务组织代码
     */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /**
     * 配货方式
     */
    @ApiModelProperty(name = "distributionWay", value = "配货方式")
    private String distributionWay;

    /**
     * 配货单价
     */
    @ApiModelProperty(name = "distributionUnitPrice", value = "配货单价")
    private BigDecimal distributionUnitPrice;

    @ApiModelProperty(value = "发票类型")
    private String invoiceType;

}
