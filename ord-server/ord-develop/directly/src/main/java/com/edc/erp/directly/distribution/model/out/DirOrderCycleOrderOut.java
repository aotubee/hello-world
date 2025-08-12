package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-27 09:00
 */
@Data
public class DirOrderCycleOrderOut {

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

    @ApiModelProperty(name = "orderId", value = "订货id")
    private Long orderId;

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

    @ApiModelProperty(name = "isCanEdit", value = "是否能编辑")
    private Integer isCanEdit;

    @ApiModelProperty(name = "createTime", value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(name = "appTopOrderGoodsOutList", value = "top3商品图片集合")
    private List<AppTopDirOrderGoodsOut> appTopOrderGoodsOutList;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;
}
