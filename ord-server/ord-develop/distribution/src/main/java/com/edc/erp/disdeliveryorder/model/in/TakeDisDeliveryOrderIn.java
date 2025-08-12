package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author fxw
 * @description: 收货入参实体
 * @since 2022/10/27 14:30
 */
@Data
public class TakeDisDeliveryOrderIn implements Serializable {
    private static final long serialVersionUID = -799465648422034749L;

    /**
     * 配货单主键
     */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键",required = true)
    @NotNull
    private Long deliveryOrderId;

    /**
     * 收货-商品入参
     */
    @ApiModelProperty(name = "takeDeliveryOrderGoodsInList", value = "收货-商品入参",required = true)
    @NotNull(message = "收货-商品入参不能为空")
    @Valid
    private List<TakeDisDeliveryOrderGoodsIn> takeDeliveryOrderGoodsInList;

    /**
     * 登录人
     */
    @ApiModelProperty(name = "loginUsername", value = "登录人")
    private String loginUsername;

    /**
     *收货备注
     */
    @ApiModelProperty(name = "takeRemark", value = "收货备注")
    private String takeRemark;

    /**
     * 附件地址集合
     */
    @ApiModelProperty(name = "attachmentUrlList", value = "附件地址集合")
    private List<String> attachmentUrlList;
}
