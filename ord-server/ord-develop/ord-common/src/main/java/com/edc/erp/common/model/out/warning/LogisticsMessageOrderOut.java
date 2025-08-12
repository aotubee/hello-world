package com.edc.erp.common.model.out.warning;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-08-11 12:05
 */
@Data
public class LogisticsMessageOrderOut extends BaseEntity {

    @ApiModelProperty(name = "id", value = "主键")
    private Integer id;

    @ApiModelProperty(name = "orderCycleId", value = "订货周期主键")
    private Integer orderCycleId;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private LocalDateTime truncationDateTime;

    @ApiModelProperty(name = "minimumOrderAmount", value = "起送额")
    private BigDecimal minimumOrderAmount;

    @ApiModelProperty(name = "orderNo", value = "订货单号")
    private String orderNo;

    @ApiModelProperty(name = "orderStatusCode", value = "订货单状态")
    private String orderStatusCode;

    @ApiModelProperty(name = "orderAmount", value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(name = "payableAmount", value = "应付金额")
    private BigDecimal payableAmount;

    @ApiModelProperty(name = "preferentialAmount", value = "优惠金额")
    private BigDecimal preferentialAmount;

    @ApiModelProperty(name = "sourceCode", value = "订货类型")
    private String sourceCode;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    private String bizOrgCode;

}
