package com.edc.erp.directly.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车订货周期类型
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-06-28 14:54
 */
@Data
public class OrderCycleHeaderOut extends BaseEntity {

    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Integer orderTypeConfigId;

    @ApiModelProperty(name = "shortOrderType", value = "订单类型简称")
    private String shortOrderType;

    @ApiModelProperty(name = "orderTypeCode", value = "订单类型代码")
    private String orderTypeCode;

    @ApiModelProperty(name = "truncationTime", value = "截单时间")
    private LocalDateTime truncationTime;

    @ApiModelProperty(name = "minimumOrderAmount", value = "起送额")
    private BigDecimal minimumOrderAmount;

    @ApiModelProperty(name = "minAmountCheckType", value = "起送额校验方式")
    private String minAmountCheckType;

    @ApiModelProperty(name = "goodsList", value = "商品集合")
    private List<OrderCartGoodsOut> goodsList;

    @ApiModelProperty(name = "distributionAndUpDownOrderPaidAmount", value = "已付款分货与跑货金额")
    private BigDecimal distributionAndUpDownOrderPaidAmount;

    @ApiModelProperty(name = "orderCycleId", value = "配货订货周期主键")
    private Integer orderCycleId;
}
