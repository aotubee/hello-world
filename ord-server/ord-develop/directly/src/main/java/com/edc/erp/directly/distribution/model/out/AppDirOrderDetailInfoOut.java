package com.edc.erp.directly.distribution.model.out;

import com.edc.erp.directly.distribution.entity.OrdDirOrderDetail;
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
public class AppDirOrderDetailInfoOut extends BaseEntity {

    @ApiModelProperty(name = "appOrderHeader", value = "订单头")
    private AppDirOrderHeaderOut appOrderHeader;

    @ApiModelProperty(name = "orderDetailList", value = "订货明细")
    private List<AppOrderDetailOut> orderDetailList;
}
