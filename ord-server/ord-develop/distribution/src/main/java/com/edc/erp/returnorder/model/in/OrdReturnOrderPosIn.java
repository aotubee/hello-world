package com.edc.erp.returnorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrdReturnOrderPosIn implements Serializable {
    /**
     * 门店主键
     */
    @ApiModelProperty(name = "storeCode", value = "门店code")
    private String storeCode;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "startTime", value = "开始时间")
    private String startTime;

    @ApiModelProperty(name = "endTime", value = "结束时间")
    private String endTime;
}