package com.edc.erp.directly.dirordercart.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 核算温层金额
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-05-04 18:26
 */
@Data
public class DirCalculationOrderCycleOut extends BaseEntity {

    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Integer orderTypeConfigId;

    @ApiModelProperty(name = "shortOrderType", value = "订单类型简称")
    private String shortOrderType;

    @ApiModelProperty(name = "listAmount", value = "当前购物车已选中商品总金额,包含补货分货已付款金额")
    private BigDecimal listAmount;

    @ApiModelProperty(name = "isHaveDistributionOrder", value = "是否含有分货单")
    private Integer isHaveDistributionOrder;
}
