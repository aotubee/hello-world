package com.edc.erp.distribution.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-24 15:16
 */
@Data
public class UpdateDisOrderIn extends BaseEntity {

    @ApiModelProperty(name = "orderId", value = "订单id",required = true)
    @NotNull(message = "订货单主键不能为空")
    private Long orderId;

    @ApiModelProperty(name = "bizOrgCode", value = "组织代码")
    @NotEmpty(message = "组织代码不能为空")
    private String bizOrgCode;

    @ApiModelProperty(name = "updateOrderGoodsInList", value = "修改的商品集合",required = true)
    @NotNull(message = "修改的商品集合不能为空")
    private List<UpdateDisOrderGoodsIn> updateOrderGoodsInList;
}
