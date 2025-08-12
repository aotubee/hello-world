package com.edc.erp.wholesale.model.in.shipment;

import com.edc.erp.wholesale.shipment.entity.WholesaleShipmentDetail;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author lx
 * @since 2022-10-28 15:44:12
 */
@Data
public class WholesaleShipmentDetailIn extends WholesaleShipmentDetail implements Serializable {

    /** 客户代码 */
    @ApiModelProperty(name = "clientCode",value = "客户代码")
    private String clientCode;

    /** 仓位id */
    @ApiModelProperty(name = "stockId",value = "仓位id")
    private Integer stockId;

    /** 仓储代码 */
    @ApiModelProperty(name = "shipmentWrh", value = "仓储代码")
    private String shipmentWrh;

    /** 仓位代码 */
    @ApiModelProperty(name = "stockCode", value = "仓位代码")
    private String stockCode;

    /**
     * 进项税率
     */
    @ApiModelProperty(name = "inTax",value = "进项税率")
    private BigDecimal inTax;

    /**
     * 销项税率
     */
    @ApiModelProperty(name = "outTax",value = "销项税率")
    private BigDecimal outTax;
}
