package com.edc.erp.distribution.model.in;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-06-29 18:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderQueryIn extends BaseEntity {

    @ApiModelProperty(name = "storeCode", value = "门店代码")
    private String storeCode;

    @ApiModelProperty(name = "orgCode", value = "业务组织代码")
    private String bizOrgCode;

    @ApiModelProperty(name = "sourceCodeList", value = "下单来源集合")
    private List<String> sourceCodeList;

    @ApiModelProperty(name = "orderStatusCodeList", value = "订货单状态集合")
    private List<String> orderStatusCodeList;

    @ApiModelProperty(name = "truncationDateTime", value = "截单时间")
    private String truncationDateTime;

    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Integer orderTypeConfigId;

    @ApiModelProperty(name = "orderCycleId", value = "订货周期主键")
    private Integer orderCycleId;
}
