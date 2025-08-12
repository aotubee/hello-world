package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(description = "修改收货明细信息入参",value = "UpdateDeliveryDetailIn")
public class UpdateDisDeliveryDetailIn implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "id",value = "明细id")
    private Long id;

    @ApiModelProperty(name = "deliveryQuantity",value = "实配数量")
    private BigDecimal deliveryQuantity;
}
