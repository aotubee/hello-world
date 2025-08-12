package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-07-14 09:50
 */
@Data
public class HandleDeliveryOrderDetailAuditIn extends BaseEntity {

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "distributionQuantity", value = "配货量")
    private BigDecimal distributionQuantity;


    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;
}
