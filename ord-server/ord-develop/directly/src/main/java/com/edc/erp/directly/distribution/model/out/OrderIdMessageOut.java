package com.edc.erp.directly.distribution.model.out;

import com.edc.plugins.common.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订货单创建成功后向单据调度发送消息入参
 *
 * @author : zhangyao@tseveryday.com
 * @date 2021-07-02 11:49
 */
@Data
public class OrderIdMessageOut extends BaseEntity {

    @ApiModelProperty(name = "orderCycle", value = "订货周期主键")
    private Integer orderCycleId;

    @ApiModelProperty(name = "orderId", value = "订货单主键")
    private Long orderId;

    @ApiModelProperty(name = "orderNo", value = "订货单单号")
    private String orderNo;

    @ApiModelProperty(name = "orderTypeConfigId", value = "订单类型主键")
    private Integer orderTypeConfigId;

    @ApiModelProperty(name = "orgCode", value = "组织代码")
    private String orgCode;

    @ApiModelProperty(name = "currentProgressCode", value = "当前流程代码")
    private String currentProgressCode;

    @ApiModelProperty(name = "bizOrgCode",value = "业务组织代码")
    private String bizOrgCode;

//    @ApiModelProperty(name = "orderProcessOutList", value = "订单类型流程出参扩展集合")
//    private List<OrderProcessOut> orderProcessOutList;
}
