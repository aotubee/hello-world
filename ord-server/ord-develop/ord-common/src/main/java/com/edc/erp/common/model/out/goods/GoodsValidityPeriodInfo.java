/**
 * Copyright © 2010-2022 Everyday Chain. All rights reserved.
 */
package com.edc.erp.common.model.out.goods;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 组织商品效期信息
 *
 * @author: gusiyuan
 * @date: 2024-07-19
 */
@Data
public class GoodsValidityPeriodInfo {

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "标准商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(value = "组织主条码")
    private String orgGoodsBar;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "进项税率")
    private BigDecimal inTax;

    @ApiModelProperty(value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(value = "是否删除")
    private Integer isDelete;

    /**
     * 进项税率ID
     */
    private Integer inTaxId;

    @ApiModelProperty(value = "商品是否管理效期")
    private Integer isManageValidityPeriod;

    @ApiModelProperty(value = "保质期(天)")
    private String expirationDate;

    @ApiModelProperty(value = "到期时间")
    private String expirationTime;

    @ApiModelProperty(value = "商品主图")
    private String imgUrl;
}
