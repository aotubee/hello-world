package com.edc.erp.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-26 10:14
 */
@Data
public class AppDisOrderDetailInfoOut extends BaseEntity {

    @ApiModelProperty(name = "appOrderHeader", value = "订单头")
    private AppDisOrderHeaderOut appOrderHeader;

    @ApiModelProperty(name = "orderDetailList", value = "订货明细")
    private List<AppOrderDetailOut> orderDetailList;
}
