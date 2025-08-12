package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订货单详细出参
 *
 * @author wanglidong
 * @since 2022/11/23 16:54
 */
@Data
@ApiModel(value = "OrdDirOrderDetailOut", description = "订货单详细出参")
public class OrdDirOrderDetailOut extends OrdDirOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 品类属性中文
     */
    @ApiModelProperty(name = "goodsTypeStr", value = "品类属性中文")
    private String goodsTypeStr;

    /**
     * 商品小分类中文
     */
    @ApiModelProperty(name = "smallSortStr", value = "商品小分类中文")
    private String smallSortStr;

    /**
     * 仓位代码中文
     */
    @ApiModelProperty(name = "positionStr", value = "仓位代码中文")
    private String positionStr;

    /**
     * 配送方式中文
     */
    @ApiModelProperty(name = "distributionTypeStr", value = "配送方式中文")
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
    private List<OrdDirOrderDetailOut> giftOrderDetails;
}
