package com.edc.erp.disdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName FreezeDeliveryOrderBaseInfo
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/7/19 19:26
 **/
@Data
public class FreezeDeliveryOrderBaseInfo implements Serializable {

    private static final long serialVersionUID = 7138113640888709416L;

    @ApiModelProperty(name = "deliveryOrderId", value = "配销单ID")
    private Long deliveryOrderId;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配销单单号")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "deliveryStatusCode", value = "配销单状态")
    private String deliveryStatusCode;

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "distributionAmount", value = "审核金额")
    private BigDecimal distributionAmount;
}
