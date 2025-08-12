/**
 * Copyright © 2010-2019 Everyday Chain. All rights reserved.
 */
package com.edc.erp.disdeliveryorder.model.in;

import com.edc.plugins.common.model.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * PpDeliveryOrderDetailsIn入参查询类
 *
 * @author liwenqiang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisDeliveryOrderDetailsIn extends Page implements Serializable {

    /**
     * 配货单主键
     */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键")
    @NotNull(message = "配货单主键不能为空")
    private Long deliveryOrderId;

    /**
     * 商品code
     */
    @ApiModelProperty(name = "skuCode", value = "商品SKU")
    private String goodsCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;


}
