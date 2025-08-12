package com.edc.erp.common.model.in.purchase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author LZQ
 * @date 2023年01月12日 17:56
 * 查询订单明细入参
 */
@Data
@Builder
public class OrdDeliveryDetailIn implements Serializable {
    private static final long serialVersionUID = 1L;


    /** 配销单状态 */
    @ApiModelProperty(name = "deliveryStatusCode", value = "配销单状态")
    private String deliveryStatusCode;


    /** 配销方式 */
    @ApiModelProperty(name = "distributionType", value = "配销方式")
    private String distributionType;

    /** 订单方代码 */
    @ApiModelProperty(name = "vendorCode", value = "订单方代码")
    private String vendorCode;

    /** 转单优先级 */
    @ApiModelProperty(name = "orderPriority", value = "转单优先级")
    private String orderPriority;

    /** 结转周期号 */
    @ApiModelProperty(name = "carryForwardCycle", value = "结转周期号")
    private String carryForwardCycle;
}
