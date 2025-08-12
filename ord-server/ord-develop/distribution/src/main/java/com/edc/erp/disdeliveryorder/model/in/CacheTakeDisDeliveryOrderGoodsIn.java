package com.edc.erp.disdeliveryorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 收货-商品入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-07-16 09:50
 */
@Data
public class CacheTakeDisDeliveryOrderGoodsIn extends BaseEntity {

    /**
     * 配货单明细主键
     */
    @ApiModelProperty(name = "deliveryOrderDetailsId", value = "配货单明细主键", required = true)
    @NotNull(message = "配货单明细主键不能为空")
    private Integer deliveryOrderDetailsId;

    @ApiModelProperty(name = "cacheArrivalQuantity", value = "缓存收货数不能为空")
    private BigDecimal cacheArrivalQuantity;

    @ApiModelProperty(name = "skuCode", value = "商品代码")
    private String skuCode;

    @ApiModelProperty(name = "sweepTheCodeNumber", value = "扫码次数", required = true)
    @NotNull(message = "扫码次数不能为空")
    private Integer sweepTheCodeNumber;
}
