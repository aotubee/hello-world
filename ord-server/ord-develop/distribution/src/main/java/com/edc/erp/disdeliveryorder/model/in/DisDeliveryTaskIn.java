package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName DisDeliveryTaskIn
 * @Description TODO
 * @Author ZhangYao
 * @CreateTime 2023/8/22 8:56
 **/
@Data
public class DisDeliveryTaskIn implements Serializable {
    private static final long serialVersionUID = 4620366051655188935L;

    @ApiModelProperty(name = "deliveryOrderNo", value = "配销单号")
    private String deliveryOrderNo;

    @ApiModelProperty(name = "bizOrgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "isNeedPay", value = "是否需要扣款")
    private Boolean isNeedPay;

    @ApiModelProperty(name = "loginUsername", value = "登录人")
    private String loginUsername;
}
