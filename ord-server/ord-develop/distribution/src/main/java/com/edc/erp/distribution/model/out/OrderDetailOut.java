package com.edc.erp.distribution.model.out;

import com.edc.erp.distribution.entity.OrdDisOrderDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author fxw
 * @description: 运营端订货单明细出参
 * @since 2022/11/15 11:16
 */
@Data
public class OrderDetailOut extends OrdDisOrderDetail implements Serializable {
    private static final long serialVersionUID = -4222436605040968934L;

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
