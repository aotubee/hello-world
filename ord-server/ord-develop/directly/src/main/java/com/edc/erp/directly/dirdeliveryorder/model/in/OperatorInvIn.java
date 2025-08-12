package com.edc.erp.directly.dirdeliveryorder.model.in;

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

    @ApiModelProperty(value = "是否改变可用库存 Y是N否-统配出/配销出")
    private String isBusinessQty;



}
