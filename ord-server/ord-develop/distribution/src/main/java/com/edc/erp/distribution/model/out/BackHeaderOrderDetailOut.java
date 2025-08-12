package com.edc.erp.distribution.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 运营端订货单明细表头
 * @since 2022/11/15 11:47
 */
@Data
@ApiModel(value = "BackHeaderOrderDetailOut",description = "运营端订货单明细表头")
public class BackHeaderOrderDetailOut extends DisOrderOut implements Serializable {

    private static final long serialVersionUID = 4427697240649884959L;

    @ApiModelProperty(name = "totalQuantity", value = "数量合计")
    private Integer totalQuantity;

    @ApiModelProperty(name = "payableTotalAmount", value = "支付金额")
    private BigDecimal payableTotalAmount;



    @ApiModelProperty(name = "originalUnitTotalPrice", value = "配货金额合计")
    private BigDecimal originalUnitTotalPrice;


    @ApiModelProperty(name = "isShowReleaseAmount", value = "是否需要释放金额")
    private boolean isShowReleaseAmount;
}
