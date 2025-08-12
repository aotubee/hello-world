package com.edc.erp.wholesale.model.in.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 出货单冲销入参类
 * @author lx
 * @since 2022-11-01 17:13:23
 */
@Data
public class ShipmentOrderWriteOffIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 批发出货单id */
    @ApiModelProperty(name = "wholesaleShipmentId", value = "批发出货单id")
    private Long wholesaleShipmentId;

    /** 批发出货单明细集合 */
    @ApiModelProperty(name = "wholesaleShipmentDetailList",value = "批发出货单明细集合")
    private List<WholesaleShipmentDetailIn> wholesaleShipmentDetailList;

    /** 业务组织代码 */
    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
