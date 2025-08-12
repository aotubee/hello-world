package com.edc.erp.directly.returnorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrdReturnOrderPosDetailIn implements Serializable {

    @ApiModelProperty(name = "returnOrderId", value = "退货单主键")
    private Long returnOrderId;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;
}
