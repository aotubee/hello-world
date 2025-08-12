package com.edc.erp.common.model.in.zk;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2022-03-29 09:06
 */
@Data
public class HandleReturnOrderDetailReceiveIn extends BaseEntity {

    @ApiModelProperty(name = "goodsCode", value = "商品代码")
    private String goodsCode;

    @ApiModelProperty(name = "returnQuantity", value = "实退数量")
    private BigDecimal returnQuantity;

    @ApiModelProperty(name = "line", value = "行号")
    private Integer line;
}
