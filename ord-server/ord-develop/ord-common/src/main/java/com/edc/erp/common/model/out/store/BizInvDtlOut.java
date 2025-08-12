/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */

package com.edc.erp.common.model.out.store;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 业务可用库存明细出参
 *
 * @author: lee
 * @date: 2022-08-31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizInvDtlOut implements Serializable {

    private static final long serialVersionUID = 590367413578909856L;

    public BizInvDtlOut(String goodsCode, BigDecimal bizInvQty){
        this.goodsCode = goodsCode;
        this.bizInvQty = bizInvQty;
    }

    /** 组织商品ID */
    @ApiModelProperty(name = "orgGoodsId", value = "组织商品ID")
    @Id
    private Long orgGoodsId;

    /** 商品代码 */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    /** 可用库存 */
    @ApiModelProperty(name = "bizInvQty", value = "可用业务库存")
    private BigDecimal bizInvQty;

    /** 商品条码 */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /** 商品名称 */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /** 仓储代码 */
    @ApiModelProperty(name = "warehouseCode", value = "仓储代码")
    private String warehouseCode;

    /** 仓储名称 */
    @ApiModelProperty(name = "warehouseName", value = "仓储名称")
    private String warehouseName;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /** 仓位名称 */
    @ApiModelProperty(name = "stockName", value = "仓位名称")
    private String stockName;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    /** 公司代码 */
    @ApiModelProperty(name = "orgCode", value = "公司代码")
    private String orgCode;
}
