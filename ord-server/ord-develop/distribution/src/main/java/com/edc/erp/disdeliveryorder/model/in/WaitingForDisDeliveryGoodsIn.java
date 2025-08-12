package com.edc.erp.disdeliveryorder.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 待收货配货单商品查询入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-07-15 18:21
 */
@Data
public class WaitingForDisDeliveryGoodsIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配货单主键
     */
    @ApiModelProperty(name = "deliveryOrderId", value = "配货单主键")
    private Integer deliveryOrderId;

    /**
     * 商品SKU
     */
    @ApiModelProperty(name = "goodsCode", value = "商品SKU")
    private String goodsCode;

    /**
     * 商品条码
     */
    @ApiModelProperty(name = "barCode", value = "商品条码")
    private String barCode;

    /**
     * 商品名称
     */
    @ApiModelProperty(name = "goodsName", value = "商品名称")
    private String goodsName;

    /**
     * 配货单状态
     */
    @ApiModelProperty(name = "deliveryStatusCode", value = "配货单状态")
    private String deliveryStatusCode;

}
