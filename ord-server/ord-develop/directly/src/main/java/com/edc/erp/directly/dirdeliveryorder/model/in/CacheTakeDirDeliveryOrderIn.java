package com.edc.erp.directly.dirdeliveryorder.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-11-04 16:02
 */
@Data
public class CacheTakeDirDeliveryOrderIn extends BaseEntity {

    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键",required = true)
    @NotNull
    private Long deliveryOrderId;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码",required = true)
    @NotEmpty(message = "组织代码不能为空")
    private String bizOrgCode;

    @ApiModelProperty(name = "cacheTakeDeliveryOrderGoodsInList", value = "缓存收货-商品入参",required = true)
    @NotNull(message = "缓存收货-商品入参不能为空")
    @Valid
    private List<CacheTakeDirDeliveryOrderGoodsIn> cacheTakeDeliveryOrderGoodsInList;
}
