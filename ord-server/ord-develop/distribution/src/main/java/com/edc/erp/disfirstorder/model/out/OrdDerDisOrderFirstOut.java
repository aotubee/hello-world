package com.edc.erp.disfirstorder.model.out;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import com.edc.erp.disfirstorder.entity.OrdDisOrderFirst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *配销铺货单出参
 * @author weichao
 */
@Data
public class OrdDerDisOrderFirstOut extends OrdDisOrderFirst {

    @ApiModelProperty(value = "铺货单状态中文")
    private String firstOrderStatusStr;

    @ApiModelProperty(value = "配货单号集合")
    private List<OrdDisDelivery> deliveryOrders;
}
