package com.edc.erp.directly.dirdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发货入参
 */
@Data
public class UpdateDirDeliveryInfoIn implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "id",value = "配货单id")
    private Long id;

    @ApiModelProperty(name = "updateDirDeliveryDetailInList",value = "发货明细信息入参")
    private List<UpdateDirDeliveryDetailIn> updateDirDeliveryDetailInList;
}
