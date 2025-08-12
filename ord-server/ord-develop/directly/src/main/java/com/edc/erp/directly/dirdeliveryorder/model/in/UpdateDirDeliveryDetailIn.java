package com.edc.erp.directly.dirdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class UpdateDirDeliveryDetailIn implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "id",value = "明细id")
    private Long id;

    @ApiModelProperty(name = "deliveryQuantity",value = "发货数量")
    private BigDecimal deliveryQuantity;
}
