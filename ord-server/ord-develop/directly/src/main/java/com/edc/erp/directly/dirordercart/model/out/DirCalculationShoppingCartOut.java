package com.edc.erp.directly.dirordercart.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-13 15:51
 */
@Data
public class DirCalculationShoppingCartOut extends BaseEntity {

    @ApiModelProperty(name = "accountBalance", value = "账户余额")
    private BigDecimal accountBalance;

    @ApiModelProperty(name = "orderAmount", value = "订货金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(name = "calculationOrderCycleOutList", value = "核算订货周期金额")
    private List<DirCalculationOrderCycleOut> calculationOrderCycleOutList;

    @ApiModelProperty(name = "preferentialAmount", value = "优惠金额")
    private BigDecimal preferentialAmount;
}
