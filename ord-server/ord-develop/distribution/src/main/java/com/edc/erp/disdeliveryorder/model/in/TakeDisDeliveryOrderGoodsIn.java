package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author fxw
 * @description: 收货-商品入参
 * @since 2022/10/27 14:37
 */
@Data
public class TakeDisDeliveryOrderGoodsIn implements Serializable {
    private static final long serialVersionUID = 5147127074933920827L;

    /**
     * 配货单明细主键
     */
    @ApiModelProperty(name = "deliveryOrderDetailsId", value = "配货单明细主键", required = true)
    @NotNull(message = "配货单明细主键不能为空")
    private Long deliveryOrderDetailsId;

    /**
     * 收货数
     */
    @ApiModelProperty(name = "arrivalQuantity", value = "收货数", required = true)
    @NotNull(message = "收货数不能为空")
    private BigDecimal arrivalQuantity;

    /**
     * 差异类型
     */
    @ApiModelProperty(name = "differenceType",value = "差异类型")
    private String diffenenceType;

    /**
     * 商品代码
     */
    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "expiry", value = "商品效期码")
    private String expiry;
}
