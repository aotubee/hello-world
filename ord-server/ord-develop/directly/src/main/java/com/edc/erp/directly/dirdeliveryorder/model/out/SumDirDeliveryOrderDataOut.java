package com.edc.erp.directly.dirdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName SumDisDeliveryOrderDataOut
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/23 16:43
 **/
@Data
public class SumDirDeliveryOrderDataOut implements Serializable {
    private static final long serialVersionUID = 2369503158959360249L;


    /** 集货数量 */
    @ApiModelProperty(name = "orderQuantity", value = "集货数量")
    private BigDecimal orderQuantity;

    /** 集货金额 */
    @ApiModelProperty(name = "orderAmount", value = "集货金额")
    private BigDecimal orderAmount;

    /** 配销数量 */
    @ApiModelProperty(name = "distributionQuantity", value = "配销数量")
    private BigDecimal distributionQuantity;

    /** 配销金额 */
    @ApiModelProperty(name = "distributionAmount", value = "配销金额")
    private BigDecimal distributionAmount;

    /** 实配数量 */
    @ApiModelProperty(name = "deliveryQuantity", value = "实配数量")
    private BigDecimal deliveryQuantity;

    /** 实配金额 */
    @ApiModelProperty(name = "deliveryAmount", value = "实配金额")
    private BigDecimal deliveryAmount;

    @ApiModelProperty(name = "totalArrivalQuantity",value = "总实收数量")
    private BigDecimal totalArrivalQuantity;

    @ApiModelProperty(name = "totalArrivalAmount",value = "总实收金额")
    private BigDecimal totalArrivalAmount;
}
