package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发货信息入参
 */
@Data
@ApiModel(description = "发货信息入参",value = "UpdateDeliveryInfoIn")
public class UpdateDisDeliveryInfoIn implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "id",value = "配销单id")
    private Long id;

    @ApiModelProperty(name = "updateDeliveryDetailInList",value = "发货明细信息入参")
    private List<UpdateDisDeliveryDetailIn> updateDeliveryDetailInList;
}
