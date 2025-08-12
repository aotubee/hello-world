package com.edc.erp.wholesale.model.out.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批发出货单和明细结果集
 * @author lx
 * @since 2022-10-26 16:41:26
 */
@Data
public class ShipmentWithDetailOut extends WholesaleShipmentOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 出货单明细结果集合 */
    @ApiModelProperty(name = "wholesaleShipmentDetailOuts",value = "出货单明细结果集合")
    private List<WholesaleShipmentDetailOut> wholesaleShipmentDetailOuts;
}
