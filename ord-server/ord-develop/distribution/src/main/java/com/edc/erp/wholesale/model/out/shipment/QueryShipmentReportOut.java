package com.edc.erp.wholesale.model.out.shipment;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 批发出货单和明细结果集
 * @author zy
 * @since 2023-10-24 16:41:26
 */
@Data
public class QueryShipmentReportOut implements Serializable {


    private static final long serialVersionUID = 2499427321960028964L;
    @ApiModelProperty(name = "applicationQuantity", value = "申请数量")
    private Integer applicationQuantity;

    /** 申请金额 */
    @ApiModelProperty(name = "applicationAmount", value = "申请金额")
    private BigDecimal applicationAmount;

//    /** 审核数量 */
//    @ApiModelProperty(name = "auditQuantity", value = "审核数量")
//    private Integer auditQuantity;
//
//    /** 审核金额 */
//    @ApiModelProperty(name = "auditAmount", value = "审核金额")
//    private BigDecimal auditAmount;
//

    /** 出库数量 */
    @ApiModelProperty(name = "shipmentQuantity", value = "出库数量")
    private Integer shipmentQuantity;

    /** 实际出库金额 */
    @ApiModelProperty(name = "practicalShipmentAmount", value = "实际出库金额")
    private BigDecimal practicalShipmentAmount;

}
