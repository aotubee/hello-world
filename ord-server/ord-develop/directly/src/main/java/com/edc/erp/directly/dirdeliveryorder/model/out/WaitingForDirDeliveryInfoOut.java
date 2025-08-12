package com.edc.erp.directly.dirdeliveryorder.model.out;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 待收货配货单信息
 *
 * @author : zhangyao@tseveryday.com
 * @date 2020-07-15 18:08
 */
@Data
public class WaitingForDirDeliveryInfoOut implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数量合计
     */
    @ApiModelProperty(name = "totalQuantity", value = "数量合计")
    private BigDecimal totalQuantity;

    /**
     * 品项数
     */
    @ApiModelProperty(name = "skuItemQuantity", value = "品项数")
    private Integer skuItemQuantity;

    /**
     * 总箱数
     */
    @ApiModelProperty(name = "totalPackageQuantity", value = "总箱数")
    private BigDecimal totalPackageQuantity;

    /**
     * 配货单商品明细对象
     */
    @ApiModelProperty(name = "deliveryGoodsInfoList", value = "配货单商品明细对象")
    private List<WaitingForDirDeliveryGoodsOut> deliveryGoodsInfoList;
}
