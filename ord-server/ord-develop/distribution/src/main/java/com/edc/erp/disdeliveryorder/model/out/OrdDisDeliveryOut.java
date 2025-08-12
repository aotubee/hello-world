package com.edc.erp.disdeliveryorder.model.out;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disdeliveryorder.entity.OrdDisDeliveryDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 配销单出参
 * @since 2022/10/25 15:14
 */
@Data
@ApiModel(value = "OrdDisDeliveryOut",description = "配销单出参")
public class OrdDisDeliveryOut extends OrdDisDelivery implements Serializable {
    private static final long serialVersionUID = 8174752115585812239L;

    @ApiModelProperty(value = "配销单明细出参列表")
    private List<OrdDisDeliveryDetailOut> detailOutList;

    @ApiModelProperty(value = "配销单明细")
    private List<OrdDisDeliveryDetail> detailList;
}
