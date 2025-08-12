package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * @return: 直营订货单表头信息
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
@ApiModel(value = "OrderHeaderOut", description = "直营订货单表头信息")
public class OrderHeaderOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "orderNo", value = "订货单号")
    private String orderNo;

    @ApiModelProperty(name = "orderStatusCode", value = "订货单状态")
    private String orderStatusCode;

    @ApiModelProperty(name = "orderStatusCodeStr", value = "订货单状态中文")
    private String orderStatusCodeStr;

    @ApiModelProperty(name = "orderAmount", value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(name = "preferentialAmount", value = "优惠金额")
    private BigDecimal preferentialAmount;

    @ApiModelProperty(name = "orderTypeCode", value = "订单类型编码")
    private String orderTypeCode;

    @ApiModelProperty(name = "orderTypeName", value = "订单类型名称")
    private String orderTypeName;

    @ApiModelProperty(name = "cutOffTime", value = "截单时间")
    private LocalDateTime cutOffTime;

    @ApiModelProperty(name = "sourceCode", value = "订货类型")
    private String sourceCode;

    @ApiModelProperty(name = "sourceCodeStr", value = "订货类型中文")
    private String sourceCodeStr;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "storeName", value = "门店名称")
    private String storeName;

    @ApiModelProperty(name = "submitter", value = "提交人")
    private String submitter;

    @ApiModelProperty(name = "sumbitTime", value = "提交时间")
    private LocalDateTime sumbitTime;

    @ApiModelProperty(name = "requestOrderNo", value = "要货单单号")
    private String requestOrderNo;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(name = "distributionOrderNo", value = "分货单号")
    private String distributionOrderNo;
}