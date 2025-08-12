package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 配货单回传通知明细
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-02-24 15:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandleDeliveryOrderDetailIn extends BaseEntity {

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "deliveryQuantity", value = "实配数量")
    private BigDecimal deliveryQuantity;

    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;

}
