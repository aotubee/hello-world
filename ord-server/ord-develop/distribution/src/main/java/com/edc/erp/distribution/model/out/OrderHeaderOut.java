package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author fxw
 * @description: 订货单表头信息
 * @since 2022/11/14 15:53
 */
@Data
@ApiModel(description = "订货单表头信息")
public class OrderHeaderOut implements Serializable {

    private static final long serialVersionUID = -7638124126612273950L;

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
