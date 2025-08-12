package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 直营订货单明细表头出参类
 *
 * @author wanglidong
 * @since 2022/11/24 16:01
 */
@Data
@ApiModel(value = "BackHeaderOrderDetailOut", description = "直营订货单明细表头出参类")
public class BackHeaderOrderDetailOut extends DirOrderOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "totalQuantity", value = "数量合计")
    private Integer totalQuantity;

    @ApiModelProperty(name = "payableTotalAmount", value = "支付金额")
    private BigDecimal payableTotalAmount;

    @ApiModelProperty(name = "originalUnitTotalPrice", value = "配货金额合计")
    private BigDecimal originalUnitTotalPrice;

    @ApiModelProperty(name = "isShowReleaseAmount", value = "是否需要释放金额")
    private boolean isShowReleaseAmount;
}