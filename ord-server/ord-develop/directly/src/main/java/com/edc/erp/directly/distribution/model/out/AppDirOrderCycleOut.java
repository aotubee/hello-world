package com.edc.erp.directly.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-23 11:44
 */
@Data
public class AppDirOrderCycleOut extends BaseEntity {

    @ApiModelProperty(name = "orderCycleId", value = "订货周期id")
    private Integer orderCycleId;

    @ApiModelProperty(name = "shortOrderType", value = "订单类型简称")
    private String shortOrderType;

    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private LocalDateTime truncationDateTime;

    @ApiModelProperty(name = "minimumOrderAmount", value = "起送额")
    private BigDecimal minimumOrderAmount;

    @ApiModelProperty(name = "totalAmount", value = "总额，如果有支付流程，则是已付款总额，否则全部金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(name = "appOrderOutList", value = "订货单集合")
    private List<AppDirOrderOut> appOrderOutList;
}
