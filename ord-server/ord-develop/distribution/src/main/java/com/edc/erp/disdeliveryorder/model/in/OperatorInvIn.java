package com.edc.erp.disdeliveryorder.model.in;

import com.edc.erp.disdeliveryorder.entity.OrdDisDelivery;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OperatorInvIn implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "发生位置")
    private String occurrenceLocation;

    @ApiModelProperty(value = "实际增/减")
    private String actualLowering;

    @ApiModelProperty(value = "申请增/减")
    private String applyLowering;

    @ApiModelProperty(value = "否改变可用库存 Y是N否-统配出/配销出")
    private String isBusinessQty;



}
