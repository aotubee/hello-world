package com.edc.erp.directly.dirfirstorder.model.out;

import com.edc.erp.directly.dirdeliveryorder.entity.OrdDirDelivery;
import com.edc.erp.directly.dirfirstorder.entity.OrdDirOrderFirst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *直营配货铺货单出参
 * @author weichao
 */
@Data
public class OrdDerDirOrderFirstOut extends OrdDirOrderFirst {

    @ApiModelProperty(value = "铺货单状态中文")
    private String firstOrderStatusStr;

    @ApiModelProperty(value = "配货单号集合")
    private List<OrdDirDelivery> deliveryOrders;
}
