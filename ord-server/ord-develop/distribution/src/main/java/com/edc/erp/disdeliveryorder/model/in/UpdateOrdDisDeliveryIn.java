package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author LZQ
 * @date 2023年01月06日 10:16
 * 修改配销单信息入参
 */
@Data
@Builder
public class UpdateOrdDisDeliveryIn implements Serializable {
    private static final long serialVersionUID = 428318948846582085L;

    /** 配销单状态 */
    @ApiModelProperty(name = "deliveryStatusCode", value = "配销单状态")
    private String deliveryStatusCode;


    /** 配销单修改的状态 */
    @ApiModelProperty(name = "deliveryStatusCodeUpdate", value = "配销单修改的状态")
    private String deliveryStatusCodeUpdate;

    /** 配销方式 */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionType;

    /** 配销单id */
    @ApiModelProperty(name = "deliveryOrderId", value = "配销单id")
    private Long deliveryOrderId;

    @ApiModelProperty(name = "distributionQuantity", value = "配销数量")
    private BigDecimal distributionQuantity;

    @ApiModelProperty(name = "distributionAmount",value = "配销金额")
    private BigDecimal distributionAmount;

}
