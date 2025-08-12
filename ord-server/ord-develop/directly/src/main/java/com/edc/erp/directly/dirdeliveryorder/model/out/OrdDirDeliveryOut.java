package com.edc.erp.directly.dirdeliveryorder.model.out;


import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDeliveryDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 配货单出参
 * @since 2022/10/25 15:14
 */
@Data
@ApiModel(value = "OrdDirDeliveryOut", description = "配货单出参")
public class OrdDirDeliveryOut extends OrdDirDelivery implements Serializable {
    private static final long serialVersionUID = 8174752115585812239L;

    @ApiModelProperty(value = "配货单明细出参列表")
    private List<OrdDirDeliveryDetailOut> detailOutList;

    @ApiModelProperty(value = "配货单明细")
    private List<OrdDirDeliveryDetail> detailList;
}
