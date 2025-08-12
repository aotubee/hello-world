package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author fxw
 * @description: 更新收货心跳时间入参
 * @since 2022/11/1 14:42
 */
@Data
public class UpdateDisDeliveryOrderHeartRateIn implements Serializable {
    private static final long serialVersionUID = 428318948846582085L;

    @ApiModelProperty(name = "deliveryOrderId", value = "配销单主键",required = true)
    @NotNull(message = "请指定配货单")
    private Long deliveryOrderId;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码",required = true)
    @NotEmpty(message = "业务组织代码不能为空")
    private String bizOrgCode;
}
