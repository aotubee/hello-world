package com.edc.erp.distribution.model.in;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除门店商品关联入参类
 * @author lx
 * @since 2022-11-14 10:08:59
 */
@Data
public class DeleteStoreGoodsIn implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分货订单主键
     */
    @ApiModelProperty(name = "distributionOrderId", value = "分货单id")
    private Long distributionOrderId;

    /**
     * 分货门店商品关联表主键
     */
    @ApiModelProperty(name = "storeGoodsId", value = "门店商品明细主键")
    private Long storeGoodsId;
}
