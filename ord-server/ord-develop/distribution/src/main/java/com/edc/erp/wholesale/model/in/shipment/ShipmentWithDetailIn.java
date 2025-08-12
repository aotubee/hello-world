package com.edc.erp.wholesale.model.in.shipment;

import com.edc.erp.wholesale.shipment.entity.WholesaleShipment;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批发出货单和明细新增入参类
 * @author lx
 * @since 2022-10-26 16:31:45
 */
@Data
public class ShipmentWithDetailIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批发出货单 */
    @ApiModelProperty(name = "wholesaleShipment",value = "批发出货单")
    private WholesaleShipment wholesaleShipment;

    /** 批发出货单明细集合 */
    @ApiModelProperty(name = "wholesaleShipmentDetailList",value = "批发出货单明细集合")
    private List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList;

    @ApiModelProperty(name = "stockId",value = "仓位ID")
    private Integer stockId;
}
