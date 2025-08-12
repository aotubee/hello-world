package com.edc.erp.disdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 收货信息出参类
 * @since 2022/10/26 16:31
 */
@Data
public class TakeDeliveryInfoOut implements Serializable {
    private static final long serialVersionUID = 3243168423061277080L;

    @ApiModelProperty(name = "takeRemark", value = "收货备注")
    private String takeRemark;

    @ApiModelProperty(name = "takeAttachmentUrlList", value = "收货附件")
    private List<String> takeAttachmentUrlList;

    @ApiModelProperty(name = "disDeliveryOrderSigningOut",value = "配销单签收对象")
    private DisDeliveryOrderSigningOut disDeliveryOrderSigningOut;
}
