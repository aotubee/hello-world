package com.edc.erp.directly.distribution.model.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * @return: 订货单明细信息出参
 * @Author: fxw
 * @Date: 2022/11/23
 */
@Data
@ApiModel(value = "OrderDetailInfoOut", description = "订货单明细信息出参")
public class OrderDetailInfoOut extends OrderHeaderOut implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(name = "orderDetailList", value = "订货明细")
    private List<OrderDetailOut> orderDetailList;
}