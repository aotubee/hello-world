package com.edc.erp.directly.dirdeliveryorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-11-05 09:37
 */
@Data
public class ResetTakeDirDeliveryIn extends BaseEntity {

    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键",required = true)
    @NotNull(message = "请指定配货单")
    private Long deliveryOrderId;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码",required = true)
    @NotEmpty(message = "组织代码不能为空")
    private String bizOrgCode;
}
