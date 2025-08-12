package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-07-13 17:47
 */
@Data
public class HandleReturnOrderDetailAuditIn extends BaseEntity {

    /**
     * 商品SKU
     */
    @ApiModelProperty(name = "goodsCode", value = "商品SKU")
    private String goodsCode;

    /**
     * 批准数量
     */
    @ApiModelProperty(name = "ratifyReturnQuantity", value = "批准数量")
    private BigDecimal ratifyReturnQuantity;

    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

}
