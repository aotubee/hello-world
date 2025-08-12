package com.edc.erp.directly.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-26 09:54
 */
@Data
public class AppDirOrderHeaderOut extends BaseEntity {

    @ApiModelProperty(name = "orderNo", value = "订货单号")
    private String orderNo;

    @ApiModelProperty(name = "orderStatusCode", value = "订货单状态")
    private String orderStatusCode;

    @ApiModelProperty(name = "orderStatusCodeStr", value = "订货单状态中文")
    private String orderStatusCodeStr;

    @ApiModelProperty(name = "orderAmount", value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(name = "payableAmount", value = "应付金额")
    private BigDecimal payableAmount;

    @ApiModelProperty(name = "preferentialAmount", value = "优惠金额")
    private BigDecimal preferentialAmount;

    @ApiModelProperty(name = "sourceCode", value = "订货类型")
    private String sourceCode;

    @ApiModelProperty(name = "sourceCodeStr", value = "订货类型中文")
    private String sourceCodeStr;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;
}
