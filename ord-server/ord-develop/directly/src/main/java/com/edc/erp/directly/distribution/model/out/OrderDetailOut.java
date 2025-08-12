package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 直营订货单明细出参
 *
 * @author wanglidong
 * @since 2022/11/16 11:42
 */
@Data
@ApiModel(value = "OrderDetailOut", description = "直营订货单明细出参")
public class OrderDetailOut extends OrdDirOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "smallSortStr", value = "商品小分类中文")
    private String smallSortStr;

    @ApiModelProperty(name = "positionName", value = "仓位名称")
    private String positionName;

    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    @ApiModelProperty(name = "distributionTypeStr",value = "配送方式中文")
    private String distributionTypeStr;

    @ApiModelProperty(name = "activityCode", value = "活动编码")
    private String activityCode;

    @ApiModelProperty(name = "originalUnitTotalPrice", value = "商品配货金额")
    private BigDecimal originalUnitTotalPrice;

    @ApiModelProperty(name = "payUnitTotalPrice", value = "商品支付金额")
    private BigDecimal payUnitTotalPrice;

    @ApiModelProperty(name = "realUnitTotalPrice", value = "实付单价")
    private BigDecimal realUnitTotalPrice;

    @ApiModelProperty(name = "giftOrderDetails", value = "赠品集合")
    private List<OrderDetailOut> giftOrderDetails;

    @ApiModelProperty(name = "invoiceTypeStr", value = "发票类型中文")
    private String invoiceTypeStr;
}